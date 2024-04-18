package com.genymobile.transferclient.home.compose.connection

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genymobile.transferclient.R
import com.genymobile.transferclient.home.MainVm


@Composable
fun FindNearDeviceButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    buttonText: String
) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .padding(top = 16.dp)
            .height(20.dp)
//            .background(Color.Red)
    ) {
        Text(
            text = "附近的设备",
//            color = Color.White,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
        Divider(modifier = Modifier.weight(0.9f))
    }
    Button(
        onClick = onClick,
        modifier = modifier.padding(16.dp)
    ) {
        Text(buttonText)
    }

}

@Composable
fun InputConnection(onClick: (String) -> Unit) {
    var text by remember { mutableStateOf("192.168.43.1") }

    Row(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .background(
                color = Color(0xffe7e0ec), shape = RoundedCornerShape(16.dp)
            )
            .height(38.dp)
    ) {
        Box(
            Modifier
                .weight(1f)
                .align(CenterVertically)
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "使用IP连接",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(start = 12.dp),
            )

        }

        Button(
            onClick = { onClick(text) },
            shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            Text("连接")
        }
    }
}

@Composable
fun DeviceList(vm: MainVm, onClick: () -> Unit) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xffebeaf3))
            .padding(10.dp)

    ) {
        items(vm.devicesList.size) { index ->
            val item = vm.devicesList[index]
            val imagePainter: Painter = painterResource(id = R.drawable.phone)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(vertical = 8.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = imagePainter,
                    contentDescription = "SVG Image",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.CenterVertically)
                        .padding(end = 10.dp, start = 15.dp)
                        .background(/*Color(0xff495d92)*/Color.Transparent)
                        .padding(vertical = 5.dp),
                    contentScale = ContentScale.Fit,
//            colorFilter = ColorFilter.tint(Color.Red) // 如果需要对SVG进行着色，可以添加colorFilter
                )
                Column(
                    modifier = Modifier.align(CenterVertically),
                    verticalArrangement = Arrangement.Center,
                    content = {
                        Text(text = item.model, fontSize = 20.sp)
                        Text(
                            text = item.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Log.d(TAG, "DeviceList: " + vm.devicesList.size)
                    }
                )
            }
        }
    }
}

private const val TAG = "FormCompose"