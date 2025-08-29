package com.baegaepro.kr.baegaeproapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WebViewActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    private val PERMISSION_REQUEST_CODE = 123
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.VIBRATE
    )
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        
        webView = findViewById(R.id.webview)
        
        if (checkAllPermissions()) {
            initializeWebView()
        } else {
            showPermissionDialog()
        }
    }
    
    private fun checkAllPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다!")
            .setMessage("앱 사용을 위해 다음 권한들이 필요합니다:\n\n• WiFi 접근 및 변경\n• 위치 정보 (WiFi 스캔용)\n• 진동 제어")
            .setPositiveButton("권한 허용") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("취소") { _, _ ->
                showExitToast()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            PERMISSION_REQUEST_CODE
        )
    }
    
    private fun showExitToast() {
        Toast.makeText(this, "권한이 필요합니다! 앱을 종료합니다.", Toast.LENGTH_LONG).show()
        finish()
    }
    
    private fun initializeWebView() {
        webView.webViewClient = WebViewClient()
        
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.setGeolocationEnabled(true)
        webSettings.databaseEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
        
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        
        webView.loadUrl("https://baegaepro-service.ncloud.sbs/ ")
        
        Toast.makeText(this, "모든 권한이 승인되었습니다!", Toast.LENGTH_SHORT).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
            
            if (deniedPermissions.isEmpty()) {
                initializeWebView()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("권한이 거부되었습니다")
                    .setMessage("앱 사용을 위해 모든 권한이 필요합니다.\n\n거부된 권한:\n${deniedPermissions.joinToString("\n")}")
                    .setPositiveButton("다시 시도") { _, _ ->
                        requestPermissions()
                    }
                    .setNegativeButton("앱 종료") { _, _ ->
                        showExitToast()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
    
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
