package com.genymobile.transferclient.home.data

import java.io.Serializable
import java.net.Socket

data class Device(
    val model: String,
    val name: String,
    val width: Int,
    val height: Int,
    val dpi: Int,
) : Serializable
