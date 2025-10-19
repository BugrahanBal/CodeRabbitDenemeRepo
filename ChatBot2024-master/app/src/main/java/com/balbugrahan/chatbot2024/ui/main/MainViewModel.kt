package com.balbugrahan.chatbot2024.ui.main

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.balbugrahan.chatbot2024.R
import com.balbugrahan.chatbot2024.base.BaseViewModel
import com.balbugrahan.chatbot2024.data.model.Step
import com.balbugrahan.chatbot2024.data.repository.StepRepository
import com.balbugrahan.chatbot2024.data.repository.WebSocketRepository
import com.balbugrahan.chatbot2024.util.JsonHelper
import com.balbugrahan.chatbot2024.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val webSocketRepository: WebSocketRepository,
    private val stepRepository: StepRepository,
    private val context: Application
) : BaseViewModel(context) {

    private val steps: List<Step> = JsonHelper.loadSteps(context)
    private val _currentStep = MutableLiveData<Step>()
    val currentStep: LiveData<Step> get() = _currentStep
    private val _finishEvent = MutableLiveData<Boolean>()
    val finishEvent: LiveData<Boolean> = _finishEvent

    //Socket bağlantısı viewmodel çağrıldığında başlatılır.
    init {
        webSocketRepository.connectWebSocket()
        observeWebSocketMessages()
        loadInitialStep()
    }

    //Sayfa açılınce ilk adım yüklenir.
    private fun loadInitialStep() {
        _currentStep.value = steps.firstOrNull { it.step == "step_1" }
    }

    private fun observeWebSocketMessages() {
        webSocketRepository.messageLiveData.observeForever { response ->
            val nextStep = steps.find { it.step == response }
            nextStep?.let {
                _currentStep.value = it
                saveStepToRoom(it)
            }
        }
    }
    //Room'a kaydeder.
    private fun saveStepToRoom(step: Step) {
        viewModelScope.launch {
            stepRepository.saveStep(step)
        }
    }
    //Room'dan veri alır.
    fun getSavedStepsFromRoom() {
        viewModelScope.launch {
            stepRepository.getSavedSteps() // Room'dan veri alır.
        }
    }
    //Kullanıcı arayüzünde sockete aksiyon gönderir öncesinde internet kontrolü yapılır.
    fun sendAction(action: String) {
        if (NetworkUtil.isInternetAvailable(context)) {
            if (!webSocketRepository.isConnected()) {
                // Bağlantı yoksa yeniden bağlanmayı deniyorum. Çok kısa sürede tekrar bağlanıyor.
                webSocketRepository.reconnectWebSocket() }
            if (webSocketRepository.isConnected()) {
                webSocketRepository.sendAction(action) }
            else {
                Toast.makeText(context, context.getText(R.string.websocket_connection_failed),
                    Toast.LENGTH_SHORT).show() }
        } else {
            Toast.makeText(context, context.getText(R.string.internet_connection_failed),
                Toast.LENGTH_SHORT).show()
        }
    }
    // ViewModel'den finish tetiklemek için burayı çağrılabiliriz.
    fun onFinishRequested() {
        webSocketRepository.disconnectWebSocket()
        _finishEvent.postValue(true)
    }
}


