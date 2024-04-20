package com.genymobile.transferclient.home.compose.file

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.genymobile.transferclient.R
import com.genymobile.transferclient.home.MainVm
import com.genymobile.transferclient.home.MessageType


@Composable
fun ShareBtn(vm: MainVm, modifier: Modifier) {
    Box(
        modifier
            .background(Color.Transparent)
            .padding(bottom = 50.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.share),
            contentDescription = "SVG Image",
            modifier = Modifier
                .width(65.dp)
                .height(65.dp)
                .clip(RoundedCornerShape(999.dp))
                .clickable {
                    // 创建一个Intent来选择文件
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.setType("*/*")
// 设置可多选
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
// 启动Activity来选择文件
                    vm.mContext.startActivityForResult(
                        Intent.createChooser(intent, "选择文件"),
                        MessageType.FILE
                    )

                }
                .graphicsLayer {
                    // 添加触摸反馈
                    shadowElevation = 8.dp.toPx()
                }
                .background(Color(0xFF4a90e2))
                .padding(15.dp), // 将Image置于Box的底部右侧
            // 可选：添加内外边距以更好地定位
            contentScale = ContentScale.Fit,
//            colorFilter = ColorFilter.tint(Color.Red) // 如果需要对SVG进行着色，可以添加colorFilter
        )
    }
}