package com.exa.android.letstalk.presentation.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exa.android.letstalk.presentation.navigation.component.CustomBottomNavigationBar
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.MainRoute
import com.exa.android.letstalk.presentation.navigation.component.SheetState
import com.exa.android.letstalk.AppManager.curBottomSheetState
import com.exa.android.letstalk.AppManager.switchSheetState
import com.exa.android.letstalk.presentation.navigation.component.PriorityMessageRoute
import com.exa.android.letstalk.presentation.navigation.component.ProfileRoute
import com.exa.android.letstalk.presentation.navigation.component.ScheduledMessageRoute
import com.exa.android.letstalk.utils.helperFun.permissionHandling

@Composable
fun AppNavigation(
    navController: NavHostController,
    isLoggedIn: Boolean,
    context: FragmentActivity
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            permissionHandling(context)
        }
    }

    Scaffold(
        bottomBar = {
            if (currentDestination == HomeRoute.ChatList.route ||
                currentDestination == ProfileRoute.CurProfileScreen.route ||
                currentDestination == ScheduledMessageRoute.ScheduledMessageScreen.route ||
                currentDestination == PriorityMessageRoute.PriorityMessageScreen.route) {
                if (curBottomSheetState.value == SheetState.HIDE) {
                    CustomBottomNavigationBar(navController)
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





