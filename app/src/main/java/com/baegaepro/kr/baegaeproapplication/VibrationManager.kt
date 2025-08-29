package com.baegaepro.kr.baegaeproapplication

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

class VibrationManager(private val activity: Activity) {
    
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    fun vibrate(patternString: String, fadeInOut: Boolean) {
        try {
            val pattern = patternString.split(",").map { it.trim().toLong() }.toLongArray()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (fadeInOut && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    createFadeEffect(pattern)
                } else {
                    VibrationEffect.createWaveform(pattern, -1)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            vibrateOnce(500, fadeInOut)
        }
    }
    
    fun vibrateOnce(duration: Long, fadeInOut: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (fadeInOut && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                createFadeOnceEffect(duration)
            } else {
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.R)
    private fun createFadeEffect(pattern: LongArray): VibrationEffect {
        val amplitudes = IntArray(pattern.size) { index ->
            when {
                index % 2 == 0 -> {
                    when (index) {
                        0 -> 50
                        pattern.size - 1 -> 50
                        else -> 255
                    }
                }
                else -> 0
            }
        }
        
        return VibrationEffect.createWaveform(pattern, amplitudes, -1)
    }
    
    @RequiresApi(Build.VERSION_CODES.R)
    private fun createFadeOnceEffect(duration: Long): VibrationEffect {
        val fadeSteps = 10
        val stepDuration = duration / fadeSteps
        
        val timings = mutableListOf<Long>()
        val amplitudes = mutableListOf<Int>()
        
        for (i in 1..fadeSteps/2) {
            timings.add(stepDuration)
            amplitudes.add((255 * i / (fadeSteps/2)).coerceAtMost(255))
        }
        
        for (i in (fadeSteps/2) downTo 1) {
            timings.add(stepDuration)
            amplitudes.add((255 * i / (fadeSteps/2)).coerceAtMost(255))
        }
        
        return VibrationEffect.createWaveform(
            timings.toLongArray(),
            amplitudes.toIntArray(),
            -1
        )
    }
    
    fun stop() {
        vibrator.cancel()
    }
}
