package com.genymobile.transferclient.home.compose

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genymobile.transferclient.home.data.ApplicationInfo
import com.genymobile.transferclient.tools.Utils
import kotlinx.coroutines.launch

private const val TAG = "TestLetterIndex"

@Composable
fun ShowAddressBookView(
    data: Map<Char, List<ApplicationInfo>>,
    onClick: (ApplicationInfo) -> Unit
) {
    AddressBookView(
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

                val imageBitmap: ImageBitmap? =
                    Utils.decodeBase64ToBitmap(it.icon)
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null, // 添加图片描述
                        modifier = Modifier.size(45.dp), // 设置图片大小
                        contentScale = ContentScale.Crop // 设置图片裁剪方式
                    )
                }
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
            )
        })
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> AddressBookView(
    data: Map<Char, List<T>>,
    modifier: Modifier = Modifier,
    contentBody: @Composable (item: T) -> Unit,
    contentTitle: @Composable (item: Char) -> Unit,
) {
    val charList = getCharList()
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
            /*item{
                Box(Modifier.height(80.dp)) {

                }
            }*/
            data.forEach { char, listData ->
                item {
                    Column {
//                        stickyHeader(contentType = initial) {
//                            contentTitle(initial)
//                        }
                        contentTitle(char)
                        LazyRow(
                            content = {
                                items(listData) {
                                    contentBody(it)
                                }
                            }
                        )
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
                        .size(15.dp)
//                        .background(color)
                        .clickable {
                            if (char == 35.toChar()) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            } else {
                                var index = 0
                                for (typeList in charList) {
                                    if (typeList == char) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                        break
                                    } else {
                                        val size = data[typeList]?.size ?: 0
                                        index += size + 1
                                    }
                                }
                            }

                        }
                ) {
                    Text(
                        text = char.toString(),
                        color = Color.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }

            }
        }
    }

    if (listState.isScrollInProgress) {
//            val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
//            isSelectType = layoutInfo.visibleItemsInfo.first().contentType as Char
    }
}

//获取字母列表
private fun getCharList(): List<Char> {
    val charList = mutableListOf<Char>()
    val char = 35
    charList.add(char.toChar())
    (65..90).forEach { letter ->
        charList.add(letter.toChar())
    }
    return charList
}
//获取数据


