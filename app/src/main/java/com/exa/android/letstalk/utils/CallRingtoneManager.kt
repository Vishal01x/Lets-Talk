package com.exa.android.letstalk.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages ringtone playback for incoming calls
 * Handles looping ringtone and vibration
 */
@Singleton
class CallRingtoneManager @Inject constructor(
    private val context: Context
) {
    private val TAG = "CallRingtoneManager"
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false
    
    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Start playing ringtone in loop
     */
    fun startRingtone() {
        if (isPlaying) {
            Log.d(TAG, "Ringtone already playing")
            return
        }
        
        try {
            // Get default ringtone URI
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            
            startVibration()
            isPlaying = true
            Log.d(TAG, "Ringtone started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ringtone", e)
        }
    }
    
    /**
     * Stop ringtone playback
     */
    fun stopRingtone() {
        if (!isPlaying) {
            return
        }
        
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            
            stopVibration()
            isPlaying = false
            Log.d(TAG, "Ringtone stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop ringtone", e)
        }
    }
    
    /**
     * Start vibration pattern
     */
    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 1000, 1000) // vibrate 1s, pause 1s, repeat
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0) // 0 means repeat
                )
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 1000, 1000)
                vibrator?.vibrate(pattern, 0)
            }
            Log.d(TAG, "Vibration started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration", e)
        }
    }
    
    /**
     * Stop vibration
     */
    private fun stopVibration() {
        try {
            vibrator?.cancel()
            Log.d(TAG, "Vibration stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop vibration", e)
        }
    }
    
    /**
     * Check if ringtone is currently playing
     */
    fun isRingtonePlaying(): Boolean = isPlaying
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopRingtone()
    }
}
