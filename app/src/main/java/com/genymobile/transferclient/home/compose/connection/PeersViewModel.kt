package com.genymobile.transferclient.home.compose.connection

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.ViewModel
import com.genymobile.transferclient.home.MainVm

class PeersViewModel(val vm: MainVm) : ViewModel() {

    val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        vm.mContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }

    var mChannel: WifiP2pManager.Channel? = null
    var receiver: BroadcastReceiver? = null

    @SuppressLint("MissingPermission")
    fun initPeersScanner() {
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        mChannel = manager?.initialize(vm.mContext, vm.mContext.mainLooper, null).apply {
            receiver = WiFiDirectBroadcastReceiver(this!!, manager!!, vm)
            vm.mContext.registerReceiver(receiver, intentFilter)
        }


        //清除已连接的设备
        manager?.removeGroup(mChannel, null)
        manager?.discoverPeers(mChannel, null)
    }


}