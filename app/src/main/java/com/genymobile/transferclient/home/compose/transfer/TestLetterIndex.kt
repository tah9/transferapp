package com.genymobile.transferclient.home.compose.transfer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genymobile.transferclient.home.data.ApplicationInfo
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch

private const val TAG = "TestLetterIndex"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppListContainer(
    data: Map<Char, List<ApplicationInfo>>,
    onClick: (ApplicationInfo) -> Unit
) {

    AppListLayout(
        data = data,
        modifier = Modifier
//            .padding(top = 80.dp/*, bottom = 50.dp*/)
            .background(Color.White),
        contentBody = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 5.dp)
                    .width(60.dp)
//                    .background(Color.Red)
//                    .height(30.dp)
                    .clickable {
                        onClick(it)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = rememberDrawablePainter(it.icon),
                    contentDescription = null, // 添加图片描述
                    modifier = Modifier.size(45.dp), // 设置图片大小
                    contentScale = ContentScale.Crop // 设置图片裁剪方式
                )
                Text(
                    text = it.name,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        contentTitle = {
            Text(
                text = it.toString(),
                color = Color.Black,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(start = 10.dp)
            )
        })
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AppListLayout(
    data: Map<Char, List<ApplicationInfo>>,
    modifier: Modifier = Modifier,
    contentBody: @Composable (item: ApplicationInfo) -> Unit,
    contentTitle: @Composable (item: Char) -> Unit,
) {
    val charList = data.keys
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    var isSelectType by remember {
        mutableStateOf(35.toChar())
    }
    Box() {
        //左侧主要内容
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
        ) {
            data.forEach { char, listData ->
                item(key = char) {
                    Column {
                        contentTitle(char)
                        FlowRow {
                            listData.forEach { item ->
                                contentBody(item)
                            }
                        }
                    }
                }
            }
            //尾部占位
            item {
                Box(Modifier.height(80.dp)) {

                }
            }
        }


        //右侧字母索引
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.CenterEnd)
                .padding(bottom = 60.dp)
//                .width(15.dp)
//                .fillMaxHeight()
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (char in charList) {
//                    val color = if (isSelectType == char) Color.Green else Color.White
                val color = Color.Green
                Box(
                    modifier = Modifier
//                        .background(color)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(charList.indexOf(char))
                            }
                        }
                ) {
                    Text(
                        text = char.toString(),
                        color = Color.Black,
                        fontSize = 15.sp,
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }
            }
        }
    }
}



