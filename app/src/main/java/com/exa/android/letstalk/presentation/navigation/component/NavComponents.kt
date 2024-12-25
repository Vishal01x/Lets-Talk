package com.exa.android.letstalk.presentation.navigation.component

sealed class AuthRoute(val route : String){
    data object Login : AuthRoute("login")
    data object Register : AuthRoute("register")
    data object ForgetPassword : AuthRoute("forget_password")
}

sealed class MainRoute(val route : String){
    data object Home : MainRoute("home")
    data object Profile : MainRoute("status")
    data object Setting : MainRoute("setting")
}


sealed class HomeRoute(val route : String){
    data object ChatList : HomeRoute("chats_list")
    data object ChatDetail : HomeRoute("chat_detail/{encodedChatJson}"){
        fun createRoute(encodedChatJson : String) : String = "chat_detail/${encodedChatJson}"
    }
    data object SearchScreen : HomeRoute("search")
    data object ZoomImage : HomeRoute("zoomImage/{imageId}") {
        fun createRoute(imageId: Int): String = "zoomImage/$imageId"
    }
    data object AllUserScreen : HomeRoute("all_users/{purpose}?messageListJson={messageListJson}") {
        fun createRoute(purpose : ScreenPurpose, messageListJson: String? = null): String {
            val baseRoute = "all_users/${purpose.name}"
            return if (messageListJson != null) {
                "$baseRoute?messageListJson=$messageListJson"
            } else {
                baseRoute // No arguments passed
            }
        }
    }
}

sealed class ChatInfo(val route : String){
    data object ProfileScreen : ChatInfo("profile")
    data object ChatMedia : ChatInfo("media")
    data object ProfileImage : ChatInfo("photo")
    data object StarredMessage : ChatInfo("starred")
    data object MediaVisibility : ChatInfo("visibility")
    data object BlockUser : ChatInfo("block")
}

sealed class Call(val route : String){
    data object VoiceCall : Call("voice")
    data object VideoCall : Call("video")
}

sealed class NavigationCommand{
    data object ToMainApp : NavigationCommand()
    data object ToAuth : NavigationCommand()
}


enum class SheetState{
    SHOW,
    HIDE
}

enum class ScreenPurpose{
    NEW_CHAT,
    NEW_GROUP,
    FORWARD_MESSAGES
}

