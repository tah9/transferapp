package com.genymobile.transferclient.home.compose.connection

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.genymobile.transferclient.home.MainVm


/*
    检测核心服务是否正在运行
 */
@Composable
fun CapturePermission(vm: MainVm) {
    var str: String? = null
    if (vm.kernelRunning.value) {
        str = "核心服务已启动"
    } else {
        str = "核心未启动"
    }
    Text(text = str, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}