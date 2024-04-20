package com.genymobile.transferclient.home.compose.file

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genymobile.transferclient.home.MainVm
import com.genymobile.transferclient.home.compose.DeviceListDialog


@Preview
@Composable
fun FilesContainer(vm: MainVm = MainVm(Activity())) {
    val TAG="FilesContainer"
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp)
    ) {
        ListHistory(vm = vm)
        ShareBtn(vm = vm, modifier = Modifier.align(Alignment.BottomCenter))

        if (vm.showTransferFileDialog.value) {

            DeviceListDialog(
                title = "选择接收设备",
                vm = vm,
                flag = vm.showTransferFileDialog,
                onDeviceClick = {
                    val customSocket = vm.socketList[it]
                    vm.showTransferFileDialog.value = false

                    Log.d("TAG", "FilesContainer: ")
                    vm.uploadFile(it)

                })
        }

    }
}
