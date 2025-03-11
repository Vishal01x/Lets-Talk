package com.exa.android.letstalk

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exa.android.letstalk.data.domain.main.ViewModel.UserViewModel
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.presentation.navigation.AppNavigation
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.MainRoute
import com.exa.android.letstalk.ui.theme.LetsTalkTheme
import com.exa.android.letstalk.utils.MyLifecycleObserver
import com.exa.android.letstalk.utils.NetworkCallbackReceiver
import com.exa.android.letstalk.utils.helperFun.permissionHandling
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        val curUser = userViewModel.curUserId

        curUser?.let {
            val lifecycleObserver = MyLifecycleObserver(userViewModel, it)
            lifecycle.addObserver(lifecycleObserver)
        }
        setContent {
            LetsTalkTheme {
                UpdateStatus(this)
                App()
            }
        }
        permissionHandling(this)
    }
}

@Composable
fun UpdateStatus(context: Context) {
    val viewModel: UserViewModel = hiltViewModel()
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    DisposableEffect(Unit) {
        val callback = NetworkCallbackReceiver { connected ->
            viewModel.observeUserConnectivity()
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

@Composable
fun App() {


    val viewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn = viewModel.authStatus.collectAsState().equals(true)

    val navController = rememberNavController()
    OnBackPressed(navController = navController) // handle on back pressed like finish activity on Home
    // and back pressed else get back to home from other screen
    AppNavigation(navController, isLoggedIn) // initiate navigation
}


@Composable
fun OnBackPressed(navController: NavController) {
    // Handle back press based on the current screen
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    // Observe the current destination route
    val currentRoute = currentBackStackEntry?.destination?.route
    val context = LocalContext.current
    //Log.d("currentBackStackEntry->onBack", currentRoute.toString())

    // Listen for the back press event
    BackHandler {
        when (currentRoute) {
            MainRoute.Profile.route -> { // it helps to get rid of loops for home and profile screen
                navController.navigate(HomeRoute.ChatList.route) {
                    popUpTo(HomeRoute.ChatList.route) { inclusive = true }
                }
            }
            HomeRoute.ChatList.route -> {
                // Close the app only if we are on the Home screen
                (context as? Activity)?.finish()
            }
            AuthRoute.Login.route -> {
                // Allow default back button behavior for login screen (closing app)
                (context as? Activity)?.finish()
            }
            else -> {
                // If on other screens, navigate back normally
                navController.popBackStack()
            }
        }
    }

}