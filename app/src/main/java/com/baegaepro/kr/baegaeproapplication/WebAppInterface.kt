package com.baegaepro.kr.baegaeproapplication

import android.app.Activity
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject

class WebAppInterface(private val activity: Activity) {
    
    private val wifiManager = WiFiManager(activity)
    private val vibrationManager = VibrationManager(activity)
    
    @JavascriptInterface
    fun scanWiFi(): String {
        return try {
            val networks = wifiManager.scanNetworks()
            val jsonArray = JSONArray()
            for (network in networks) {
                val jsonObject = JSONObject()
                jsonObject.put("ssid", network.ssid)
                jsonObject.put("bssid", network.bssid)
                jsonObject.put("level", network.level)
                jsonObject.put("frequency", network.frequency)
                jsonObject.put("capabilities", network.capabilities)
                jsonArray.put(jsonObject)
            }
            jsonArray.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("error", e.message)
            }.toString()
        }
    }
    
    @JavascriptInterface
    fun connectToWiFi(ssid: String, password: String): String {
        return try {
            val result = wifiManager.connectToWiFi(ssid, password)
            JSONObject().apply {
                put("success", result)
                put("message", if (result) "연결 성공" else "연결 실패")
            }.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("error", e.message)
            }.toString()
        }
    }
    
    @JavascriptInterface
    fun connectToWiFiAsSecondary(ssid: String, password: String): String {
        return try {
            val result = wifiManager.connectAsSecondary(ssid, password)
            JSONObject().apply {
                put("success", result)
                put("message", if (result) "보조 연결 성공" else "보조 연결 실패")
            }.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("error", e.message)
            }.toString()
        }
    }
    
    @JavascriptInterface
    fun vibrate(pattern: String, fadeInOut: Boolean): String {
        return try {
            vibrationManager.vibrate(pattern, fadeInOut)
            JSONObject().apply {
                put("success", true)
                put("message", "진동 실행됨")
            }.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("error", e.message)
            }.toString()
        }
    }
    
    @JavascriptInterface
    fun vibrateOnce(duration: Long, fadeInOut: Boolean): String {
        return try {
            vibrationManager.vibrateOnce(duration, fadeInOut)
            JSONObject().apply {
                put("success", true)
                put("message", "진동 실행됨")
            }.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("error", e.message)
            }.toString()
        }
    }
    
    @JavascriptInterface
    fun stopVibration(): String {
        return try {
            vibrationManager.stop()
            JSONObject().apply {
                put("success", true)
                put("message", "진동 중지됨")
            }.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("error", e.message)
            }.toString()
        }
    }
}
