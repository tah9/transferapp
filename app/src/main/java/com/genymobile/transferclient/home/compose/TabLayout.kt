package com.genymobile.transferclient.home.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
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
        modifier = finalModifier
    ) {
        val mo = Modifier
            .weight(1f)
            .fillMaxWidth()
            .fillMaxHeight()
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
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Text(
            text = text,
            color = if (active) Color.Black else Color.Gray,
            fontSize = if (active) 18.sp else 14.sp,
            textAlign = TextAlign.Center,
            modifier= Modifier.align(Alignment.Center)
        )
    }
}
