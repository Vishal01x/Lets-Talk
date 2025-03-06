package com.exa.android.letstalk.data.domain.main.repository

import android.app.Application
import android.util.Log
import com.zegocloud.uikit.internal.ZegoUIKitLanguage
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig
import com.zegocloud.uikit.plugin.common.PluginCallbackListener
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallType
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import im.zego.zim.enums.ZIMConnectionEvent
import im.zego.zim.enums.ZIMConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class ZegoRepository @Inject constructor(private val application: Application) {

    private val _callActive = MutableStateFlow(false)
    val callActive: StateFlow<Boolean> get() = _callActive

    fun initZegoService(appID: Long, appSign: String, userID: String, userName: String) {
        // Initialize Zego service
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        callInvitationConfig.translationText = ZegoTranslationText(ZegoUIKitLanguage.ENGLISH)
        callInvitationConfig.provider =
            ZegoUIKitPrebuiltCallConfigProvider { invitationData: ZegoCallInvitationData? ->
                ZegoUIKitPrebuiltCallInvitationConfig.generateDefaultConfig(
                    invitationData
                )
            }
        ZegoUIKitPrebuiltCallService.events.errorEventsListener =
            ErrorEventsListener { errorCode: Int, message: String ->
                Timber.d("onError() called with: errorCode = [$errorCode], message = [$message]")
            }
        ZegoUIKitPrebuiltCallService.events.invitationEvents.pluginConnectListener =
            SignalPluginConnectListener { state: ZIMConnectionState, event: ZIMConnectionEvent, extendedData: JSONObject ->
                Timber.d("onSignalPluginConnectionStateChanged() called with: state = [$state], event = [$event], extendedData = [$extendedData$]")
            }
        ZegoUIKitPrebuiltCallService.init(
            application, appID, appSign, userID, userName, callInvitationConfig
        )
        ZegoUIKitPrebuiltCallService.enableFCMPush()

        ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener =
            CallEndListener { callEndReason: ZegoCallEndReason?, jsonObject: String? ->
                Log.d(
                    "CallEndListener",
                    "Call Ended with reason: $callEndReason and json: $jsonObject"
                )
            }
    }

    fun makeCall(inviteeEmails: List<String>, isVideoCall: Boolean) {
        val invitees = inviteeEmails.map { email -> ZegoUIKitUser(email, email) }
        val callType =
            if (isVideoCall) ZegoInvitationType.VIDEO_CALL else ZegoInvitationType.VOICE_CALL
        ZegoUIKitPrebuiltCallService.sendInvitation(
            invitees,
            callType,
            "",
            60,  // Timeout in seconds
            UUID.randomUUID().toString(),
            ZegoSignalingPluginNotificationConfig()

        ) { result -> Log.d("CallEndListener", "Callback received: $result") }
        Log.d("CallEndListener", "makeCall: $inviteeEmails")
    }


    fun endCall() {
        ZegoUIKitPrebuiltCallService.unInit()
        _callActive.value = false
    }
}

