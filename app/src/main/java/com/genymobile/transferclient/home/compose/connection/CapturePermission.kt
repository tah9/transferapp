package com.genymobile.transferclient.home.compose.connection

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genymobile.transferclient.R
import com.genymobile.transferclient.home.MainVm


/*
    检测核心服务是否正在运行
 */
@Preview
@Composable
fun CapturePermission(vm: MainVm = MainVm(Activity())) {
    var str: String? = null
    var colorFilter: ColorFilter? = null
    if (vm.kernelRunning.value) {
        str = "核心服务运行中"

    } else {
        str = "核心未启动"
        colorFilter = ColorFilter.tint(Color.Gray)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.kernel),
            contentDescription = "",
            colorFilter = colorFilter
        )
        Text(
            text = str,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}