package com.rohit.geminitest

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel : ViewModel() {

    private val messagesStateList = mutableStateListOf<Message>()

    private var generativeModel: GenerativeModel? = null

    var userInput by mutableStateOf("")

    private val _messages = MutableStateFlow(messagesStateList)
    val messages = _messages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        mutableStateListOf()
    )

    private var senderMsg: Message? = null

    init {
        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.geminiApiKey
        )
    }

    fun ask() {
        messagesStateList.add(Message(msg = userInput, timeStamp = "${System.currentTimeMillis()}"))
        senderMsg = Message(msg = "Loading...", timeStamp = "", isSender = true)
        messagesStateList.add(senderMsg!!)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                generativeModel?.generateContentStream(userInput)?.distinctUntilChanged()
                    ?.catch {
                        Log.d("TESTING", "ask: ${it.localizedMessage}")
                    }?.collectLatest { res ->
                        val m = messagesStateList.find {
                            it.id == senderMsg?.id
                        }
                        val idx = messagesStateList.indexOf(m)
                        if (m != null) {
                            messagesStateList[idx] = m.copy(
                                msg = if(m.msg == "Loading...") "${res.text}" else "${m.msg}${res.text}",
                                timeStamp = "${System.currentTimeMillis()}"
                            )
                        }
                    }
            }
        }
        userInput = ""
    }

    override fun onCleared() {
        super.onCleared()
        generativeModel = null
    }
}