package com.genymobile.transferclient.home

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.genymobile.transfer.ITransferInterface
import com.genymobile.transferclient.IO.readFully
import com.genymobile.transferclient.MainActivity
import com.genymobile.transferclient.config.PortConfig
import com.genymobile.transferclient.home.compose.connection.PeersViewModel
import com.genymobile.transferclient.home.data.ApplicationInfo
import com.genymobile.transferclient.home.data.Device
import com.genymobile.transferclient.home.data.DownloadHistory
import com.genymobile.transferclient.home.data.DownloadHistoryDao
import com.genymobile.transferclient.home.data.DownloadHistoryDatabase
import com.genymobile.transferclient.tools.FileUtils
import com.genymobile.transferclient.tools.getUriInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.sourceforge.pinyin4j.PinyinHelper
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.text.DecimalFormat
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread


class MainVm(val mContext: Activity) : ViewModel() {
    private val scope = viewModelScope
    private val TAG = "MainVm"
    val peersViewModel = PeersViewModel(this)


    class CustomSocket(
        var socket: Socket
    ) {
        val outputStream = socket.getOutputStream()
        val inputStream = socket.getInputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val dataInputStream = DataInputStream(inputStream)
    }

    var listHistory = mutableStateListOf<DownloadHistory>()
    val peersDevices = mutableStateListOf<WifiP2pDevice>() //扫描到的设备列表

    val devicesList = mutableStateListOf<Device>()
    val socketList = mutableStateListOf<CustomSocket>()
    var homeActiveIndex = mutableStateOf(0)
    val kernelRunning = mutableStateOf(false)

    var mainSocket: CustomSocket? = null

    var showTransferFileDialog = mutableStateOf(false)
    var transferFileUri: Uri? = null
    var showTransferAppDialog = mutableStateOf(false)

    val localDevice by lazy { createDevice(mContext) }

    lateinit var database: DownloadHistoryDatabase
    val downloadHistoryDao: DownloadHistoryDao by lazy { database.downloadHistoryDao() }

    init {
        scope.launch(Dispatchers.IO) {
            scannerApps()
//            asyncStartKernel() //启动服务
            passivityConnection()
            connectionService()
        }
        scope.launch(Dispatchers.IO) {
            // 初始化数据库
            database = Room.databaseBuilder(
                mContext,
                DownloadHistoryDatabase::class.java,
                "download_history.db"
            ).build()
            val allHistories = downloadHistoryDao.getAllHistories()
            Log.d(TAG, "list size=${allHistories.size} ")
            withContext(Dispatchers.Main) {
                allHistories.forEach { item ->
                    listHistory.add(item)
//                    downloadHistoryDao.deleteById(item.id)
                }
            }
        }
        peersViewModel.initPeersScanner()
    }


    val format = DecimalFormat("#.0")

