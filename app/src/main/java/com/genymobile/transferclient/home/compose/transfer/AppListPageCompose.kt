package com.genymobile.transferclient.home.compose.transfer

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.genymobile.transferclient.tools.Utils.compressToBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.sourceforge.pinyin4j.PinyinHelper

private const val TAG = "AppListPageCompose"

@Composable
fun AppListContainer(context: Activity, vm: MainVm, onClick: (ApplicationInfo) -> Unit) {
    Column {

        var appsByFirstLetter by remember { mutableStateOf(emptyMap<Char, List<ApplicationInfo>>()) }

        LaunchedEffect(Unit) {
            coroutineScope {
                val deferredAppInfos = async(Dispatchers.IO) {
                    val packageInfos = context.getPackageManager().getInstalledPackages(0)
                    val packageManager = context.packageManager

                    val applicationInfos = mutableListOf<ApplicationInfo>()
                    packageInfos.forEach { packageInfo ->
                        if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                            val appName =
                                packageInfo.applicationInfo.loadLabel(packageManager).toString()
                            val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                            val packageName = packageInfo.packageName
                            val compressToBase64 = appIcon.compressToBase64(64, 64)!!

//                            Log.d(TAG, "AppListContainer: $compressToBase64")
                            val appInfo =
                                ApplicationInfo(
                                    appName,
                                    compressToBase64,
                                    packageName,
                                    if (Character.toString(appName.get(0))
                                            .matches(Regex("[\\u4E00-\\u9FA5]+"))
                                    ) {
                                        PinyinHelper.toHanyuPinyinStringArray(appName.get(0)).get(0)
                                            .get(0)
                                            .uppercaseChar()
                                    } else {
                                        appName.firstOrNull()?.uppercaseChar() ?: '_'
                                    }
                                )
//                            }
                            applicationInfos.add(appInfo)
                        }
                    }

                    // 排序和分组也在IO调度器上完成，但分组键的获取要在主线程，因为它涉及到字符串访问
                    val sorted = applicationInfos.sortedBy { it.pin }
                    sorted.groupBy { it.pin }
                }

                // 获取异步操作的结果并在主线程中更新状态
                val groupedApps = deferredAppInfos.await()

                appsByFirstLetter = groupedApps
            }
        }

        ShowAddressBookView(
            appsByFirstLetter,
            onClick
        )
    }

}


@Composable
fun DevicesContainer(context: Activity, vm: MainVm) {
    val localDevice = vm.createDevice(context)
    Column(
//        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(color = Color(0xfff2f6fe))
            .fillMaxWidth()
    ) {
        DeviceItem(device = localDevice)
    }
}

@Composable
fun DeviceItem(device: Device) {
    val imagePainter: Painter = painterResource(id = R.drawable.phone)

    Row(
        Modifier
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
                text = device.name, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
            Text(
                text = device.model, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }

}