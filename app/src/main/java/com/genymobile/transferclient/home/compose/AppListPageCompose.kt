package com.genymobile.transferclient.home.compose

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.genymobile.transferclient.R
import com.genymobile.transferclient.home.MainVm
import com.genymobile.transferclient.home.data.ApplicationInfo
import com.genymobile.transferclient.home.data.Device
import com.genymobile.transferclient.tools.Utils

private const val TAG = "AppListPageCompose"

@Composable
fun AppListContainer(context: Activity, vm: MainVm, onClick: (ApplicationInfo) -> Unit) {
    Column {
        DevicesContainer(context, vm)

        val applicationInfos = mutableListOf<ApplicationInfo>()


        var packageInfos = context.getPackageManager().getInstalledPackages(0);
        val packageManager = context.packageManager
// 创建一个可变列表来保存ApplicationInfo实体对象，并按名称排序
        packageInfos.forEach { packageInfo ->

            if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                val appName =
                    packageInfo.applicationInfo.loadLabel(packageManager).toString()
                val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                val packageName = packageInfo.packageName

                // 创建一个ApplicationInfo实例并将它添加到列表中
                val appInfo =
                    ApplicationInfo(
                        appName,
                        Utils.drawableToBase64(context, appIcon)!!,
                        packageName
                    )
                applicationInfos.add(appInfo)
            }
        }

// 对应用名按字母顺序进行排序
        val sortedAppInfos = applicationInfos.sortedBy { it.name }

// 如果需要按首字母分组，可以进一步处理sortedAppInfos，例如：
        val appsByFirstLetter: Map<Char, List<ApplicationInfo>> =
            sortedAppInfos.groupBy { it.name.firstOrNull() ?: '_' }

        ShowAddressBookView(appsByFirstLetter, onClick)
    }
}


@Composable
fun DevicesContainer(context: Activity, vm: MainVm) {
    val localDevice = vm.createDevice(context)
    Row(
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