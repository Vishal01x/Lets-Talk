package com.exa.android.letstalk.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.exa.android.letstalk.presentation.auth.signIn.LoginScreen
import com.exa.android.letstalk.presentation.auth.signIn.WelcomeScreen
import com.exa.android.letstalk.presentation.auth.signUp.HandleForgetPassword
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.presentation.auth.signUp.RegisterScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(startDestination = AuthRoute.Welcome.route, route = "auth") {
        composable(AuthRoute.Welcome.route) {
            WelcomeScreen(
                SignIn = { navController.navigate(AuthRoute.Login.route) },
                CreateAccount = { navController.navigate(AuthRoute.Register.route) }
            )
        }
        composable(AuthRoute.Login.route) { LoginScreen(navController) }
        composable(AuthRoute.Register.route) { RegisterScreen(navController) }
    }
}


