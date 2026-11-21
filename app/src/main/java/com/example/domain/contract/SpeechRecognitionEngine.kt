package com.example.domain.contract

import kotlinx.coroutines.flow.StateFlow

interface SpeechRecognitionEngine {
    val isListening: StateFlow<Boolean>
    val recognizedText: StateFlow<String>
    val speechError: StateFlow<String?>
    val rmsLevel: StateFlow<Float>

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit = {})
    fun stopListening()
    fun feedRecognizedText(text: String)
    fun isRecognitionAvailable(): Boolean
}
