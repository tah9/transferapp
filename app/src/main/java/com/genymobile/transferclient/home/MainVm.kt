package com.genymobile.transferclient.home

import android.app.Activity
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genymobile.transfer.ITransferInterface
import com.genymobile.transferclient.config.PortConfig
import com.genymobile.transferclient.home.data.Device
import com.genymobile.transferclient.tools.FileUtils
import com.genymobile.transferclient.tools.RunProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

class MainVm(val mContext: Activity) : ViewModel() {
    private val scope = viewModelScope
    private val TAG = "MainVm"

    val mutableList = mutableStateListOf<Device>()
    var homeActiveIndex = mutableStateOf(1)
    val kernelRunning = mutableStateOf(false)

    init {

        scope.launch(Dispatchers.IO) {
            asyncStartKernel()
            passivitySocket()
            connectionService()
            var currentAttempt = 0
            val maxAttempts = 5 // 最大重试次数

//            while (currentAttempt < maxAttempts) {
//                try {
//                    connectionKernel()
//                } catch (e: Exception) {
//                    Log.d(TAG, "connectionKernelFail=$e")
//                    delay(1000) // 延迟一段时间后再次尝试连接
//                    currentAttempt++
//                }
//            }
//            Log.d(TAG, "核心服务启动失败")

        }
    }

    //被动连接监听
    suspend fun passivitySocket() {
        //模拟器临时使用20001端口测试
        val serverSocket = ServerSocket(20001)
        Log.d(TAG, "onCreate: 等待其他设备连接")
        Thread {
            while (true) {
                //每个其他的设备主动连接该设备
                val fromOtherDeviceSocket = serverSocket.accept()
                Log.d(TAG, "onCreate: 已被连接")
                Thread {
                    val outputStream = fromOtherDeviceSocket.getOutputStream()
                    val inputStream = fromOtherDeviceSocket.getInputStream()

                    val dataInputStream = ObjectInputStream(inputStream)
                    val objectOutputStream = ObjectOutputStream(outputStream)

                    val remoteDevice: Device = dataInputStream.readObject() as Device
                    mutableList.add(remoteDevice)
                    val localDevice = createDevice(mContext)
                    objectOutputStream.writeObject(localDevice)
                    objectOutputStream.flush()

                    Log.d(TAG, "onCreate: receive $remoteDevice")
                }.start()
            }
        }.start()
    }

    //主动连接
    fun initiativeSocket(it: String) {
        /*
                连接逻辑：
                主动连接的一方发送自己的设备信息，
                然后读取被连接的一方设备信息。
                 */
        Log.d(TAG, "RootContainer:1 $it")
        Thread {
            Log.d(TAG, "RootContainer:2 $it")
            val otherSocket = Socket(it, PortConfig.MAIN_PORT)
            Log.d(TAG, "RootContainer: 已连接")
            val outputStream = otherSocket.getOutputStream()
            val inputStream = otherSocket.getInputStream()


            val objectOutputStream = ObjectOutputStream(outputStream)
            val objectInputStream = ObjectInputStream(inputStream)

            val localDevice = createDevice(mContext)
            objectOutputStream.writeObject(localDevice)
            objectOutputStream.flush()

            val remoteDevice = objectInputStream.readObject() as Device
            mutableList.add(remoteDevice)


        }.start()
    }

    var transferService: ITransferInterface? = null



    suspend fun asyncStartKernel() {
        //异步启动核心程序
        val targetJarFileName = "transfer.jar"
        FileUtils.copyAssetsFileToAdbPath(mContext, "finish.dex", targetJarFileName)

        val path = mContext.filesDir.absolutePath + "/" + targetJarFileName

        val terminal = "CLASSPATH=$path app_process / com.genymobile.transfer.Server "

        val suTerminal = "su -c $terminal"
        Log.d(TAG, "onCreate: suTerminal=$suTerminal")
        RunProcess.runProcess("su -c setenforce 0")//设置se宽容模式
        RunProcess.runProcessAsync(suTerminal)
    }

    suspend fun connectionService() {
        val clazz = Class.forName("android.os.ServiceManager")
        val method = clazz.getMethod("getService", String::class.java)
        val iBinder = method.invoke(
            null,
            "TransferService"
        ) as IBinder
        transferService =
            ITransferInterface.Stub.asInterface(
                iBinder
            )
        withContext(Dispatchers.Main){
            kernelRunning.value=true
        }
        Log.d(TAG, "核心服务已经连接")
//        val appRunOnTargetDisplay = transferService.appRunOnTargetDisplay()
//
//
//        val dataOutputStream = DataOutputStream(socket.getOutputStream())
//        val dataInputStream = DataInputStream(socket.getInputStream())
//        dataOutputStream.writeChar(MessageType.APP.code)
//        dataOutputStream.writeUTF("displayRegion=0-0-${}-${}")
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


}