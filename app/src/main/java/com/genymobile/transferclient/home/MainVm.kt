package com.genymobile.transferclient.home

import android.app.Activity
import android.graphics.Point
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.genymobile.transferclient.home.data.Device

class MainVm : ViewModel() {
    val mutableList = mutableStateListOf<Device>()
    var homeActiveIndex= mutableStateOf(1)

    fun addItemToList(item: Device) {
        mutableList.add(item)
    }

    fun removeItemToList(item: Device) {
        mutableList.remove(item)
    }

    fun createDevice(context: Activity): Device {
        val model = Build.MODEL  // 获取手机型号
        val deviceName = Build.DEVICE  // 获取设备名

        val display = context.windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val width = size.x  // 屏幕宽度（像素）
        val height = size.y  // 屏幕高度（像素）


        val dpi = context.resources.displayMetrics.densityDpi  // 屏幕 DPI

        Log.d(TAG, "createDevice: $model+$deviceName+$width+$height+$dpi")


        return Device(model, deviceName, width, height, dpi)

    }

    private val TAG = "MainVm"
}