package com.exa.android.letstalk.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.presentation.auth.signIn.LoginScreen
import com.exa.android.letstalk.presentation.auth.signUp.ForgetPasswordScreen
import com.exa.android.letstalk.presentation.auth.signUp.RegisterScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(startDestination = AuthRoute.Login.route, route = "auth") {
        composable(AuthRoute.Login.route) { LoginScreen(navController) }
        composable(AuthRoute.Register.route) { RegisterScreen(navController) }
        composable(AuthRoute.ForgetPassword.route) { ForgetPasswordScreen(navController) }
    }
}


