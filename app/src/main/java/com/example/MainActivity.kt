package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MagixViewModel
import com.example.ui.automations.AutomationsScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.components.MagixBottomNav
import com.example.ui.components.MagixLivePresence
import com.example.ui.components.MagixScreen
import com.example.ui.home.HomeScreen
import com.example.ui.memory.MemoryScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MagixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val magixViewModel: MagixViewModel = viewModel()
            val userSettings by magixViewModel.userSettings.collectAsState()

            // Dynamic Theme persistence across entire app lifecycle (Dark, Light, System)
            MagixTheme(themeMode = userSettings.themeMode) {
                MagixApp(viewModel = magixViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // When active in foreground, dismiss background floating overlay
        try {
            com.example.service.MagixOverlayService.stop(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        // When minimized / in background, show floating MAGIX Dynamic Island logo if overlay permitted
        try {
            val appEngine = com.example.data.assistant.MagixAppEngine.get(application)
            if (appEngine.canDrawOverlays()) {
                com.example.service.MagixOverlayService.start(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun MagixApp(viewModel: MagixViewModel) {
    var currentScreen by rememberSaveable { mutableStateOf(MagixScreen.HOME) }
    val assistantState by viewModel.assistantState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val colors = MagixTheme.colors

    Scaffold(
        containerColor = colors.void,
        bottomBar = {
            MagixBottomNav(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.void)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Main Screen Content
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 200),
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    MagixScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    MagixScreen.CHAT -> ChatScreen(
                        viewModel = viewModel
                    )
                    MagixScreen.AUTOMATIONS -> AutomationsScreen(
                        viewModel = viewModel
                    )
                    MagixScreen.MEMORY -> MemoryScreen(
                        viewModel = viewModel
                    )
                    MagixScreen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }

            // MAGIX Live Presence (Compact / Expanded top-of-screen presence)
            val activeTaskProgress by viewModel.activeTaskProgress.collectAsState()
            val isListening by viewModel.isListening.collectAsState()
            val recognizedText by viewModel.recognizedSpeechText.collectAsState()
            val speechError by viewModel.speechError.collectAsState()
            val liveTranscript = activeTaskProgress
                ?: if (isListening && recognizedText.isNotBlank()) recognizedText
                else if (speechError != null && !isListening) speechError
                else null
            val isLivePresenceExpanded by viewModel.isLivePresenceExpanded.collectAsState()

            val micPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    viewModel.onMicPermissionGranted()
                } else {
                    viewModel.onMicPermissionDenied()
                }
            }

            val speechRecognitionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        viewModel.submitDirectiveDirectly(text)
                    }
                }
            }

            val micPermissionRequested by viewModel.micPermissionRequested.collectAsState()
            val speechIntentRequested by viewModel.speechIntentRequested.collectAsState()

            LaunchedEffect(micPermissionRequested) {
                if (micPermissionRequested) {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    viewModel.resetMicPermissionRequest()
                }
            }

            LaunchedEffect(speechIntentRequested) {
                if (speechIntentRequested) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak directive to Magix")
                    }
                    try {
                        speechRecognitionLauncher.launch(intent)
                    } catch (_: Exception) {}
                    viewModel.resetSpeechIntentRequest()
                }
            }

            val shouldShowLivePresence = userSettings.livePresenceEnabled && (isLivePresenceExpanded || assistantState != com.example.domain.model.AssistantState.IDLE)

            AnimatedVisibility(
                visible = shouldShowLivePresence,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                MagixLivePresence(
                    assistantState = assistantState,
                    onMicToggle = {
                        if (!viewModel.isMicrophonePermissionGranted) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            viewModel.triggerMicToggle()
                        }
                    },
                    onInterrupt = {
                        viewModel.emergencyHalt()
                        viewModel.setLivePresenceExpanded(false)
                    },
                    activeTranscript = liveTranscript,
                    isOverlayGranted = viewModel.appEngine.canDrawOverlays(),
                    onRequestOverlay = {
                        val intent = Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        context.startActivity(intent)
                    },
                    isExpanded = true,
                    onExpandedChange = { expanded ->
                        viewModel.setLivePresenceExpanded(expanded)
                        if (!expanded) {
                            viewModel.interruptAssistant()
                        }
                    },
                    hasMicPermission = viewModel.isMicrophonePermissionGranted,
                    onRequestMicPermission = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onLaunchSpeechIntent = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak directive to Magix")
                        }
                        try {
                            speechRecognitionLauncher.launch(intent)
                        } catch (_: Exception) {}
                    },
                    onSubmitDirective = { text ->
                        viewModel.submitDirectiveDirectly(text)
                    }
                )
            }
        }
    }
}
