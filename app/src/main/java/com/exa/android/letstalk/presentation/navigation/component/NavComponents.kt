package com.exa.android.letstalk.presentation.navigation.component

sealed class AuthRoute(val route : String){
    data object Login : AuthRoute("login")
    data object Register : AuthRoute("register")
    data object ForgetPassword : AuthRoute("forget_password")
    data object Welcome : AuthRoute("welcome")
    data object CreateUser : AuthRoute("create_user")
}

sealed class MainRoute(val route : String){
    data object Home : MainRoute("home")
    data object Profile : MainRoute("profile")
    data object Setting : MainRoute("setting")
    data object ScheduledMessage : MainRoute("scheduled_message")
    data object PriorityMessage : MainRoute("priority_message")
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
    data object CreateGroup : HomeRoute("create_group")
    data object PermissionScreen : HomeRoute("permission")
}

sealed class ScheduledMessageRoute(val route: String){
    data object ScheduledMessageScreen : ScheduledMessageRoute("scheduled_message_screen")
}

sealed class PriorityMessageRoute(val route: String){
    data object PriorityMessageScreen : PriorityMessageRoute("priority_message_screen")
}

sealed class ProfileRoute(val route: String) {
    data object OtherProfileScreen : ProfileRoute("profile/other_profile?userId={userId}") {
        fun createRoute(userId: String? = null): String {
            return "profile/other_profile?userId=$userId"
        }
    }

    data object CurProfileScreen : ProfileRoute("profile/my_profile")
}




sealed class ChatInfo(val route : String){
    data object ProfileScreen : ChatInfo("other_profile/{encodedUserJson}"){
        fun createRoute(encodedUserJson : String) : String = "profile/${encodedUserJson}"
    }

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

sealed class ProfileType(val name: String) {
    object MY_PROFILE : ProfileType("SELF")
    object OTHER_PROFILE : ProfileType("OTHER")
    object SIGNUP_PROFILE : ProfileType("SIGNUP")

    companion object {
        fun fromString(value: String?): ProfileType {
            return when (value) {
                MY_PROFILE.name -> MY_PROFILE
                OTHER_PROFILE.name -> OTHER_PROFILE
                else -> SIGNUP_PROFILE
            }
        }
    }
}

