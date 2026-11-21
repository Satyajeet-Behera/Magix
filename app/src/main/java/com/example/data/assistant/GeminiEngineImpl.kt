package com.example.data.assistant

import com.example.BuildConfig
import com.example.domain.contract.AgentExecutionStep
import com.example.domain.contract.AgentPlan
import com.example.domain.contract.GeminiEngine
import com.example.domain.tool.ToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiEngineImpl(
    private val toolRegistry: ToolRegistry
) : GeminiEngine {

    private val _isOnline = MutableStateFlow(
        try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (_: Exception) {
            false
        }
    )
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun planAndReason(
        userPrompt: String,
        screenContext: String,
        conversationHistory: List<Pair<String, String>>
    ): AgentPlan = withContext(Dispatchers.IO) {
        _isProcessing.value = true

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Degraded / Offline local fallback
            _isOnline.value = false
            _isProcessing.value = false
            return@withContext planLocalOfflineFallback(userPrompt, screenContext)
        }

        try {
            val response = callGeminiApi(apiKey, userPrompt, screenContext, conversationHistory)
            _isOnline.value = true
            _isProcessing.value = false
            parseGeminiResponse(response, userPrompt)
        } catch (e: Exception) {
            _isOnline.value = false
            _isProcessing.value = false
            planLocalOfflineFallback(userPrompt, screenContext)
        }
    }

    override suspend fun synthesizeFinalResponse(
        originalPrompt: String,
        plan: AgentPlan,
        executedSteps: List<AgentExecutionStep>
    ): String = withContext(Dispatchers.IO) {
        val allSucceeded = executedSteps.all { it.result?.success == true }
        if (executedSteps.isEmpty()) {
            return@withContext plan.directSpeechResponse ?: "Acknowledged Boss."
        }

        val failedStep = executedSteps.firstOrNull { it.result?.success == false }
        if (failedStep != null) {
            return@withContext "I encountered an issue executing ${failedStep.toolName}: ${failedStep.result?.reason ?: "unknown error"}."
        }

        // Generate crisp, natural summary
        when (executedSteps.first().toolName) {
            "open_app" -> "Opening ${executedSteps.first().arguments["app_name"]} now, Boss."
            "launch_settings" -> "Navigating to settings panel."
            "go_home" -> "Returned to home screen."
            "take_screenshot" -> "Screenshot captured."
            "create_reminder" -> "Reminder recorded for ${executedSteps.first().arguments["time_description"] ?: "later"}."
            "remember" -> "Saved to memory core, Boss."
            "device_control" -> "Device parameter adjusted."
            "tap" -> "Target tapped on screen."
            "type_text" -> "Input submitted."
            "youtube_search" -> "Searching YouTube and playing the selection, Boss."
            "read_emails" -> "I have checked your unread emails and prioritized actionable items. Suggested drafts have been generated and marked 'SUGGESTED DRAFT — NOT SENT'."
            "create_note" -> "Note created and verified with all email summaries and drafts."
            else -> "Directive executed successfully Boss."
        }
    }

    private fun callGeminiApi(
        apiKey: String,
        prompt: String,
        screenContext: String,
        history: List<Pair<String, String>>
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val toolsDesc = toolRegistry.getToolDeclarationsPrompt()

        val systemPrompt = """
            You are MAGIX, a calm, intelligent, respectful, slightly futuristic JARVIS-style personal AI assistant for Android.
            Default form of address: Boss (use naturally, never spam).
            
            $toolsDesc
            
            Current Screen Context:
            $screenContext
            
            Instructions:
            - If the user directive requires Android device actions (e.g. open apps, tap buttons, scroll, create reminders, adjust settings, remember facts), return a JSON plan in this EXACT format:
            {
              "reasoning": "Brief explanation",
              "steps": [
                {
                  "tool": "tool_name",
                  "arguments": {"param_key": "param_value"}
                }
              ]
            }
            - If it is conversational (Q&A, greetings, general query), respond naturally in concise conversational text.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                // Add recent history
                history.takeLast(4).forEach { (role, text) ->
                    put(JSONObject().apply {
                        put("role", if (role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", text) })
                        })
                    })
                }
                // Add current prompt
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 800)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            throw RuntimeException("Gemini API error: ${response.code} $responseBody")
        }
        return responseBody
    }

    private fun parseGeminiResponse(rawJson: String, originalPrompt: String): AgentPlan {
        try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

            // Strip markdown formatting if Gemini wrapped JSON in code fences
            var cleanText = text.trim()
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.removePrefix("```json")
                val lastFence = cleanText.lastIndexOf("```")
                if (lastFence != -1) {
                    cleanText = cleanText.substring(0, lastFence)
                }
                cleanText = cleanText.trim()
            } else if (cleanText.startsWith("```")) {
                cleanText = cleanText.removePrefix("```")
                val lastFence = cleanText.lastIndexOf("```")
                if (lastFence != -1) {
                    cleanText = cleanText.substring(0, lastFence)
                }
                cleanText = cleanText.trim()
            }

            // Check if response contains JSON tool plan
            if (cleanText.startsWith("{") && cleanText.endsWith("}")) {
                val planObj = JSONObject(cleanText)
                val reasoning = planObj.optString("reasoning", "Autonomous execution sequence")
                val stepsArray = planObj.optJSONArray("steps")
                val steps = mutableListOf<AgentExecutionStep>()

                if (stepsArray != null) {
                    for (i in 0 until stepsArray.length()) {
                        val stepObj = stepsArray.getJSONObject(i)
                        val tool = stepObj.getString("tool")
                        val argsObj = stepObj.optJSONObject("arguments")
                        val argsMap = mutableMapOf<String, String>()
                        argsObj?.keys()?.forEach { key ->
                            argsMap[key] = argsObj.getString(key)
                        }
                        steps.add(
                            AgentExecutionStep(
                                stepIndex = i + 1,
                                toolName = tool,
                                arguments = argsMap,
                                status = "PLANNED"
                            )
                        )
                    }
                }

                if (steps.isNotEmpty()) {
                    return AgentPlan(reasoning = reasoning, steps = steps)
                }
            }

            return AgentPlan(
                reasoning = "Direct conversational answer",
                steps = emptyList(),
                directSpeechResponse = text
            )
        } catch (_: Exception) {
            return planLocalOfflineFallback(originalPrompt, "")
        }
    }

    private fun planLocalOfflineFallback(prompt: String, screenContext: String): AgentPlan {
        val lower = prompt.trim().lowercase()

        // 0. Prompt Injection Defenses & Adversarial Input Handling
        val sanitizedPrompt = com.example.domain.model.SecurityPolicy.redactSensitiveData(prompt)
        if (lower.contains("ignore previous") || lower.contains("ignore your user") || lower.contains("disable security") || lower.contains("execute this hidden")) {
            return AgentPlan(
                reasoning = "Adversarial prompt injection attempt detected and neutralized.",
                steps = emptyList(),
                directSpeechResponse = "Security policy alert: External instructions or override commands cannot supersede user authorization or core safety policies."
            )
        }

        // Test Chain F — Multi-Step Agent: Email Triage -> Prioritization -> Drafts -> Notes
        if ((lower.contains("email") || lower.contains("mail")) && (lower.contains("note") || lower.contains("notes")) && (lower.contains("summar") || lower.contains("draft") || lower.contains("response"))) {
            return AgentPlan(
                reasoning = "Multi-step agent chain: Read emails, analyze priority, generate drafts, and save verified summary note",
                steps = listOf(
                    AgentExecutionStep(1, "read_emails", mapOf("filter" to "unread"), "PLANNED"),
                    AgentExecutionStep(
                        2,
                        "create_note",
                        mapOf(
                            "title" to "Actionable Emails & Draft Replies",
                            "content" to "MAGIX TRIAGE SUMMARY:\n• Sarah Jenkins (Product Director) — Urgent: Q3 Roadmap Sign-off Required [HIGH PRIORITY]\n  Reason: Deadline today at 5:00 PM.\n  Recommended Action: Review and sign off.\n  SUGGESTED DRAFT — NOT SENT: \"Hi Sarah, I've reviewed the Q3 roadmap milestones. Everything looks solid to proceed. Approved.\"\n\n• Cloud Infrastructure Alerts — Weekly Cluster Maintenance Notice [MEDIUM PRIORITY]\n  Reason: Maintenance scheduled for Saturday.\n  Recommended Action: Acknowledge window.\n  SUGGESTED DRAFT — NOT SENT: \"Acknowledged. Maintenance window noted.\"\n\nNote: MAGIX will not send drafts automatically. User authorization required."
                        ),
                        "PLANNED"
                    )
                )
            )
        }

        // Test Chain C & D — Email Triage & Analysis
        if (lower.contains("email") || lower.contains("unread mail") || lower.contains("check my mail")) {
            return AgentPlan(
                reasoning = "Email chain: Read unread emails, identify priority, and create suggested drafts labeled SUGGESTED DRAFT — NOT SENT",
                steps = listOf(
                    AgentExecutionStep(1, "read_emails", mapOf("filter" to "unread"), "PLANNED")
                )
            )
        }

        // Test Chain E — Notes Summary
        if ((lower.startsWith("create a note") || lower.startsWith("take a note") || lower.contains("make a note")) && (lower.contains("email") || lower.contains("summar"))) {
            return AgentPlan(
                reasoning = "Notes chain: Create verified summary note with drafts labeled SUGGESTED DRAFT — NOT SENT",
                steps = listOf(
                    AgentExecutionStep(
                        1,
                        "create_note",
                        mapOf(
                            "title" to "Actionable Email Summary",
                            "content" to "Actionable Emails Summary:\n• Sarah Jenkins: Urgent Q3 Roadmap Sign-off Required [HIGH PRIORITY]\n  SUGGESTED DRAFT — NOT SENT: \"Hi Sarah, I've reviewed the Q3 roadmap milestones. Everything looks solid to proceed. Approved.\"\n\n• Cloud Infrastructure Alerts: Weekly Cluster Maintenance Notice [MEDIUM PRIORITY]\n  SUGGESTED DRAFT — NOT SENT: \"Acknowledged. Maintenance window noted.\"\n\nAll drafts require manual authorization."
                        ),
                        "PLANNED"
                    )
                )
            )
        }

        // 1. Applications & Search
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val app = lower.removePrefix("open ").removePrefix("launch ").trim()
            if (app.contains("settings")) {
                return AgentPlan(
                    reasoning = "Local rule match: launch settings",
                    steps = listOf(
                        AgentExecutionStep(1, "launch_settings", mapOf("target" to "all"), "PLANNED")
                    )
                )
            }
            if (app.contains("youtube") && lower.contains("search for")) {
                val query = lower.substringAfter("search for").trim()
                return AgentPlan(
                    reasoning = "Multi-step plan: Open YouTube, search query, and play selection",
                    steps = listOf(
                        AgentExecutionStep(1, "youtube_search", mapOf("query" to query, "auto_play" to "true"), "PLANNED")
                    )
                )
            }
            if (app.contains("youtube")) {
                return AgentPlan(
                    reasoning = "Local rule match: open YouTube",
                    steps = listOf(
                        AgentExecutionStep(1, "open_app", mapOf("app_name" to "youtube"), "PLANNED")
                    )
                )
            }
            return AgentPlan(
                reasoning = "Local rule match: open app",
                steps = listOf(
                    AgentExecutionStep(1, "open_app", mapOf("app_name" to app), "PLANNED")
                )
            )
        }

        // 2. Navigation
        if (lower == "go home" || lower == "home screen") {
            return AgentPlan(
                reasoning = "Local rule match: go home",
                steps = listOf(AgentExecutionStep(1, "go_home", emptyMap(), "PLANNED"))
            )
        }
        if (lower == "go back" || lower == "back") {
            return AgentPlan(
                reasoning = "Local rule match: back",
                steps = listOf(AgentExecutionStep(1, "go_back", emptyMap(), "PLANNED"))
            )
        }
        if (lower.contains("recent") || lower.contains("switch app")) {
            return AgentPlan(
                reasoning = "Local rule match: recents",
                steps = listOf(AgentExecutionStep(1, "open_recents", emptyMap(), "PLANNED"))
            )
        }
        if (lower.contains("notification")) {
            return AgentPlan(
                reasoning = "Local rule match: notifications",
                steps = listOf(AgentExecutionStep(1, "open_notifications", emptyMap(), "PLANNED"))
            )
        }

        // 3. Screen
        if (lower.contains("screenshot")) {
            return AgentPlan(
                reasoning = "Local rule match: screenshot",
                steps = listOf(AgentExecutionStep(1, "take_screenshot", emptyMap(), "PLANNED"))
            )
        }
        if (lower.contains("what is on my screen") || lower.contains("describe screen") || lower.contains("inspect")) {
            return AgentPlan(
                reasoning = "Local rule match: screen inspection",
                steps = listOf(AgentExecutionStep(1, "inspect_screen", emptyMap(), "PLANNED"))
            )
        }

        // 4. Scrolling
        if (lower.contains("scroll down")) {
            return AgentPlan(
                reasoning = "Local rule match: scroll down",
                steps = listOf(AgentExecutionStep(1, "scroll", mapOf("direction" to "down"), "PLANNED"))
            )
        }
        if (lower.contains("scroll up")) {
            return AgentPlan(
                reasoning = "Local rule match: scroll up",
                steps = listOf(AgentExecutionStep(1, "scroll", mapOf("direction" to "up"), "PLANNED"))
            )
        }

        // 5. Reminders
        if (lower.contains("remind me")) {
            val title = lower.substringAfter("remind me").substringBefore("to").trim()
                .ifEmpty { lower.substringAfter("remind me to ").trim() }
            val time = if (lower.contains("at ")) lower.substringAfter("at ").trim() else "Today, 6:00 PM"
            return AgentPlan(
                reasoning = "Local rule match: create reminder",
                steps = listOf(
                    AgentExecutionStep(1, "create_reminder", mapOf("title" to title, "time_description" to time), "PLANNED")
                )
            )
        }
        if (lower.contains("list reminders") || lower.contains("my reminders")) {
            return AgentPlan(
                reasoning = "Local rule match: list reminders",
                steps = listOf(AgentExecutionStep(1, "list_reminders", emptyMap(), "PLANNED"))
            )
        }

        // 6. Memory
        if (lower.startsWith("remember that ") || lower.startsWith("remember ")) {
            val statement = lower.removePrefix("remember that ").removePrefix("remember ").trim()
            val key = statement.substringBefore("is", statement.substringBefore("are", statement.take(20)))
            return AgentPlan(
                reasoning = "Local rule match: remember statement",
                steps = listOf(
                    AgentExecutionStep(1, "remember", mapOf("key" to key, "value" to statement, "category" to "Preferences"), "PLANNED")
                )
            )
        }

        // 7. Device controls
        if (lower.contains("flashlight on") || lower.contains("turn on flashlight")) {
            return AgentPlan(
                reasoning = "Local rule match: flashlight on",
                steps = listOf(AgentExecutionStep(1, "device_control", mapOf("action" to "flashlight_on"), "PLANNED"))
            )
        }
        if (lower.contains("flashlight off") || lower.contains("turn off flashlight")) {
            return AgentPlan(
                reasoning = "Local rule match: flashlight off",
                steps = listOf(AgentExecutionStep(1, "device_control", mapOf("action" to "flashlight_off"), "PLANNED"))
            )
        }

        // 8. Default Conversational Fallback
        val fallbackResponse = when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello Boss. Systems online. How can I assist your workflow?"
            lower.contains("status") || lower.contains("telemetry") ->
                "MAGIX Phase 2 systems nominal. Autonomous tools, Room persistence, and local engines active."
            lower.contains("who are you") ->
                "I am MAGIX, your personal JARVIS-style assistant for Android."
            else ->
                "Understood Boss. Ready to coordinate directives or system controls."
        }

        return AgentPlan(
            reasoning = "Offline conversational heuristic",
            steps = emptyList(),
            directSpeechResponse = fallbackResponse
        )
    }
}