    fun uploadFile(index: Int) {

        scope.launch(Dispatchers.IO) {

            val customSocket = socketList[index]
            customSocket.dataOutputStream.writeInt(MessageType.FILE)
            val fileServerSocket = ServerSocket(0)
            //将动态通信端口发送出去
            customSocket.dataOutputStream.writeInt(fileServerSocket.localPort)
            /*
               发送文件
              0 发送文件名称
              1 发送文件大小 long
              2 循环:发送数据包大小 int
              3 循环:发送数据包
              4 循环:发送进度 float
              5 结束标识 int -1
                */
            val uriInfo = transferFileUri!!.getUriInfo(mContext)
            val newHistory = DownloadHistory(
                fileName = uriInfo.second,
                fileSize = uriInfo.first,
                downloadTime = System.currentTimeMillis(),
                downloadPath = transferFileUri.toString(),
                status = "发送中"
            )
            withContext(Dispatchers.Main) {
                listHistory.add(0, newHistory)
            }


            val transferFileUri = transferFileUri!!
            val sizeFromUri = uriInfo.first
            Log.d(TAG, "FilesContainer: sizeFromUri$sizeFromUri")

            val customFileSocket = CustomSocket(fileServerSocket.accept())

            customFileSocket.dataOutputStream.writeUTF(uriInfo.second)
            customFileSocket.dataOutputStream.writeLong(sizeFromUri)
            val inputStream = mContext.contentResolver.openInputStream(transferFileUri)
            val buffer = ByteArray(8192)
            var readBytesLength: Int
            var currentSize: Long = 0
            while (inputStream!!.read(buffer).also { readBytesLength = it } > 0) {
                customFileSocket.dataOutputStream.writeInt(readBytesLength)
                customFileSocket.dataOutputStream.write(buffer, 0, readBytesLength)
                currentSize += readBytesLength
                val progress: Float = currentSize / sizeFromUri.toFloat()
                customFileSocket.dataOutputStream.writeFloat(progress)
                scope.launch(Dispatchers.Main) {
                    listHistory[0] = listHistory.first().copy(
                        status = "发送中(${format.format(progress * 100)}%)"
                    )
                }
                Log.d("FilesContainer: ", "FilesContainer: progress=$progress")
            }
            customFileSocket.dataOutputStream.writeInt(-1)
            customFileSocket.socket.close()
            withContext(Dispatchers.Main) {
                listHistory[0] = listHistory.first().copy(
                    status = "发送完成"
                )
                withContext(Dispatchers.IO) {
                    downloadHistoryDao.insert(listHistory.first())
                }
            }
        }
    }

