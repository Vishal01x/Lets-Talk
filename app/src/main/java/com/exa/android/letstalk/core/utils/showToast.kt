package com.exa.android.letstalk.core.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun showToast(context: Context, message: String) {
    // Ensure toast is always shown on the main thread
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    } else {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
