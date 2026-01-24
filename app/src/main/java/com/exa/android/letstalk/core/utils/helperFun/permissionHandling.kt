package com.exa.android.letstalk.core.utils.helperFun


import android.Manifest
import android.os.Build
import android.util.Log
import androidx.fragment.app.FragmentActivity
//import com.permissionx.guolindev.PermissionX

// fun permissionHandling(activityContext: FragmentActivity) {
//    PermissionX.init(activityContext).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
//        .onExplainRequestReason { scope, deniedList ->
//            scope.showRequestReasonDialog(deniedList, "We need permission for calling", "Allow", "Deny")
//        }.request { _, _, _ -> }
//}


fun permissionHandling(activityContext: FragmentActivity) {
    // Request for notification permission first (required for Android 13 and above)
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        PermissionX.init(activityContext).permissions(Manifest.permission.POST_NOTIFICATIONS)
//            .onExplainRequestReason { scope, deniedList ->
//                scope.showRequestReasonDialog(deniedList, "We need permission to send notifications", "Allow", "Deny")
//            }.request { allGranted, _, _ ->
//                if (allGranted) {
//                    // If notification permission is granted, proceed with the next permission request
//                    requestSystemAlertPermission(activityContext)
//                } else {
//                    // Handle the case when permission is denied (e.g., show a message or handle the flow accordingly)
//                    Log.e("Permission", "Notification permission denied")
//                }
//            }
//    } else {
//        // If it's below Android 13, we can skip notification permission request
//        requestSystemAlertPermission(activityContext)
//    }
}

private fun requestSystemAlertPermission(activityContext: FragmentActivity) {
    // Now request for SYSTEM_ALERT_WINDOW permission
//    PermissionX.init(activityContext).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
//        .onExplainRequestReason { scope, deniedList ->
//            scope.showRequestReasonDialog(deniedList, "We need permission for calling", "Allow", "Deny")
//        }.request { _, _, _ -> }
}
