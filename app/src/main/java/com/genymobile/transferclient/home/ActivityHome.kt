package com.genymobile.transferclient.home

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.genymobile.transferclient.config.PortConfig
import com.genymobile.transferclient.home.compose.AppListContainer
import com.genymobile.transferclient.home.compose.DeviceList
import com.genymobile.transferclient.home.compose.FindNearDeviceButton
import com.genymobile.transferclient.home.compose.InputConnection
import com.genymobile.transferclient.home.compose.TabView
import com.genymobile.transferclient.home.data.Device
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

class ActivityHome : ComponentActivity() {
    private val TAG = "ActivityHome"
    private val context = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge() //全屏显示
        setContent {
            MyApplicationTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        if (vm.homeActiveIndex.value == 0) {
                            ConnectionContainer()
                        } else if (vm.homeActiveIndex.value == 1) {
                            AppListContainer(context, vm, onClick = {
//                                Log.e("test", "ShowAddressBookView:${it.name} ")

                                val packageManager = context.packageManager
                                val intent = packageManager.getLaunchIntentForPackage(it.packageName)
                                intent?.let {
                                    context.startActivity(it)
                                }
                            })
                        }
                        TabView(
                            vm,
                            Modifier
                                .background(color = Color.White)
                                .fillMaxWidth()
                                .height(50.dp)
                                .align(Alignment.BottomCenter),
                            onClick = {
                                Log.d(TAG, "onCreate: ${vm.homeActiveIndex}")
                            }
                        )
                    }
                }
            }
        }


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
                    vm.addItemToList(remoteDevice)
                    val localDevice = vm.createDevice(context)
                    objectOutputStream.writeObject(localDevice)
                    objectOutputStream.flush()

                    Log.d(TAG, "onCreate: receive $remoteDevice")
                }.start()
            }
        }.start()
    }

    @Composable
    fun ConnectionContainer() {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 50.dp)
        ) {
            FindNearDeviceButton(
                onClick = { /* 处理按钮点击 */ },
                buttonText = "扫描附近的设备",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            InputConnection {
                Log.d(TAG, "RootContainer:1 $it")
                Thread {
                    Log.d(TAG, "RootContainer:2 $it")
                    val otherSocket = Socket(it, PortConfig.MAIN_PORT)
                    Log.d(TAG, "RootContainer: 已连接")
                    val outputStream = otherSocket.getOutputStream()
                    val inputStream = otherSocket.getInputStream()


                    val objectOutputStream = ObjectOutputStream(outputStream)
                    val objectInputStream = ObjectInputStream(inputStream)

                    val localDevice = vm.createDevice(context)
                    objectOutputStream.writeObject(localDevice)
                    objectOutputStream.flush()

                    val remoteDevice = objectInputStream.readObject() as Device
                    vm.mutableList.add(remoteDevice)

                }.start()
            }
            DeviceList(vm)
        }
    }

    val vm = MainVm()

}