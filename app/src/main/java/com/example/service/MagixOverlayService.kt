package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.assistant.MagixAppEngine
import com.example.domain.model.AssistantState
import com.example.ui.theme.MagixTheme

/**
 * Foreground Service that displays the floating MAGIX logo / Dynamic Island pill
 * at the top of the Android screen when the app is minimized.
 *
 * Expands into the Spotify/Dynamic Island-style mini-app upon "Hey, Magix!"
 * or direct tap, responding with "Greetings Boss! How can I help you now?".
 */
class MagixOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private lateinit var appEngine: MagixAppEngine

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        appEngine = MagixAppEngine.get(application)
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_HIDE_OVERLAY -> {
                removeOverlayView()
                stopSelf()
            }
            ACTION_EXPAND_MINI_APP -> {
                showOverlayView(initiallyExpanded = true)
            }
            else -> {
                showOverlayView(initiallyExpanded = false)
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "magix_overlay_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MAGIX Live Presence",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows MAGIX floating presence and hotword listener"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MAGIX Assistant Active")
            .setContentText("Listening for 'Hey, Magix!' · Tap to open")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showOverlayView(initiallyExpanded: Boolean) {
        if (!Settings.canDrawOverlays(this)) {
            return
        }

        if (composeView != null) {
            // Already attached
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 36 // Placed elegantly beneath notch / punch-hole camera
        }

        val owner = OverlayLifecycleOwner()
        lifecycleOwner = owner
        owner.onCreate()
        owner.onStart()

        val view = ComposeView(this).apply {
            owner.attachTo(this)
            setContent {
                MagixTheme {
                    FloatingMagixIsland(
                        appEngine = appEngine,
                        initialExpanded = initiallyExpanded,
                        onCollapse = {
                            // Shrink back to logo pill
                        },
                        onOpenFullApp = {
                            val intent = Intent(this@MagixOverlayService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                        },
                        onCloseOverlay = {
                            removeOverlayView()
                            stopSelf()
                        }
                    )
                }
            }
        }

        composeView = view

        try {
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlayView() {
        composeView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeView = null
        }
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }

    override fun onDestroy() {
        removeOverlayView()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 2049
        const val ACTION_SHOW_OVERLAY = "com.example.action.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.example.action.HIDE_OVERLAY"
        const val ACTION_EXPAND_MINI_APP = "com.example.action.EXPAND_MINI_APP"

        fun start(context: Context) {
            val intent = Intent(context, MagixOverlayService::class.java).apply {
                action = ACTION_SHOW_OVERLAY
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MagixOverlayService::class.java).apply {
                action = ACTION_HIDE_OVERLAY
            }
            try {
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * Sleek Dynamic Island / Floating Mini-App for MAGIX on the system window layer.
 */
@Composable
fun FloatingMagixIsland(
    appEngine: MagixAppEngine,
    initialExpanded: Boolean = false,
    onCollapse: () -> Unit,
    onOpenFullApp: () -> Unit,
    onCloseOverlay: () -> Unit
) {
    val colors = MagixTheme.colors
    val assistantState by appEngine.assistantEngine.state.collectAsState()
    val isListening by appEngine.speechRecognitionEngine.isListening.collectAsState()
    val activeProgress by appEngine.activeTaskProgress.collectAsState()

    var isExpanded by remember { mutableStateOf(initialExpanded) }
    val currentAddress = appEngine.userSettings.value.formOfAddress.ifBlank { "Boss" }
    var greetingPrompt by remember { mutableStateOf(appEngine.greetingEngine.generateInvocationGreeting(currentAddress)) }
    var recognizedText by remember { mutableStateOf("") }

    // Pulsing animation for compact pill logo
    val infiniteTransition = rememberInfiniteTransition(label = "MagixGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoPulse"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloPulse"
    )

    // Register hotword / external invocation listener
    DisposableEffect(Unit) {
        val listener = {
            isExpanded = true
            val addr = appEngine.userSettings.value.formOfAddress.ifBlank { "Boss" }
            greetingPrompt = appEngine.greetingEngine.generateInvocationGreeting(addr)
        }
        appEngine.registerInvocationListener(listener)
        onDispose {
            appEngine.unregisterInvocationListener(listener)
        }
    }

    // When expanding via Hey Magix or tap:
    fun triggerJarvisExperience() {
        isExpanded = true
        val addr = appEngine.userSettings.value.formOfAddress.ifBlank { "Boss" }
        greetingPrompt = appEngine.greetingEngine.generateInvocationGreeting(addr)
        recognizedText = ""
        appEngine.invokeJarvisGreeting {
            // After speaking time-aware greeting, mic starts listening
        }
    }

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            (fadeIn(tween(250)) + slideInVertically(tween(250)) { -it / 2 })
                .togetherWith(fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 2 })
        },
        label = "IslandExpansion"
    ) { expanded ->
        if (!expanded) {
            // --- COMPACT LOGO PILL (Dynamic Island Pill) ---
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF080B11).copy(alpha = 0.95f))
                    .border(1.2.dp, colors.cyan.copy(alpha = haloAlpha), RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerJarvisExperience()
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Glowing MAGIX Logo Dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFE0F7FA), colors.cyan, colors.cyan.copy(alpha = 0.2f))
                                )
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "MAGIX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.cyan,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Mini Audio Waveform indicator
                    MiniAudioWaves(tint = colors.cyan, isLive = true)
                }
            }
        } else {
            // --- EXPANDED MINI-APP (Spotify / Dynamic Island style mini-app) ---
            Box(
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF080B11).copy(alpha = 0.96f))
                    .border(1.5.dp, colors.cyan.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Bar: Brand, State, Collapse & Full App buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(colors.cyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MAGIX NEURAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.cyan,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Open in full app
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141A26))
                                    .clickable { onOpenFullApp() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open Full App",
                                    tint = colors.cyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Collapse back to pill
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141A26))
                                    .clickable {
                                        isExpanded = false
                                        onCollapse()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowUp,
                                    contentDescription = "Collapse",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // JARVIS Greeting Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141A26))
                            .border(1.dp, colors.cyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = colors.cyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "JARVIS VOICE SYNTHESIS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.cyan
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = greetingPrompt,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                lineHeight = 20.sp
                            )

                            if (activeProgress != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Active: $activeProgress",
                                    fontSize = 11.sp,
                                    color = colors.purpleExec
                                )
                            }

                            if (recognizedText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"$recognizedText\"",
                                    fontSize = 12.sp,
                                    color = colors.cyanGlow,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic / Voice Trigger
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isListening) colors.cyan else Color(0xFF141A26))
                                .border(1.5.dp, colors.cyan, CircleShape)
                                .clickable {
                                    if (isListening) {
                                        appEngine.stopMicListening()
                                    } else {
                                        appEngine.triggerMicListening { spoken ->
                                            recognizedText = spoken
                                            greetingPrompt = "Executing: $spoken"
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Directive",
                                tint = if (isListening) Color(0xFF080B11) else colors.cyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Emergency Halt
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF141A26))
                                .border(1.dp, colors.redError.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    appEngine.emergencyHalt()
                                    greetingPrompt = "Emergency halt executed. System idle."
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Emergency Halt",
                                tint = colors.redError,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Action Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MiniActionChip(
                            label = "Screenshot",
                            icon = Icons.Outlined.Camera,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                greetingPrompt = "Capturing screen..."
                                appEngine.sendUserMessage("Take screenshot") { result ->
                                    greetingPrompt = "Screenshot captured successfully."
                                }
                            }
                        )
                        MiniActionChip(
                            label = "Inspect UI",
                            icon = Icons.Outlined.FindInPage,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                greetingPrompt = "Analyzing on-screen hierarchy..."
                                appEngine.sendUserMessage("Inspect current screen") { result ->
                                    greetingPrompt = "Screen analyzed."
                                }
                            }
                        )
                        MiniActionChip(
                            label = "YouTube",
                            icon = Icons.Outlined.PlayCircle,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                greetingPrompt = "Opening YouTube..."
                                appEngine.sendUserMessage("Open YouTube")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MagixTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF141A26))
            .border(1.dp, Color(0xFF222D3D), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.cyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniAudioWaves(tint: Color, isLive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Waves")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse), label = "h3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(modifier = Modifier.width(2.dp).height(h1.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.width(2.dp).height(h2.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.width(2.dp).height(h3.dp).clip(CircleShape).background(tint))
    }
}
