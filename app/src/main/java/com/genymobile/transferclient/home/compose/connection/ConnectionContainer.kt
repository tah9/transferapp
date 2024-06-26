package com.genymobile.transferclient.home.compose.connection

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.genymobile.transferclient.home.MainVm

@Composable
fun ConnectionContainer(vm: MainVm) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 50.dp)
    ) {
//        CapturePermission(vm)


        FindNearDeviceButton(
            vm = vm,
            onClick = {
                vm.peersViewModel.initPeersScanner()
            },
            buttonText = "扫描附近的设备",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        InputConnection {
            vm.initiativeSocket(it)
        }
        TextDiv(str = "已连接设备")
        DeviceList(vm) {
            Log.d("TAG", "DeviceList: click")
        }
    }
}