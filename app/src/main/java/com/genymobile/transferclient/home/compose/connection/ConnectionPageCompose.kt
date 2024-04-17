package com.genymobile.transferclient.home.compose.connection

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genymobile.transferclient.home.MainVm

@Composable
fun TabView(vm: MainVm, modifier: Modifier, onClick: () -> Unit) {
    val finalModifier = modifier.then(
        modifier.drawWithContent {
            drawContent()
            drawLine(
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                color = Color.Black,
                strokeWidth = 1f,
                cap = StrokeCap.Square,
                alpha = 0.8f
            )
        }
    )
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = CenterVertically,
        modifier = finalModifier
    ) {
        val mo= Modifier
            .weight(1f)
            .fillMaxWidth()
        TabItem(
            text = "互联",
            active = vm.homeActiveIndex.value == 0,
            onClick = {
                vm.homeActiveIndex.value = 0
                onClick()
            },
            modifier = mo
        )
        TabItem(
            text = "流转",
            active = vm.homeActiveIndex.value == 1,
            onClick = {
                vm.homeActiveIndex.value = 1
                onClick()
            },
            modifier = mo
        )
        TabItem(
            text = "互传",
            active = vm.homeActiveIndex.value == 2,
            onClick = {
                vm.homeActiveIndex.value = 2
                onClick()
            },
            modifier = mo
        )
    }
}

@Composable
fun TabItem(text: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = if (active) Color.Black else Color.Gray,
        fontSize = if (active) 18.sp else 14.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.clickable(onClick = onClick)
    )
}

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
    var text by remember { mutableStateOf("10.0.2.2") }

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
fun DeviceList(vm: MainVm) {
    LazyVerticalGrid(GridCells.Adaptive(100.dp)) {
        items(vm.mutableList.size) { index ->
            val item = vm.mutableList[index]
            // 在这里放置每个项目的内容，这里只是一个示例
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp)
                    .background(color = Color.Cyan, shape = RoundedCornerShape(16.dp))
                    .padding(8.dp),
                content = {
                    Text(text = item.name)
                    Text(
                        text = item.model,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Log.d(TAG, "DeviceList: " + vm.mutableList.size)
                }
            )
        }
    }
}

private const val TAG = "FormCompose"