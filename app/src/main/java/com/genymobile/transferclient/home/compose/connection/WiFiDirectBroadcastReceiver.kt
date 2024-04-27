package com.genymobile.transferclient.home.compose.connection

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.genymobile.transferclient.home.MainVm

class WiFiDirectBroadcastReceiver(
    private val channel: WifiP2pManager.Channel,
    private val manager: WifiP2pManager,
    private val vm: MainVm,
) : BroadcastReceiver() {

    private val TAG = "WiFiDirectBroadcastReceiver"

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel) { peerList: WifiP2pDeviceList? ->
                    vm.peersDevices.clear()
                    for (wifiP2pDevice in peerList!!.deviceList) {
                        vm.peersDevices.add(wifiP2pDevice)
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                manager.requestConnectionInfo(channel,
                    object : WifiP2pManager.ConnectionInfoListener {
                        override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
                            if (info.groupOwnerAddress == null) return
                            val host = info.groupOwnerAddress.hostAddress

                            if (!info.isGroupOwner) {
                                vm.initiativeSocket(host)
                            }
                            return
                        }
                    })
            }
        }
    }
}