package com.genymobile.transferclient.home

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.genymobile.transferclient.home.compose.TabView
import com.genymobile.transferclient.home.compose.connection.ConnectionContainer
import com.genymobile.transferclient.home.compose.file.FilesContainer
import com.genymobile.transferclient.home.compose.transfer.AppListContainer
import com.genymobile.transferclient.home.compose.transfer.DevicesContainer
import com.genymobile.transferclient.home.data.DownloadHistory
import com.genymobile.transferclient.tools.ScreenUtil
import com.genymobile.transferclient.tools.requestReadWritePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date


class ActivityHome : ComponentActivity() {
    private val TAG = "ActivityHome"
    private val context = this

    val vm by lazy { MainVm(context) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MessageType.FILE) {
            // 获取返回的文件URI
            if (data?.data != null) {
                val fileUri = data.data
                // 处理文件URI，例如读取文件内容
                Log.d(TAG, "onActivityResult: single")
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

        // 示例：插入一条下载历史记录
        lifecycleScope.launch(Dispatchers.IO) {
            val newHistory = DownloadHistory(
                fileName = "example_file.mp4",
                fileSize = 1024 * 1024,
                downloadPath = "/sdcard/Download/test.mp4", // 假设这是一个有效的下载路径
                downloadTime = Date(),
                status = "成功"
            )
            vm.listHistory.add(newHistory)
            vm.listHistory.add(newHistory)
            vm.listHistory.add(newHistory)
            vm.listHistory.add(newHistory)
            vm.listHistory.add(
                DownloadHistory(
                    fileName = "example_file.pdf",
                    fileSize = 123435,
                    downloadPath = "/sdcard/Download/test.pdf", // 假设这是一个有效的下载路径
                    downloadTime = Date(),
                    status = "成功"
                )
            )
        }


        setContent {
            MyApplicationTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {


                        if (vm.homeActiveIndex.value == 0) {
                            ConnectionContainer(vm)
                        } else if (vm.homeActiveIndex.value == 1) {
                            var packageName by remember { mutableStateOf<String?>(null) }
                            var showDialog by remember { mutableStateOf(false) }
                            AppListContainer(context, vm, onClick = {
                                packageName = it.packageName
                                showDialog = true
                            })
                            if (showDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDialog = false },
                                    title = { Text("选择接力设备") },
                                    text = {
                                        DevicesContainer(context, vm) {
                                            Log.d(TAG, "onCreate: $packageName")
                                            vm.appRelay(packageName!!, it)
                                        }
                                    },
                                    confirmButton = {},
                                    dismissButton = {
                                        Button(onClick = { showDialog = false }, enabled = true) {
                                            Text("关闭")
                                        }
                                    },
                                    shape = AlertDialogDefaults.shape,
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