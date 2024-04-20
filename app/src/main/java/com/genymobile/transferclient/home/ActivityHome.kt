package com.genymobile.transferclient.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.genymobile.transferclient.home.compose.DeviceListDialog
import com.genymobile.transferclient.home.compose.TabView
import com.genymobile.transferclient.home.compose.connection.ConnectionContainer
import com.genymobile.transferclient.home.compose.file.FilesContainer
import com.genymobile.transferclient.home.compose.transfer.AppListContainer
import com.genymobile.transferclient.tools.getUriInfo
import com.genymobile.transferclient.tools.requestReadWritePermissions


class ActivityHome : ComponentActivity() {
    private val TAG = "ActivityHome"
    private val context = this

    val vm by lazy { MainVm(context) }


    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MessageType.FILE) {
            // 获取返回的文件URI
            if (data?.data != null) {
                val fileUri = data.data
                vm.transferFileUri = fileUri
                vm.showTransferFileDialog.value = true
            }

            // 对于多选文件
            if (data?.clipData != null) {
                val clipData = data.clipData
                Log.d(TAG, "onActivityResult: multiple")

//                for (i in 0 until clipData!!.itemCount) {
//                    val fileUri = clipData.getItemAt(i).uri
//                    // 处理每个文件URI
//                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge() //全屏显示

        requestReadWritePermissions()


        setContent {
            MyApplicationTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {


                        if (vm.homeActiveIndex.value == 0) {
                            ConnectionContainer(vm)
                        } else if (vm.homeActiveIndex.value == 1) {
                            // todo 把逻辑整理到一个compose
                            var packageName by remember { mutableStateOf<String?>(null) }
                            AppListContainer(context, vm, onClick = {
                                packageName = it.packageName
                                vm.showTransferAppDialog.value = true
                            })
                            if (vm.showTransferAppDialog.value) {
                                DeviceListDialog(
                                    title = "选择接力设备",
                                    vm = vm,
                                    vm.showTransferAppDialog,
                                    onDeviceClick = {
                                        Log.d(TAG, "onCreate: $packageName")
                                        vm.appRelay(packageName!!, it)
                                        vm.showTransferAppDialog.value = false
                                    }
                                )
                            }
                        } else {
                            FilesContainer(vm = vm)
                        }
                        TabView(
                            vm,
                            Modifier
                                .background(color = Color.White)
                                .fillMaxWidth()
                                .height(50.dp)
                                .align(Alignment.BottomCenter),
                            onClick = {
//                                Log.d(TAG, "onCreate: ${vm.homeActiveIndex}")
                            }
                        )
                    }
                }
            }
        }

    }


}