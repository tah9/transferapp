package com.genymobile.transferclient.home

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.genymobile.transferclient.home.compose.transfer.AppListContainer
import com.genymobile.transferclient.home.compose.transfer.DevicesContainer
import com.genymobile.transferclient.home.compose.connection.TabView
import com.genymobile.transferclient.home.compose.connection.ConnectionContainer


class ActivityHome : ComponentActivity() {
    private val TAG = "ActivityHome"
    private val context = this

    val vm by lazy { MainVm(context) }


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
                            ConnectionContainer(vm)
                        } else if (vm.homeActiveIndex.value == 1) {
                            var showDialog by remember { mutableStateOf(false) }
                            AppListContainer(context, vm, onClick = {
//                                Log.e("test", "ShowAddressBookView:${it.name} ")
                                showDialog = true

//                                //当点击的时候会将
//                                val packageManager = context.packageManager
//                                val intent =
//                                    packageManager.getLaunchIntentForPackage(it.packageName)
//                                intent?.let {
//                                    context.startActivity(it)
//                                }

                            })
                            if (showDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDialog = false },
                                    title = { Text("选择目标设备") },
                                    text = {
                                        DevicesContainer(context, vm)

//                                        val listItems = listOf("Item 1", "Item 2", "Item 3", "Item 4")
//                                        LazyColumn {
//                                            items(listItems) { item ->
//                                                Text(text = item)
//                                            }
//                                        }
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
                                Log.d(TAG, "onCreate: ${vm.homeActiveIndex}")
                            }
                        )
                    }
                }
            }
        }

    }


}