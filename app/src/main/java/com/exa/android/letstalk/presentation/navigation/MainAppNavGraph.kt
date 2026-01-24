package com.exa.android.letstalk.presentation.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.exa.android.letstalk.domain.CallType
import com.exa.android.letstalk.presentation.Main.StatusScreen
import com.exa.android.letstalk.presentation.Main.profile.OtherProfileScreen
import com.exa.android.letstalk.presentation.Main.profile.UserProfileScreen

import com.exa.android.letstalk.presentation.Main.scheduledMessages.ScheduledMessagesScreen
import com.exa.android.letstalk.presentation.navigation.component.MainRoute
import com.exa.android.letstalk.presentation.navigation.component.PriorityMessageRoute
import com.exa.android.letstalk.presentation.navigation.component.ProfileRoute
import com.exa.android.letstalk.presentation.navigation.component.ProfileType
import com.exa.android.letstalk.presentation.navigation.component.ScheduledMessageRoute
import com.exa.android.letstalk.presentation.Main.priorityMessages.PriorityMessagingScreen
import com.exa.android.letstalk.presentation.call.CallScreen
import com.exa.android.letstalk.presentation.call.CallViewModel

fun NavGraphBuilder.mainAppNavGraph(navController: NavHostController) {

    navigation(startDestination = "home", route = "main_app") {
        homeNavGraph(navController)

        scheduledMessagesNavGraph(navController)

        composable(MainRoute.Setting.route) {
            StatusScreen(navController)
        }

        priorityNavGraph(navController)

        profileNavGraph(navController)

        // Call navigation
        composable(
            route = "call/{receiverId}?name={receiverName}&image={receiverImage}&type={callType}&callerId={callerId}&isOutgoing={isOutgoing}",
            arguments = listOf(
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType },
                navArgument("receiverImage") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument("callType") { type = NavType.StringType },
                navArgument("callerId") { type = NavType.StringType },
                navArgument("isOutgoing") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
            val receiverImage =
                backStackEntry.arguments?.getString("receiverImage")?.takeIf { it.isNotEmpty() }
            val callTypeStr = backStackEntry.arguments?.getString("callType") ?: "VOICE"
            val callerId = backStackEntry.arguments?.getString("callerId") ?: ""
            val isOutgoing = backStackEntry.arguments?.getBoolean("isOutgoing") ?: false

            Log.d(
                "WEBRTC_CALL",
                "🎬 Navigation | isOutgoing=$isOutgoing | caller=$callerId | receiver=$receiverId"
            )

            // Get activity-scoped ViewModel (shared with MainActivity)
            val activity = LocalContext.current as? androidx.activity.ComponentActivity
            val activityCallViewModel: CallViewModel = viewModel(
                viewModelStoreOwner = activity
                    ?: androidx.compose.ui.platform.LocalLifecycleOwner.current as androidx.lifecycle.ViewModelStoreOwner
            )

            val context = LocalContext.current

            // For outgoing calls, create renderers and initiate the call
            if (isOutgoing) {
                val localRenderer = remember {
                    org.webrtc.SurfaceViewRenderer(context).apply {
                        init(activityCallViewModel.eglBaseContext, null)
                        setZOrderMediaOverlay(true)
                        setMirror(true)  // Mirror for selfie view
                    }
                }

                val remoteRenderer = remember {
                    org.webrtc.SurfaceViewRenderer(context).apply {
                        init(activityCallViewModel.eglBaseContext, null)
                        setMirror(false)  // Don't mirror remote video
                    }
                }

                // Clean up renderers when navigation changes
                DisposableEffect(Unit) {
                    onDispose {
                        localRenderer.release()
                        remoteRenderer.release()
                    }
                }

                LaunchedEffect(Unit) {
                    Log.d("WEBRTC_CALL", "✅ [CALLER] Initiating call via ViewModel")
                    activityCallViewModel.initiateCall(
                        callerId = callerId,
                        receiverId = receiverId,
                        receiverName = receiverName,
                        receiverImage = receiverImage,
                        callType = if (callTypeStr == "VIDEO") CallType.VIDEO else CallType.VOICE,
                        localRenderer = localRenderer,
                        remoteRenderer = remoteRenderer
                    )
                }
            } else {
                Log.d("WEBRTC_CALL", "⛔ [RECEIVER] Skipping call initiation (incoming call)")
            }


            // Show call screen with shared ViewModel
            CallScreen(
                currentUserId = callerId,
                onCallEnded = {
                    navController.popBackStack()
                },
                callViewModel = activityCallViewModel
            )
        }
    }
}

fun NavGraphBuilder.priorityNavGraph(navController: NavHostController) {
    navigation(
        startDestination = PriorityMessageRoute.PriorityMessageScreen.route,
        route = MainRoute.PriorityMessage.route
    ) {
        composable(PriorityMessageRoute.PriorityMessageScreen.route) {
            PriorityMessagingScreen(navController)
        }
    }
}

fun NavGraphBuilder.scheduledMessagesNavGraph(navController: NavHostController) {

    navigation(
        startDestination = ScheduledMessageRoute.ScheduledMessageScreen.route,
        route = MainRoute.ScheduledMessage.route
    ) {
        composable(ScheduledMessageRoute.ScheduledMessageScreen.route) {
            ScheduledMessagesScreen {

            }
        }
    }
}

fun NavGraphBuilder.profileNavGraph(navController: NavHostController) {
    navigation(
        startDestination = ProfileRoute.CurProfileScreen.route,
        route = MainRoute.Profile.route
    ) {
        composable(ProfileRoute.CurProfileScreen.route) {
            UserProfileScreen(null, ProfileType.MY_PROFILE, navController)
        }
        composable(
            route = ProfileRoute.OtherProfileScreen.route,
            arguments = listOf(
                navArgument("otherUserId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("curUserId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }

            )) {
            val otherUserId = it.arguments?.getString("otherUserId")
            val curUserId = it.arguments?.getString("curUserId")
            OtherProfileScreen(
                otherUserId, onCloseClick = {
                    navController.popBackStack()
                }, onVideoClick = { user ->
                    navController.navigate(
                        "call/${Uri.encode(otherUserId)}" +
                                "?name=${Uri.encode(user.name)}" +
                                "&image=${Uri.encode(user.profilePicture ?: "")}" +
                                "&type=VIDEO" +
                                "&callerId=${Uri.encode(curUserId)}" +
                                "&isOutgoing=true"
                    )
                },
                onVoiceClick = { user ->
                    navController.navigate(
                        "call/${Uri.encode(otherUserId)}" +
                                "?name=${Uri.encode(user.name)}" +
                                "&image=${Uri.encode(user.profilePicture ?: "")}" +
                                "&type=VOICE" +
                                "&callerId=${Uri.encode(curUserId)}" +
                                "&isOutgoing=true"
                    )
                })
        }
    }
}

//object MainRoutes {
//    const val MainApp = "main_app"
//    const val Profile = "profile"
//    const val Settings = "settings"
//}