    private fun receiverListener(
        objectOutputStream: ObjectOutputStream,
        objectInputStream: ObjectInputStream,
        remoteSocket: CustomSocket
    ) {
        thread {
            val host = remoteSocket.socket.inetAddress.hostAddress
            val dataInputStream = remoteSocket.dataInputStream
            while (true) {
                val messageType = dataInputStream.readInt()
                Log.d(TAG, "receiverListener: messageType=$messageType")
                //接力app
                if (messageType == MessageType.APP) {
                    Log.d(TAG, "receiverListener: after type")
                    val dynamicPort = dataInputStream.readInt()
                    Log.d(TAG, "receiverListener: after port")
                    Log.d(TAG, "receiverListener: host=$host,dynamicPort=${dynamicPort}")

                    val intent = Intent(mContext, MainActivity::class.java)
                        .putExtra("host", host)
                        .putExtra("dynamicPort", dynamicPort)
                        .putExtra("type", "appRelay")
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }
                //被镜像
                /*
                这里的用户模型比较有意思,此处逻辑只是被镜像的交互app逻辑
                 */
                else if (messageType == MessageType.MIRROR) {
                    Log.d(TAG, "receiverListener: mirror")
                    //通知核心服务
                    mainSocket!!.dataOutputStream.writeInt(MessageType.MIRROR)
                    //接收分配的端口
                    val dynamicPort = mainSocket!!.dataInputStream.readInt()
                    Log.d(TAG, "receiverListener: 交互app收到端口 dynamicPort")
                    //将端口发送给远程设备
                    remoteSocket.dataOutputStream.writeInt(MessageType.MIRROR_PORT)
                    remoteSocket.dataOutputStream.writeInt(remoteSocket.dataInputStream.readInt())
                    remoteSocket.dataOutputStream.writeInt(dynamicPort)
                    Log.d(TAG, "receiverListener: 交互app发送端口 dynamicPort")
                } else if (messageType == MessageType.MIRROR_PORT) {
                    val remoteDevice = devicesList[remoteSocket.dataInputStream.readInt()]
                    val dynamicPort = remoteSocket.dataInputStream.readInt()
                    val intent = Intent(mContext, MainActivity::class.java)
                    intent.putExtra("host", remoteSocket.socket.inetAddress.hostAddress)
                    intent.putExtra("dynamicPort", dynamicPort)
                    intent.putExtra("type", "mirror")
                    intent.putExtra("width", remoteDevice.width)
                    intent.putExtra("height", remoteDevice.height)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }
                //接受文件
                else if (messageType == MessageType.FILE) {
                    Log.d(TAG, "receiverListener: file")
                    //接收动态分配的端口，后续通信都使用新通道端口
                    val dynamicPort = dataInputStream.readInt()
                    scope.launch(Dispatchers.IO) {

                        val fileCustomSocket = CustomSocket(Socket(host, dynamicPort))
                        val fileName = fileCustomSocket.dataInputStream.readUTF()
                        val fileSize = fileCustomSocket.dataInputStream.readLong()

                        // 创建文件输出流写入接收到的文件数据
                        val outputFile =
                            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath + "/" + fileName)

                        val newHistory = DownloadHistory(
                            fileName = fileName,
                            fileSize = fileSize,
                            downloadTime = System.currentTimeMillis(),
                            downloadPath = transferFileUri.toString(),
                            status = "接收中"
                        )
                        withContext(Dispatchers.Main) {
                            listHistory.add(0, newHistory)
                        }

                        val fileOutputStream = FileOutputStream(outputFile)


                        // 缓冲区
                        val buffer = ByteArray(8192)
                        var readBytesLength: Int
                        var currentSize: Long = 0

                        // 开始接收文件数据
                        while (true) {
                            readBytesLength = fileCustomSocket.dataInputStream.readInt()
                            if (readBytesLength == -1) {
                                Log.d(TAG, "receiverListener: endFile")
                                // 文件传输结束标志
                                break
                            }

                            // 读取数据块
                            fileCustomSocket.dataInputStream.readFully(buffer, 0, readBytesLength)
                            fileOutputStream.write(buffer, 0, readBytesLength)

                            currentSize += readBytesLength

                            // 可以读取进度信息，但在这个示例中我们仅接收文件，不处理进度
                            val progress: Float = fileCustomSocket.dataInputStream.readFloat()
                            scope.launch(Dispatchers.Main) {
                                listHistory[0] = listHistory.first().copy(
                                    status = "接收中(${format.format(progress * 100)}%)"
                                )
                            }


                            // 可以在这里打印或处理进度信息
                            Log.d("Receiver: ", "Progress: $progress")
                        }

                        //文件传输完毕，销毁通道
                        fileCustomSocket.socket.close()

                        withContext(Dispatchers.Main) {
                            listHistory[0] = listHistory.first().copy(
                                status = "接收完成"
                            )
                            withContext(Dispatchers.IO) {
                                downloadHistoryDao.insert(listHistory.first())
                            }
                        }
                    }
                }
            }

        }
    }

    //镜像
    /*
    此处是主动镜像交互app逻辑
     */
    fun mirrorDevice(deviceIndex: Int) {
        /*
        1 通知其他通知被镜像的设备,开始镜像屏幕
        2 交互程序接收分配的端口号
        3 交互程序通知连接的设备接收新端口
        4 交互程序将host和端口发送给它
         */
        Log.d(TAG, "mirrorDevice: ")
        thread {
            val customSocket = socketList[deviceIndex]
            customSocket.dataOutputStream.writeInt(MessageType.MIRROR)
            customSocket.dataOutputStream.writeInt(deviceIndex)
        }
    }

    //应用接力
    fun appRelay(packageName: String, index: Int) {
        /*
        1通知服务,开始应用接力
        需要发送给服务两个字符串:包名和options
        接收一个int即暴露给其他设备的socket端口
        2通知连接的设备接收新端口
        将端口发送给它
         */
        val device = devicesList[index]
        val optStr = "dpi=${device.dpi},displayRegion=0-0-${device.width}-${device.height}"
        Log.d(TAG, "appRelay: optStr=$optStr")


        thread {
            val mainDataOutputStream = mainSocket!!.dataOutputStream
            mainDataOutputStream.writeInt(MessageType.APP)
            mainDataOutputStream.writeUTF(packageName)
            mainDataOutputStream.writeUTF(optStr)
            val dynamicPort = mainSocket!!.dataInputStream.readInt()

            val dataOutputStream = socketList[index].dataOutputStream
            dataOutputStream.writeInt(MessageType.APP)
            dataOutputStream.writeInt(dynamicPort)

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
//                passivityMutex.lock()
                //每个其他的设备主动连接该设备
                val fromOtherDeviceSocket = serverSocket.accept()
                val customSocket = CustomSocket(fromOtherDeviceSocket)
//                for (itemSocket in socketList) {
//                    if (itemSocket.socket.inetAddress.hostAddress==fromOtherDeviceSocket.inetAddress.hostAddress){
//                        passivityMutex.unlock()
//                        return@thread
//                    }
//                }
                socketList.add(customSocket)
//                passivityMutex.unlock()
                Log.d(TAG, "被动连接->新设备连接")
                //
                thread {
                    val outputStream = fromOtherDeviceSocket.getOutputStream()
                    val inputStream = fromOtherDeviceSocket.getInputStream()

                    val objectInputStream = ObjectInputStream(inputStream)
                    val objectOutputStream = ObjectOutputStream(outputStream)

                    val remoteDevice: Device = objectInputStream.readObject() as Device
                    //todo 不可以连接自己

                    devicesList.add(remoteDevice)
                    objectOutputStream.writeObject(localDevice)
                    objectOutputStream.flush()

                    Log.d(TAG, "onCreate: receive $remoteDevice")

                    receiverListener(
                        objectOutputStream,
                        objectInputStream,
                        customSocket
                    )
                }
            }
        }
    }

    val mutex = ReentrantLock()
    val passivityMutex = ReentrantLock()

    //主动连接
    fun initiativeSocket(it: String) {
        /*
        连接逻辑：
        主动连接的一方发送自己的设备信息，
        然后读取被连接的一方设备信息。
         */
        Log.d(TAG, "RootContainer:initiativeSocket $it")

        thread {
            mutex.lock()
            for (itemSocket in socketList) {
                if (itemSocket.socket.inetAddress.hostAddress == it) {
                    mutex.unlock()
                    Log.d(TAG, "initiativeSocket: unlock1")
                    return@thread
                }
            }
            val customSocket = CustomSocket(Socket(it, PortConfig.DEVICE_PORT))
            socketList.add(customSocket)
            mutex.unlock()
            Log.d(TAG, "initiativeSocket: unlock2")
            Log.d(TAG, "RootContainer:2 $it")

            Log.d(TAG, "RootContainer: 已连接")
            val outputStream = customSocket.outputStream
            val inputStream = customSocket.inputStream


            val objectOutputStream = ObjectOutputStream(outputStream)
            val objectInputStream = ObjectInputStream(inputStream)

            val localDevice = createDevice(mContext)
            objectOutputStream.writeObject(localDevice)
            objectOutputStream.flush()

            val remoteDevice = objectInputStream.readObject() as Device
            scope.launch(Dispatchers.Main) {
                devicesList.add(remoteDevice)
            }

            receiverListener(
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

        val terminal = "CLASSPATH=$path app_process / com.genymobile.transfer.Server"

        Log.d(TAG, "onCreate: terminal=$terminal")

    }


    suspend fun connectionService() {
        /*
        多种方案,
        如使用root注册系统服务,进行aidl通信,
        如使用localSocket(普通应用连不上,权限问题)
        本次使用socket通信
         */
        var count = 0
        while (count++ < 60) {
            try {
                val socket = Socket("localhost", PortConfig.MAIN_PORT)
//                Log.d(TAG, "connectionService: ${socket.isConnected}")
                if (socket.isConnected) {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "connectionService: 1")
                        mainSocket = CustomSocket(socket)
                        Log.d(TAG, "connectionService: 2")
                        kernelRunning.value = true
                        Log.d(TAG, "connectionService: 3")
                    }
                    Log.d(TAG, "核心服务已经连接")
                    count = Int.MAX_VALUE
                }
            } catch (e: Exception) {
                delay(1000)
//                Log.d(TAG, "connectionService: ")
            }
        }
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
                        val appName =packageInfo.applicationInfo.loadLabel(packageManager).toString()
                        val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                        val packageName = packageInfo.packageName
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
                        applicationInfos.add(appInfo)
                    }
                }
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