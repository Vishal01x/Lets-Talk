package com.exa.android.letstalk.presentation.navigation

import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.exa.android.letstalk.data.repositories.main.ViewModel.ChatViewModel
import com.exa.android.letstalk.presentation.Main.SettingScreen
import com.exa.android.letstalk.presentation.Main.StatusScreen
import com.exa.android.letstalk.presentation.navigation.component.MainRoute
import com.exa.android.letstalk.presentation.navigation.homeNavGraph

fun NavGraphBuilder.mainAppNavGraph(navController: NavHostController) {

    navigation(startDestination = "home", route = "main_app") {
        homeNavGraph(navController)

        composable(MainRoute.Setting.route) {
            StatusScreen(navController)
        }
        composable(MainRoute.Profile.route) {
            val viewModel : ChatViewModel = hiltViewModel()
            Log.d("settingsScreen", "send")
            SettingScreen(viewModel)
        }
    }
}

//object MainRoutes {
//    const val MainApp = "main_app"
//    const val Profile = "profile"
//    const val Settings = "settings"
//}
