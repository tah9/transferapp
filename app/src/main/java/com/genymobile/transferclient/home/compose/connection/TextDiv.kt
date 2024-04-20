package com.genymobile.transferclient.home.compose.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextDiv(str: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .height(30.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = str,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
            )
        }
        Divider(
            modifier = Modifier
                .weight(0.9f)
                .padding(top = 2.dp)
                .background(Color.Yellow)
        )
    }
}