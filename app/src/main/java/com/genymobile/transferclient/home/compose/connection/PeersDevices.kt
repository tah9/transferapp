package com.genymobile.transferclient.home.compose.connection

import android.annotation.SuppressLint
import android.app.Activity
import android.net.wifi.p2p.WifiP2pConfig
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genymobile.transferclient.R
import com.genymobile.transferclient.home.MainVm

@SuppressLint("MissingPermission")
@Preview
@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
fun PeersDeviceList(vm: MainVm = MainVm(Activity())) {
    FlowRow(
        Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xffebeaf3))
            .padding(10.dp)
    ) {
        for (item in vm.peersDevices) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    if (item.status == 0) return@clickable
                    Log.d(TAG, "PeersDeviceList: clicked")
                    val targetConfig = WifiP2pConfig()
                    targetConfig.deviceAddress = item.deviceAddress
                    Log.d(TAG, "PeersDeviceList: ${targetConfig.deviceAddress}")
                    vm.peersViewModel.manager!!.connect(
                        vm.peersViewModel.mChannel,
                        targetConfig,
                        null
                    )
                }) {
                Box(
                    contentAlignment = Alignment.Center,
//                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.phone),
                        contentDescription = ""
                    )
                    if (item.status == 0)
                        Image(
                            painter = painterResource(id = R.drawable.connection),
                            contentDescription = ""
                        )
                }
                Text(
                    text = item.deviceName,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .width(60.dp)
                        .padding(top = 10.dp)
                )
            }
        }
    }
}
