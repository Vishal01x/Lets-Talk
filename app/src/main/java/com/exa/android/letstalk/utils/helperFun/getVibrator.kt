package com.exa.android.khacheri.utils.helperFun

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

fun getVibrator(context : Context) : Vibrator? {
    val vibratorManager =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        }else{
            null
        }

    return  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        vibratorManager ?. defaultVibrator
    }else{
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}