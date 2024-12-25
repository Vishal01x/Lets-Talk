package com.exa.android.letstalk

import androidx.compose.runtime.mutableStateOf
import com.exa.android.letstalk.presentation.navigation.component.SheetState
import com.exa.android.letstalk.utils.models.Chat

object AppManager {

    val curBottomSheetState = mutableStateOf(SheetState.HIDE)

    val curChat = mutableStateOf<Chat?>(null)

    fun switchSheetState(){
        if(curBottomSheetState.value == SheetState.HIDE){
            curBottomSheetState.value = SheetState.SHOW
        }else{
            curBottomSheetState.value = SheetState.HIDE
        }
    }

    fun setCurChat(chat : Chat){
        curChat.value = chat
    }

}