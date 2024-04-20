package com.genymobile.transferclient.home.compose

import android.app.Activity
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.genymobile.transferclient.home.MainVm
import com.genymobile.transferclient.home.compose.transfer.DevicesContainer

@Composable
fun DeviceListDialog(
    title: String,
    vm: MainVm = MainVm(Activity()),
    flag: MutableState<Boolean>,
    onDeviceClick: (index: Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = { flag.value = false },
        title = { Text(title) },
        text = {
            DevicesContainer(vm.mContext, vm, onDeviceClick = onDeviceClick)

        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = { flag.value = false }, enabled = true) {
                Text("关闭")
            }
        },
        shape = AlertDialogDefaults.shape,
    )
}