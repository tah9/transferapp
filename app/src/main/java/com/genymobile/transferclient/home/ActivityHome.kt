package com.genymobile.transferclient.home

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.genymobile.transferclient.MainActivity
import com.genymobile.transferclient.home.compose.TabView
import com.genymobile.transferclient.home.compose.transfer.AppListContainer
import com.genymobile.transferclient.home.compose.transfer.DevicesContainer
import com.genymobile.transferclient.home.compose.connection.ConnectionContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ActivityHome : ComponentActivity() {
    private val TAG = "ActivityHome"
    private val context = this

    val vm by lazy { MainVm(context) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge() //全屏显示
        startActivity(Intent(this, MainActivity::class.java))

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