package com.baegaepro.kr.baegaeproapplication

import android.app.Activity
import android.os.Build
import android.webkit.JavascriptInterface
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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
                jsonObject.put("rssi", network.level)
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
    
    @JavascriptInterface
    fun logToConsole(level: String, message: String, source: String = ""): String {
        val logMessage = if (source.isNotEmpty()) "$message -- $source" else message
        
        return try {
            when (level.lowercase()) {
                "error", "e" -> Log.e("WebView-JS", logMessage)
                "warn", "warning", "w" -> Log.w("WebView-JS", logMessage)
                "info", "i" -> Log.i("WebView-JS", logMessage)
                "debug", "d" -> Log.d("WebView-JS", logMessage)
                else -> Log.d("WebView-JS", logMessage)
            }
            
            JSONObject().apply {
                put("success", true)
                put("message", "로그 기록됨")
            }.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("error", e.message)
            }.toString()
        }
    }
    
    @JavascriptInterface
    fun httpRequest(url: String, method: String, headers: String, body: String, callback: String): String {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                
                connection.requestMethod = method.uppercase()
                connection.doInput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                if (headers.isNotEmpty()) {
                    try {
                        val headersObj = JSONObject(headers)
                        headersObj.keys().forEach { key ->
                            connection.setRequestProperty(key, headersObj.getString(key))
                        }
                    } catch (e: Exception) {
                        Log.e("WebView-HTTP", "Invalid headers format: ${e.message}")
                    }
                }
                
                if (method.uppercase() in listOf("POST", "PUT", "PATCH") && body.isNotEmpty()) {
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json")
                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(body)
                        writer.flush()
                    }
                }
                
                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                val result = JSONObject().apply {
                    put("success", responseCode in 200..299)
                    put("status", responseCode)
                    put("data", responseBody)
                }
                
                activity.runOnUiThread {
                    (activity as? WebViewActivity)?.webView?.evaluateJavascript(
                        "if(typeof $callback === 'function') $callback(${result.toString()})",
                        null
                    )
                }
                
            } catch (e: Exception) {
                Log.e("WebView-HTTP", "HTTP Request failed: ${e.message}")
                val errorResult = JSONObject().apply {
                    put("success", false)
                    put("error", e.message)
                }
                
                activity.runOnUiThread {
                    (activity as? WebViewActivity)?.webView?.evaluateJavascript(
                        "if(typeof $callback === 'function') $callback(${errorResult.toString()})",
                        null
                    )
                }
            }
        }
        
        return JSONObject().apply {
            put("message", "HTTP request started")
        }.toString()
    }
}
