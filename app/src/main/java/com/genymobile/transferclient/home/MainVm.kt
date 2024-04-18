package com.genymobile.transferclient.home

import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genymobile.transfer.ITransferInterface
import com.genymobile.transferclient.MainActivity
import com.genymobile.transferclient.config.PortConfig
import com.genymobile.transferclient.home.data.ApplicationInfo
import com.genymobile.transferclient.home.data.Device
import com.genymobile.transferclient.tools.FileUtils
import com.genymobile.transferclient.tools.RunProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.sourceforge.pinyin4j.PinyinHelper
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.regex.Pattern
import kotlin.concurrent.thread

class MainVm(val mContext: Activity) : ViewModel() {
    private val scope = viewModelScope
    private val TAG = "MainVm"

    class CustomSocket(
        var socket: Socket
    ) {
        val outputStream = socket.getOutputStream()
        val inputStream = socket.getInputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val dataInputStream = DataInputStream(inputStream)
    }

    val devicesList = mutableStateListOf<Device>()
    val socketList = mutableStateListOf<CustomSocket>()
    var homeActiveIndex = mutableStateOf(0)
    val kernelRunning = mutableStateOf(false)

    var transferService: ITransferInterface? = null
    var mainSocket: CustomSocket? = null


    val localDevice by lazy { createDevice(mContext) }

    init {

        scope.launch(Dispatchers.IO) {
            scannerApps()
            asyncStartKernel() //使用Root直接启动服务
            passivityConnection()
            connectionService()
        }
    }

    private fun receiverListener(
        host: String,
        objectOutputStream: ObjectOutputStream,
        objectInputStream: ObjectInputStream,
        customSocket: CustomSocket
    ) {
        thread {
            val dataInputStream = customSocket.dataInputStream
            while (true) {
                val messageType = dataInputStream.readInt()
                //接力app
                if (messageType == MessageType.APP) {
                    Log.d(TAG, "receiverListener: after type")
                    val dynamicPort = dataInputStream.readInt()
                    Log.d(TAG, "receiverListener: after port")
                    Log.d(TAG, "receiverListener: host=${host},dynamicPort=${dynamicPort}")

                    val intent = Intent(mContext, MainActivity::class.java)
                        .putExtra("host", host)
                        .putExtra("dynamicPort", dynamicPort)
                    mContext.startActivity(intent)
                }
            }

        }
    }

    fun appRelay(packageName: String, index: Int) {
        /*
        1通知服务,开始应用接力
        需要发送给服务两个字符串:包名和options
        接收一个int即暴露给其他设备的socket端口
        2通知连接的设备接收新端口
        将端口发送给它
         */
        val optStr =
            "dpi=${localDevice.dpi},displayRegion=0-0-${localDevice.height}-${localDevice.width}"
        Log.d(TAG, "appRelay: optStr=$optStr")


        thread {
            Log.d(TAG, "appRelay: ${mainSocket==null}")
            val mainDataOutputStream = mainSocket!!.dataOutputStream
            mainDataOutputStream.writeInt(MessageType.APP)
            mainDataOutputStream.writeUTF(packageName)
            mainDataOutputStream.writeUTF(optStr)
            val dynamicPort = mainSocket!!.dataInputStream.readInt()

            val dataOutputStream = socketList[index].dataOutputStream
            dataOutputStream.writeInt(MessageType.APP)
            dataOutputStream.writeInt(dynamicPort)

//            val instantName = transferService?.appRunOnTargetDisplay(packageName, optStr)
//            Log.d(TAG, "appRelay: instantName=$instantName")

            //找到本机可用端口,用其转发本地套接字
//            val serverSocket = ServerSocket(0)
//            val dynamicPort = serverSocket.localPort
//            serverSocket.close()
//            RunProcess.runProcess("forward tcp:${dynamicPort} localabstract:${instantName}")
//            val dataOutputStream = socketList[index].dataOutputStream
//            dataOutputStream.writeInt(MessageType.APP)
//            dataOutputStream.flush()
//            dataOutputStream.writeInt(dynamicPort)

        }
    }


    /*
    无论是被动连接还是主动连接对方,得到socket后都要对该socket进行监听
     */
    //被动连接监听
    private suspend fun passivityConnection() {
        thread {
            //平板模拟器临时使用20001端口测试,实际设备20002
//            val serverSocket = ServerSocket(20001)
            val serverSocket = ServerSocket(PortConfig.DEVICE_PORT)
            Log.d(TAG, "onCreate: 等待其他设备连接")
            while (true) {
                //每个其他的设备主动连接该设备
                val fromOtherDeviceSocket = serverSocket.accept()
                val customSocket = CustomSocket(fromOtherDeviceSocket)
                socketList.add(customSocket)
                Log.d(TAG, "新设备连接")
                //
                thread {
                    val outputStream = fromOtherDeviceSocket.getOutputStream()
                    val inputStream = fromOtherDeviceSocket.getInputStream()

                    val objectInputStream = ObjectInputStream(inputStream)
                    val objectOutputStream = ObjectOutputStream(outputStream)

                    val remoteDevice: Device = objectInputStream.readObject() as Device
                    devicesList.add(remoteDevice)
                    objectOutputStream.writeObject(localDevice)
                    objectOutputStream.flush()

                    Log.d(TAG, "onCreate: receive $remoteDevice")

                    receiverListener(
                        fromOtherDeviceSocket.inetAddress.hostAddress!!,
                        objectOutputStream,
                        objectInputStream,
                        customSocket
                    )
                }
            }
        }
    }

