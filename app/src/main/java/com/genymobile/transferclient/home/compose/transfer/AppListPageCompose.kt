package com.genymobile.transferclient.home.compose.transfer

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genymobile.transferclient.R
import com.genymobile.transferclient.home.MainVm
import com.genymobile.transferclient.home.data.ApplicationInfo
import com.genymobile.transferclient.home.data.Device

private const val TAG = "AppListPageCompose"

@Composable
fun AppListContainer(context: Activity, vm: MainVm, onClick: (ApplicationInfo) -> Unit) {
    Column {

        ShowAddressBookView(
            vm.appsByFirstLetter.value,
            onClick
        )
    }

}


@Composable
fun DevicesContainer(context: Activity, vm: MainVm, onDeviceClick: (index: Int) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .background(color = Color(0xfff2f6fe))
            .fillMaxWidth()
    ) {
        items(vm.devicesList.size) { index ->
            DeviceItem(device = vm.devicesList[index], onClick = { onDeviceClick(index) })
        }
    }
}

@Composable
fun DeviceItem(device: Device, onClick: () -> Unit) {
    val imagePainter: Painter = painterResource(id = R.drawable.phone)

    Row(
        Modifier
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(8.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "SVG Image",
            modifier = Modifier
                .size(30.dp)
                .background(/*Color(0xff495d92)*/Color.Transparent)
                .padding(vertical = 5.dp),
            contentScale = ContentScale.Fit,
//            colorFilter = ColorFilter.tint(Color.Red) // 如果需要对SVG进行着色，可以添加colorFilter
        )
        Column(
            modifier = Modifier
                .width(120.dp)
                .align(Alignment.CenterVertically)
//                .background(color = Color.White, shape = RoundedCornerShape(50.dp))
        ) {

            Text(
                text = device.model, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
            Text(
                text = device.name, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }

}