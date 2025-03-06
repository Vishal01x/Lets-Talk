package com.exa.android.letstalk.utils.helperFun

import android.Manifest
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

 fun permissionHandling(activityContext: FragmentActivity) {
    PermissionX.init(activityContext).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(deniedList, "We need permission for calling", "Allow", "Deny")
        }.request { _, _, _ -> }
}