    //主动连接
    fun initiativeSocket(it: String) {
        /*
        连接逻辑：
        主动连接的一方发送自己的设备信息，
        然后读取被连接的一方设备信息。
         */
        Log.d(TAG, "RootContainer:1 $it")
        thread {
            val otherSocket = Socket(it, PortConfig.DEVICE_PORT)
            val customSocket = CustomSocket(otherSocket)
            socketList.add(customSocket)
            Log.d(TAG, "RootContainer:2 $it")

            Log.d(TAG, "RootContainer: 已连接")
            val outputStream = otherSocket.getOutputStream()
            val inputStream = otherSocket.getInputStream()


            val objectOutputStream = ObjectOutputStream(outputStream)
            val objectInputStream = ObjectInputStream(inputStream)

            val localDevice = createDevice(mContext)
            objectOutputStream.writeObject(localDevice)
            objectOutputStream.flush()

            val remoteDevice = objectInputStream.readObject() as Device
            devicesList.add(remoteDevice)

            receiverListener(
                otherSocket.inetAddress.hostAddress!!,
                objectOutputStream,
                objectInputStream,
                customSocket
            )

        }
    }


    suspend fun asyncStartKernel() {
        //异步启动核心程序
        val targetJarFileName = "transfer.jar"
        FileUtils.copyAssetsFileToAdbPath(mContext, "finish.dex", targetJarFileName)

        val path = mContext.filesDir.absolutePath + "/" + targetJarFileName

        val terminal = "CLASSPATH=$path app_process / com.genymobile.transfer.Server "

        Log.d(TAG, "onCreate: terminal=$terminal")

        //杀死上次的服务
        val str = RunProcess.runProcess("su -c ss -plnt | grep :${PortConfig.MAIN_PORT}")
        fun extractPidFromOutput(output: String): Int? {
            val pattern = Pattern.compile("(?<=pid=)(\\d+)")
            val matcher = pattern.matcher(output)
            return if (matcher.find()) {
                matcher.group(1)?.toIntOrNull()
            } else {
                null
            }
        }

        val killResult = RunProcess.runProcess("su -c kill -9 ${extractPidFromOutput(str)}")
        Log.d(TAG, "asyncStartKernel: 旧服务被杀死 $killResult")
//        delay(3000)
//        RunProcess.runProcessAsync("su -c $terminal")
    }


    suspend fun connectionService() {
        /*
        多种方案,
        如使用root注册系统服务,进行aidl通信,
        如使用localSocket(普通应用连不上,权限问题)
        本次使用socket通信
         */
//        val clazz = Class.forName("android.os.ServiceManager")
//        val method = clazz.getMethod("getService", String::class.java)
        //3000/50=60
        //会尝试等待3秒
        var count = 0
        while (count++ < 240) {
            try {
                val socket = Socket("localhost", PortConfig.MAIN_PORT)
                if (socket.isConnected) {
                    mainSocket = CustomSocket(socket)
                    kernelRunning.value = true
                    Log.d(TAG, "核心服务已经连接")
                    count = 999
                }
            } catch (e: Exception) {
                delay(50)
                Log.d(TAG, "connectionService: ")
            }
        }
//        repeat(60) {
//            val tempAny = method.invoke(
//                null,
//                "TransferService"
//            )
//            tempAny?.let { binder ->
//                transferService = ITransferInterface.Stub.asInterface(binder as IBinder)
//                kernelRunning.value = true
//                Log.d(TAG, "核心服务已经连接")
//                return@repeat
//            } ?: run {
//                delay(50)
//            }
//        }
    }


    fun createDevice(context: Activity): Device {
        val model = Build.MODEL  // 获取手机型号
        val deviceName = Build.DEVICE  // 获取设备名

        val display = context.windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val width = size.x  // 屏幕宽度（像素）
        val height = size.y  // 屏幕高度（像素）


        val dpi = context.resources.displayMetrics.densityDpi  // 屏幕 DPI

        Log.d(TAG, "createDevice: $model+$deviceName+$width+$height+$dpi")


        return Device(model, deviceName, width, height, dpi)

    }


    var appsByFirstLetter = mutableStateOf(emptyMap<Char, List<ApplicationInfo>>())

    suspend fun scannerApps() {
        withContext(Dispatchers.IO) {

            val deferredAppInfos = async(Dispatchers.IO) {
                val packageInfos = mContext.getPackageManager().getInstalledPackages(0)
                val packageManager = mContext.packageManager

                val applicationInfos = mutableListOf<ApplicationInfo>()
                packageInfos.forEach { packageInfo ->
                    if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                        val appName =
                            packageInfo.applicationInfo.loadLabel(packageManager).toString()
                        val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                        val packageName = packageInfo.packageName
//                        val compressToBase64 = appIcon.compressToBase64(64, 64)!!

//                            Log.d(TAG, "AppListContainer: $compressToBase64")
                        val appInfo =
                            ApplicationInfo(
                                appName,
                                appIcon,
                                packageName,
                                if (Character.toString(appName.get(0))
                                        .matches(Regex("[\\u4E00-\\u9FA5]+"))
                                ) {
                                    PinyinHelper.toHanyuPinyinStringArray(appName.get(0))
                                        .get(0)
                                        .get(0)
                                        .uppercaseChar()
                                } else {
                                    appName.firstOrNull()?.uppercaseChar() ?: '_'
                                }
                            )
//                            }
                        applicationInfos.add(appInfo)
                    }
                }

                // 排序和分组也在IO调度器上完成，但分组键的获取要在主线程，因为它涉及到字符串访问
                val sorted = applicationInfos.sortedBy { it.pin }
                sorted.groupBy { it.pin }
            }

            val temp = deferredAppInfos.await()
            withContext(Dispatchers.Main) {
                // 获取异步操作的结果并在主线程中更新状态
                appsByFirstLetter.value = temp

            }
        }
    }


}