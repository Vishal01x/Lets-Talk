package com.exa.android.letstalk

/*
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exa.android.letstalk.data.domain.main.ViewModel.UserViewModel
import com.exa.android.letstalk.presentation.Main.profile.OtherProfileScreen
import com.exa.android.letstalk.presentation.navigation.AppNavigation
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.MainRoute
import com.exa.android.letstalk.ui.theme.LetsTalkTheme
import com.exa.android.letstalk.utils.MyLifecycleObserver
import com.exa.android.letstalk.utils.NetworkCallbackReceiver
import com.exa.android.letstalk.utils.clearAllNotifications
import com.exa.android.letstalk.utils.helperFun.permissionHandling
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        val curUser = userViewModel.curUserId

        curUser.value?.let {
            val lifecycleObserver = MyLifecycleObserver(userViewModel, it)
            lifecycle.addObserver(lifecycleObserver)
        }
        setContent {
            LetsTalkTheme(dynamicColor = false) {
//                val navController = rememberNavController()
//
//                // Get senderId from Intent (if app opened from notification)
//                val senderId = intent?.getStringExtra("senderId")
//
//                // Pass senderId to the navigation logic
//                ChatApp(navController, senderId) for directly opening chat from pending but still passing chat into it causing error
                // so firstly ensure passing chatid only and get chatroom detail from firebase then only we can open
                // for it see code
                UpdateStatus(this)
                App(this)
            }
        }

        clearAllNotifications(this)

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
fun App(context : FragmentActivity) {


//    val viewModel: AuthViewModel = hiltViewModel()
//    val isLoggedIn = viewModel.authStatus.collectAsState().equals(true)

    val viewModel: UserViewModel = hiltViewModel()
    val isLoggedIn by viewModel.curUserId.collectAsState()
    Log.d("isLoggedIn", isLoggedIn.toString())
    val navController = rememberNavController()
    //OnBackPressed(navController = navController) // handle on back pressed like finish activity on Home
    // and back pressed else get back to home from other screen
    AppNavigation(navController, isLoggedIn != null, context) // initiate navigation

}


//@Composable
//fun OnBackPressed(navController: NavController) {
//    // Handle back press based on the current screen
//    val currentBackStackEntry by navController.currentBackStackEntryAsState()
//    // Observe the current destination route
//    val currentRoute = currentBackStackEntry?.destination?.route
//    val context = LocalContext.current
//    //Log.d("currentBackStackEntry->onBack", currentRoute.toString())
//
//    // Listen for the back press event
//    BackHandler {
//        when (currentRoute) {
//            MainRoute.Profile.route -> { // it helps to get rid of loops for home and profile screen
//                navController.navigate(HomeRoute.ChatList.route) {
//                    popUpTo(HomeRoute.ChatList.route) { inclusive = true }
//                }
//            }
//            HomeRoute.ChatList.route -> {
//                // Close the app only if we are on the Home screen
//                (context as? Activity)?.finish()
//            }
//            AuthRoute.Login.route -> {
//                // Allow default back button behavior for login screen (closing app)
//                (context as? Activity)?.finish()
//            }
//            else -> {
//                // If on other screens, navigate back normally
//                navController.popBackStack()
//            }
//        }
//    }
//
//}*/
//
//
//// MainActivity.kt
//
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//import dagger.hilt.android.AndroidEntryPoint
//import dagger.hilt.android.lifecycle.HiltViewModel
//import io.getstream.video.android.core.Call
//import io.getstream.video.android.core.StreamVideo
//import io.getstream.video.android.model.User
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//    private val requiredPermissions = arrayOf(
//        Manifest.permission.CAMERA,
//        Manifest.permission.RECORD_AUDIO
//    )
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (!hasPermissions()) {
//            ActivityCompat.requestPermissions(this, requiredPermissions, 0)
//        }
//
//        val firebaseAuth: FirebaseAuth = Firebase.auth
//        val user = firebaseAuth.currentUser ?: run {
//            // Handle user not logged in
//            finish()
//            return
//        }
//
//        StreamVideo.instance().init(
//            context = applicationContext,
//            apiKey = "YOUR_STREAM_API_KEY",
//            user = User(
//                id = user.uid,
//                name = user.displayName ?: "Anonymous",
//            ),
//            token = "DEV_TOKEN_OR_PROVIDE_PROPER_AUTH" // Replace with proper auth
//        )
//
//        setContent {
//            StreamVideoTheme {
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    VideoCallApp()
//                }
//            }
//        }
//    }
//
//    private fun hasPermissions(): Boolean {
//        return requiredPermissions.all {
//            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//}
//
//@HiltViewModel
//class CallViewModel @Inject constructor() : androidx.lifecycle.ViewModel() {
//    private val streamVideo = StreamVideo.instance()
//    val callState = mutableStateOf<CallState>(CallState.Idle)
//
//    sealed class CallState {
//        object Idle : CallState()
//        object Loading : CallState()
//        data class Active(val call: Call) : CallState()
//        data class Error(val message: String) : CallState()
//    }
//
//    fun startCall(recipientId: String) {
//        viewModelScope.launch {
//            callState.value = CallState.Loading
//            try {
//                val call = streamVideo.call("default", "call_${System.currentTimeMillis()}")
//                call.create(
//                    memberRequests = listOf(recipientId.toMemberRequest()),
//                    ring = true
//                ).await()
//                callState.value = CallState.Active(call)
//            } catch (e: Exception) {
//                callState.value = CallState.Error(e.message ?: "Call failed")
//            }
//        }
//    }
//
//    fun endCall() {
//        (callState.value as? CallState.Active)?.call?.end()
//        callState.value = CallState.Idle
//    }
//
//    fun joinCall(callId: String) {
//        viewModelScope.launch {
//            callState.value = CallState.Loading
//            try {
//                val call = streamVideo.call("default", callId)
//                call.join().await()
//                callState.value = CallState.Active(call)
//            } catch (e: Exception) {
//                callState.value = CallState.Error(e.message ?: "Join failed")
//            }
//        }
//    }
//}
//
//@Composable
//fun VideoCallApp() {
//    val viewModel: CallViewModel = viewModel()
//    val currentUserId = Firebase.auth.currentUser?.uid ?: return
//    val callState by viewModel.callState
//
//    when (val state = callState) {
//        is CallViewModel.CallState.Active -> ActiveCallScreen(call = state.call, onEndCall = viewModel::endCall)
//        is CallViewModel.CallState.Error -> ErrorState(message = state.message)
//        CallViewModel.CallState.Loading -> LoadingState()
//        CallViewModel.CallState.Idle -> CallControls(viewModel = viewModel, currentUserId = currentUserId)
//    }
//}
//
//@Composable
//fun CallControls(viewModel: CallViewModel, currentUserId: String) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Button(
//            onClick = { viewModel.startCall("RECIPIENT_USER_ID") }, // Replace with actual recipient ID
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
//        ) {
//            Text("Start Video Call")
//        }
//    }
//}
//
//@Composable
//fun ActiveCallScreen(call: Call, onEndCall: () -> Unit) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        // Local participant video
//        ParticipantVideo(
//            modifier = Modifier.fillMaxSize(),
//            call = call,
//            participant = call.state.participants.firstOrNull()
//        )
//
//        // Connection status
//        ConnectionStatus(modifier = Modifier.align(Alignment.TopCenter), call = call)
//
//        // Call controls
//        FloatingActionButton(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(16.dp),
//            onClick = onEndCall,
//            containerColor = Color.Red
//        ) {
//            Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End call")
//        }
//    }
//}
//
//@Composable
//fun LoadingState() {
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        CircularProgressIndicator()
//    }
//}
//
//@Composable
//fun ErrorState(message: String) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(text = "Error: $message", color = Color.Red)
//        Button(onClick = { /* Retry logic */ }) {
//            Text("Retry")
//        }
//    }
//}
//
//// CallNotificationService.kt
//class CallNotificationService : FirebaseMessagingService() {
//    override fun onMessageReceived(message: RemoteMessage) {
//        if (message.data["type"] == "call") {
//            val callId = message.data["callId"] ?: return
//            showIncomingCallNotification(callId)
//        }
//    }
//
//    private fun showIncomingCallNotification(callId: String) {
//        // Implement notification showing logic
//        // When clicked, should launch app and join call
//    }
//}



import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.theme.StreamColors
import io.getstream.video.android.compose.theme.StreamDimens
import io.getstream.video.android.compose.theme.StreamShapes
import io.getstream.video.android.compose.theme.StreamTypography
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.theme.VideoTheme.colors
import io.getstream.video.android.compose.theme.VideoTheme.dimens
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.compose.ui.components.call.controls.ControlActions
import io.getstream.video.android.compose.ui.components.call.controls.actions.FlipCameraAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleCameraAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleMicrophoneAction
import io.getstream.video.android.compose.ui.components.call.renderer.FloatingParticipantVideo
import io.getstream.video.android.compose.ui.components.call.renderer.ParticipantVideo
import io.getstream.video.android.compose.ui.components.video.VideoRenderer
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.RealtimeConnection
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = "mmhfdzb5evj2"
        val userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Byb250by5nZXRzdHJlYW0uaW8iLCJzdWIiOiJ1c2VyL0FuYWtpbl9Tb2xvIiwidXNlcl9pZCI6IkFuYWtpbl9Tb2xvIiwidmFsaWRpdHlfaW5fc2Vjb25kcyI6NjA0ODAwLCJpYXQiOjE3NDY4MTgxNDYsImV4cCI6MTc0NzQyMjk0Nn0.3oOsh0OrOWMUA7Zm-hSjwZvcMePSN0Uo_NfULTm52i8"
        val userId = "Anakin_Solo"
        val callId = "9FVP9nsJxe5s"

        // Create a user
        val user = User(
            id = userId, // any string
            name = "Tutorial", // name and image are used in the UI
            image = "https://bit.ly/2TIt8NR",
        )

        // Initialize StreamVideo. For a production app, we recommend adding the client to your Application class or di module.
        val client = StreamVideoBuilder(
            context = applicationContext,
            apiKey = apiKey,
            geo = GEO.GlobalEdgeNetwork,
            user = user,
            token = userToken,
        ).build()

        setContent {
            // Request permissions and join a call, which type is `default` and id is `123`.
            val call = client.call(type = "default", id = callId)
            LaunchCallPermissions(
                call = call,
                onAllPermissionsGranted = {
                    // All permissions are granted so that we can join the call.
                    val result = call.join(create = true)
                    result.onError {
                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                    }
                }
            )

            VideoTheme {
                val remoteParticipants by call.state.remoteParticipants.collectAsState()
                val remoteParticipant = remoteParticipants.firstOrNull()
                val me by call.state.me.collectAsState()
                val connection by call.state.connection.collectAsState()
                var parentSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VideoTheme.colors.baseSenary)
                        .onSizeChanged { parentSize = it }
                ) {
                    if (remoteParticipant != null) {
                        ParticipantVideo(
                            modifier = Modifier.fillMaxSize(),
                            call = call,
                            participant = remoteParticipant
                        )
                    } else {
                        if (connection != RealtimeConnection.Connected) {
                            Text(
                                text = "waiting for a remote participant...",
                                fontSize = 30.sp,
                                color = VideoTheme.colors.basePrimary
                            )
                        } else {
                            Text(
                                modifier = Modifier.padding(30.dp),
                                text = "Join call ${call.id} in your browser to see the video here",
                                fontSize = 30.sp,
                                color = VideoTheme.colors.basePrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // floating video UI for the local video participant
                    me?.let { localVideo ->
                        FloatingParticipantVideo(
                            modifier = Modifier.align(Alignment.TopEnd),
                            call = call,
                            participant = localVideo,
                            parentBounds = parentSize
                        )
                    }
                }
            }
        }
    }
}