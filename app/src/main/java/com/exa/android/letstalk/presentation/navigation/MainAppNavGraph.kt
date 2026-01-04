package com.exa.android.letstalk.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.exa.android.letstalk.data.domain.call.models.CallType
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.DetailChatScreen
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
            val receiverImage = backStackEntry.arguments?.getString("receiverImage")?.takeIf { it.isNotEmpty() }
            val callTypeStr = backStackEntry.arguments?.getString("callType") ?: "VOICE"
            val callerId = backStackEntry.arguments?.getString("callerId") ?: ""
            val isOutgoing = backStackEntry.arguments?.getBoolean("isOutgoing") ?: false

            Log.d("WEBRTC_CALL", "🎬 Navigation | isOutgoing=$isOutgoing | caller=$callerId | receiver=$receiverId")

            // Get activity-scoped ViewModel (shared with MainActivity)
            val activity = LocalContext.current as? androidx.activity.ComponentActivity
            val activityCallViewModel: CallViewModel = viewModel(
                viewModelStoreOwner = activity ?: androidx.compose.ui.platform.LocalLifecycleOwner.current as androidx.lifecycle.ViewModelStoreOwner
            )

            // For outgoing calls, initiate the call directly via ViewModel
            if (isOutgoing) {
                LaunchedEffect(Unit) {
                    Log.d("WEBRTC_CALL", "✅ [CALLER] Initiating call via ViewModel")
                    activityCallViewModel.initiateCall(
                        callerId = callerId,
                        receiverId = receiverId,
                        receiverName = receiverName,
                        receiverImage = receiverImage,
                        callType = if (callTypeStr == "VIDEO") CallType.VIDEO else CallType.VOICE,
                        localRenderer = null // TODO: Create and pass renderer
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
        composable( PriorityMessageRoute.PriorityMessageScreen.route) {
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
            UserProfileScreen(null, ProfileType.MY_PROFILE)
        }
        composable(route = ProfileRoute.OtherProfileScreen.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )) {
            val userId = it.arguments?.getString("userId")
            OtherProfileScreen(userId, onCloseClick = {
                navController.popBackStack()
            })
        }
    }
}

//object MainRoutes {
//    const val MainApp = "main_app"
//    const val Profile = "profile"
//    const val Settings = "settings"
//}
