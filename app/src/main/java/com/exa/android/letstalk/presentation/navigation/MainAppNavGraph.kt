package com.exa.android.letstalk.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
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

fun NavGraphBuilder.mainAppNavGraph(navController: NavHostController) {

    navigation(startDestination = "home", route = "main_app") {
        homeNavGraph(navController)

        scheduledMessagesNavGraph(navController)

        composable(MainRoute.Setting.route) {
            StatusScreen(navController)
        }

        priorityNavGraph(navController)

        profileNavGraph(navController)
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
