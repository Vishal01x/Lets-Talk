package com.exa.android.letstalk.utils.helperFun

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionHandler(private val activity: ComponentActivity) {

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showPermissionRationale()
            }
        }

    fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            return // Permission already granted
        }
        requestPermissionLauncher.launch(permission)
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("We need permission for calling.")
            .setPositiveButton("Allow") { _, _ ->
                requestPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
            }
            .setNegativeButton("Deny", null)
            .show()
    }
}
