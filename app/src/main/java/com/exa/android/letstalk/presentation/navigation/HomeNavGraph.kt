package com.exa.android.letstalk.presentation.navigation

import androidx.compose.compiler.plugins.kotlin.lower.changedParamCountFromTotal
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.exa.android.letstalk.data.domain.main.ViewModel.ChatViewModel
import com.exa.android.letstalk.presentation.Main.Home.newChat.AllUserScreen
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.DetailChatScreen
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.ProfileScreen
import com.exa.android.letstalk.presentation.Main.Home.HomeScreen
import com.exa.android.letstalk.presentation.Main.Home.SearchScreen
import com.exa.android.letstalk.presentation.navigation.component.ChatInfo
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.ScreenPurpose
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.Main.Home.newChat.CreateGroupScreenUI
import com.exa.android.letstalk.presentation.Main.Home.components.ZoomPhoto
import com.exa.android.letstalk.presentation.Main.Home.newChat.NewGroupViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder

fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
    navigation(startDestination = HomeRoute.ChatList.route, route = "home") {
        composable(HomeRoute.ChatList.route) {
            val viewModel : ChatViewModel = hiltViewModel()
            val chatList = listOf("Vishal", "Kanhaiya", "Joe Tam", "Holder", "Smith Darklew")
            HomeScreen(navController, viewModel)
        }

        composable(HomeRoute.ZoomImage.route) { backStackEntry ->
            val imageId = backStackEntry.arguments?.getString("imageId")
            val resourceId = imageId?.toIntOrNull() ?: R.drawable.ic_launcher_background
            ZoomPhoto(imageId = resourceId) {
                navController.popBackStack()
            }
        }

        composable(
            HomeRoute.ChatDetail.route,
            arguments = listOf(
                navArgument("encodedChatJson"){ type = NavType.StringType }
            )
            ) {backStackEntry ->
            val chatJson = backStackEntry.arguments?.getString("encodedChatJson")
            val chat = Gson().fromJson(URLDecoder.decode(chatJson, "UTF-8"), Chat::class.java)
            DetailChatScreen(navController, chat)
        }

        composable(HomeRoute.SearchScreen.route){
            val viewModel : ChatViewModel = hiltViewModel()
            SearchScreen(navController,viewModel)
        }

        composable(
            HomeRoute.AllUserScreen.route,
            arguments = listOf(
                navArgument("purpose"){
                    type = NavType.StringType
                },
                navArgument("messageListJson"){
                type = NavType.StringType
                nullable = true // Mark it as nullable to handle "no value passed" case
                defaultValue = null
            })
            ){backStackEntry ->
            val purposeString = backStackEntry.arguments?.getString("purpose") ?: ""
            val messageListJson = backStackEntry.arguments?.getString("messageListJson")

            val parentEntry = remember { navController.getBackStackEntry("home") }
            val groupViewModel: NewGroupViewModel = hiltViewModel(parentEntry)

            val gson = Gson()
            val messageList: List<String> = if(messageListJson != null) {
                val text = object : TypeToken<List<String>>() {}.type
                gson.fromJson(messageListJson, text)
            }else{
                emptyList()
            }

            val purpose = ScreenPurpose.valueOf(purposeString)
            AllUserScreen(navController = navController, purpose = purpose, forwardMessages = messageList, groupViewModel)
        }

        composable(HomeRoute.CreateGroup.route){
            val parentEntry = remember { navController.getBackStackEntry("home") }
            val groupViewModel: NewGroupViewModel = hiltViewModel(parentEntry)
            CreateGroupScreenUI(
                groupViewModel,
                onBack = { navController.popBackStack() },
                onGroupCreated = {encodedChatJson->
                    navController.navigate(HomeRoute.ChatDetail.createRoute(encodedChatJson)){
                        popUpTo(HomeRoute.AllUserScreen.route){
                            inclusive = true
                        }
                    }

                }
            )
        }

        composable(HomeRoute.PermissionScreen.route) {

        }

        chatInfoNavGraph(navController)
    }
}

fun NavGraphBuilder.chatInfoNavGraph(navController: NavHostController) {
    navigation(startDestination = ChatInfo.ProfileScreen.route, route = "chat") {
        composable(ChatInfo.ProfileScreen.route) {
            ProfileScreen(
//                "fjidjf",
//                onMediaClick = { navController.navigate(ChatInfo.ChatMedia.route) },
//                onCallClick = { navController.navigate(Call.VoiceCall.route) },
//                onMediaVisibilityClick = { navController.navigate(ChatInfo.MediaVisibility.route) },
//                onBlockClick = { navController.navigate(ChatInfo.BlockUser.route) }
            )
        }
        /*composable(ChatInfo.ChatMedia.route) { MediaScreen() }
        composable(ChatInfo.MediaVisibility.route) { MediaVisibilityScreen() }
        composable(ChatInfo.BlockUser.route) { BlockUserScreen() }
        composable(Call.VoiceCall.route) { CallScreen() }*/
    }
}


