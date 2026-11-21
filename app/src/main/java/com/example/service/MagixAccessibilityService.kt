package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.domain.contract.ScrollDirection
import com.example.domain.model.GlobalActionType
import com.example.domain.model.UiNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MagixAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        private var instance: MagixAccessibilityService? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        fun get(): MagixAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isServiceActive.value = false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Track window changes / state for real-time awareness
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    fun executeGlobal(action: GlobalActionType): Boolean {
        val actionId = when (action) {
            GlobalActionType.HOME -> GLOBAL_ACTION_HOME
            GlobalActionType.BACK -> GLOBAL_ACTION_BACK
            GlobalActionType.RECENTS -> GLOBAL_ACTION_RECENTS
            GlobalActionType.NOTIFICATIONS -> GLOBAL_ACTION_NOTIFICATIONS
            GlobalActionType.QUICK_SETTINGS -> GLOBAL_ACTION_QUICK_SETTINGS
            GlobalActionType.TAKE_SCREENSHOT -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    GLOBAL_ACTION_TAKE_SCREENSHOT
                } else {
                    return false
                }
            }
        }
        return performGlobalAction(actionId)
    }

    fun findNodesByText(text: String): List<UiNodeInfo> {
        val root = rootInActiveWindow ?: return emptyList()
        val found = root.findAccessibilityNodeInfosByText(text) ?: return emptyList()
        return found.map { it.toUiNodeInfo() }
    }

    fun clickNodeByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val found = root.findAccessibilityNodeInfosByText(text)
        if (!found.isNullOrEmpty()) {
            for (node in found) {
                if (node.isClickable) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    parent = parent.parent
                }
            }
        }
        return false
    }

    fun enterText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) {
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }
        return false
    }

    fun dispatchTap(x: Float, y: Float): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 80)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun dispatchScroll(direction: ScrollDirection): Boolean {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()

        val startX: Float
        val startY: Float
        val endX: Float
        val endY: Float

        when (direction) {
            ScrollDirection.UP -> {
                startX = width / 2
                startY = height * 0.7f
                endX = width / 2
                endY = height * 0.3f
            }
            ScrollDirection.DOWN -> {
                startX = width / 2
                startY = height * 0.3f
                endX = width / 2
                endY = height * 0.7f
            }
            ScrollDirection.LEFT -> {
                startX = width * 0.8f
                startY = height / 2
                endX = width * 0.2f
                endY = height / 2
            }
            ScrollDirection.RIGHT -> {
                startX = width * 0.2f
                startY = height / 2
                endX = width * 0.8f
                endY = height / 2
            }
        }

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun extractHierarchySummary(): String {
        val root = rootInActiveWindow ?: return "Active screen root is inaccessible or phone is on lockscreen."
        val builder = StringBuilder()
        builder.append("Package: ").append(root.packageName ?: "Unknown").append("\n")
        builder.append("Visible Elements:\n")
        traverseHierarchy(root, builder, 0)
        return builder.toString().take(1500) // Cap to preserve token limits
    }

    private fun traverseHierarchy(node: AccessibilityNodeInfo, builder: StringBuilder, depth: Int) {
        if (depth > 6) return
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        val id = node.viewIdResourceName

        if (!text.isNullOrEmpty() || !desc.isNullOrEmpty() || node.isClickable) {
            val indent = "  ".repeat(depth)
            builder.append(indent)
            if (node.isClickable) builder.append("[Button] ")
            if (node.isEditable) builder.append("[Input] ")
            if (!text.isNullOrEmpty()) builder.append("\"").append(text).append("\" ")
            if (!desc.isNullOrEmpty()) builder.append("desc=\"").append(desc).append("\" ")
            if (!id.isNullOrEmpty()) builder.append("id=\"").append(id.substringAfterLast("/")).append("\" ")
            builder.append("\n")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseHierarchy(child, builder, depth + 1)
                @Suppress("DEPRECATION")
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    child.recycle()
                }
            }
        }
    }

    private fun AccessibilityNodeInfo.toUiNodeInfo(): UiNodeInfo {
        val rect = Rect()
        getBoundsInScreen(rect)
        return UiNodeInfo(
            className = className?.toString() ?: "",
            text = text?.toString(),
            contentDescription = contentDescription?.toString(),
            viewIdResourceName = viewIdResourceName,
            isClickable = isClickable,
            isEditable = isEditable,
            isScrollable = isScrollable,
            boundsInScreen = "${rect.left},${rect.top},${rect.right},${rect.bottom}"
        )
    }
}
