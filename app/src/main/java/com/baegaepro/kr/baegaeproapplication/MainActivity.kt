package com.baegaepro.kr.baegaeproapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val intent = Intent(this, WebViewActivity::class.java)
        startActivity(intent)
        finish()
    }
}
