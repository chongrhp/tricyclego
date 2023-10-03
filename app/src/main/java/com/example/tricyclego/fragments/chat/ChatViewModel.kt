package com.example.tricyclego.fragments.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel:ViewModel() {

    private val _sendMsg = MutableLiveData<Boolean>()
    val sendMsg : LiveData<Boolean> get() = _sendMsg
}