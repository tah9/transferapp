package com.genymobile.transferclient.home.data

import android.graphics.drawable.Drawable

data class ApplicationInfo(
    val name: String,
    val icon: Drawable,
    val packageName: String,
    val pin: Char
)
