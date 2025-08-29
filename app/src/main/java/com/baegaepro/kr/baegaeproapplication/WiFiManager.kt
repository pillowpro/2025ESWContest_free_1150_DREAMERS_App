package com.baegaepro.kr.baegaeproapplication

import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi

data class NetworkInfo(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val frequency: Int,
    val capabilities: String
)

class WiFiManager(private val activity: Activity) {
    
    private val wifiManager = activity.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    fun scanNetworks(): List<NetworkInfo> {
        if (!wifiManager.isWifiEnabled) {
            return emptyList()
        }
        
        wifiManager.startScan()
        val results = wifiManager.scanResults
        
        return results.map { result ->
            NetworkInfo(
                ssid = result.SSID ?: "",
                bssid = result.BSSID ?: "",
                level = result.level,
                frequency = result.frequency,
                capabilities = result.capabilities ?: ""
            )
        }
    }
    
    fun connectToWiFi(ssid: String, password: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectToWiFiModern(ssid, password)
        } else {
            connectToWiFiLegacy(ssid, password)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWiFiModern(ssid: String, password: String): Boolean {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()
        
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                connectivityManager.bindProcessToNetwork(network)
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
            }
        }
        
        connectivityManager.requestNetwork(request, networkCallback)
        return true
    }
    
    @Suppress("DEPRECATION")
    private fun connectToWiFiLegacy(ssid: String, password: String): Boolean {
        val wifiConfig = WifiConfiguration().apply {
            SSID = "\"$ssid\""
            preSharedKey = "\"$password\""
        }
        
        val networkId = wifiManager.addNetwork(wifiConfig)
        if (networkId == -1) {
            return false
        }
        
        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()
        
        return true
    }
    
    @RequiresApi(Build.VERSION_CODES.Q)
    fun connectAsSecondary(ssid: String, password: String): Boolean {
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setIsAppInteractionRequired(true)
            .build()
        
        val suggestionsList = listOf(suggestion)
        val status = wifiManager.addNetworkSuggestions(suggestionsList)
        
        return status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
    }
}
