package com.exa.android.letstalk.presentation.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.exa.android.letstalk.presentation.Main.Home.ViewModel.ChatViewModel
import com.exa.android.letstalk.presentation.Main.Home.newChat.AllUserScreen
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.DetailChatScreen
import com.exa.android.letstalk.presentation.Main.Home.HomeScreen
import com.exa.android.letstalk.presentation.Main.Home.SearchScreen
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.ScreenPurpose
import com.exa.android.letstalk.domain.Chat
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
            val imageUrl = backStackEntry.arguments?.getString("imageId") ?: ""

            ZoomPhoto(imageUrl = imageUrl) {
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
        
        composable("scheduled_messages") {
             com.exa.android.letstalk.presentation.Main.scheduledMessages.ScheduledMessagesScreen(
                 onEditClick = { /* Handle edit if needed, or handled internally */ }
             )
        }
    }
}



