package com.exa.android.letstalk.utils.helperFun

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class NotificationPermissionHandler(private val activity: FragmentActivity) {

    fun requestNotificationPermission(launcher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                return // ✅ Permission already granted, no need to request
            }

            // 🔹 Check if we should show rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                // 📌 If rationale is needed, launch permission request
                launcher.launch(permission)
            } else {
                // 🔹 Directly request permission
                launcher.launch(permission)
            }
        }
    }
}
