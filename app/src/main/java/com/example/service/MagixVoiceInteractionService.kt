package com.example.service

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.speech.RecognitionService

class MagixVoiceInteractionService : VoiceInteractionService() {
    override fun onReady() {
        super.onReady()
        // Assistant role connected and ready
    }

    override fun onShutdown() {
        super.onShutdown()
    }
}

class MagixVoiceSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return object : VoiceInteractionSession(this) {
            override fun onShow(args: Bundle?, showFlags: Int) {
                super.onShow(args, showFlags)
                try {
                    val engine = com.example.data.assistant.MagixAppEngine.get(application)
                    engine.sessionManager.startSession(isVoiceInvocation = true)
                } catch (_: Exception) {}
            }

            @Suppress("DEPRECATION")
            override fun onHandleAssist(
                data: Bundle?,
                structure: android.app.assist.AssistStructure?,
                content: android.app.assist.AssistContent?
            ) {
                super.onHandleAssist(data, structure, content)
                // Captured assist structure when invoked via Assistant gesture/long-press home
            }
        }
    }
}

class MagixRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        // Recognition callback hook for official Assistant registration
    }

    override fun onCancel(listener: Callback?) {}

    override fun onStopListening(listener: Callback?) {}
}
