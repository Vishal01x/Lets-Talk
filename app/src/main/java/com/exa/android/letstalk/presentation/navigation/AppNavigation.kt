package com.exa.android.khacheri.screens.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exa.android.khacheri.screens.navigation.component.CustomBottomNavigationBar
import com.exa.android.khacheri.screens.navigation.component.HomeRoute
import com.exa.android.khacheri.screens.navigation.component.MainRoute
import com.exa.android.khacheri.screens.navigation.component.SheetState
import com.exa.android.letstalk.AppManager.curBottomSheetState
import com.exa.android.letstalk.AppManager.switchSheetState

@Composable
fun AppNavigation(navController: NavHostController, isLoggedIn: Boolean) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        bottomBar = {
            if(currentDestination == HomeRoute.ChatList.route || currentDestination == MainRoute.Profile.route) {
                if(curBottomSheetState.value == SheetState.HIDE) {
                    CustomBottomNavigationBar(navController) {
                        switchSheetState()
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "main_app" else "auth",
            modifier = Modifier.padding(paddingValues)
        ) {
            authNavGraph(navController)
            mainAppNavGraph(navController)
        }
    }
}





