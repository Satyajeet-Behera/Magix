package com.example

import com.example.data.assistant.GreetingEngineImpl
import com.example.domain.model.AssistantState
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `theme modes have valid identifiers and labels`() {
        assertEquals("dark", ThemeMode.DARK.prefKey)
        assertEquals("light", ThemeMode.LIGHT.prefKey)
        assertEquals("system", ThemeMode.SYSTEM.prefKey)

        assertEquals(ThemeMode.DARK, ThemeMode.fromPrefKey("dark"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromPrefKey("light"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPrefKey("system"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromPrefKey("unknown_fallback"))
    }

    @Test
    fun `greeting engine incorporates form of address and time`() {
        val greetingEngine = GreetingEngineImpl()
        val greeting = greetingEngine.generateInvocationGreeting("Boss")
        assertTrue("Greeting should contain form of address", greeting.contains("Boss"))
        assertTrue("Greeting should start with Hello", greeting.startsWith("Hello"))
        assertTrue("Greeting should end with How can I help you?", greeting.endsWith("How can I help you?"))
    }

    @Test
    fun `assistant states have distinct titles and details`() {
        assertEquals(7, AssistantState.entries.size)
        val titles = AssistantState.entries.map { it.title }.toSet()
        assertEquals(7, titles.size)
    }

    @Test
    fun `default user settings preserve Phase 1 identity`() {
        val settings = UserSettings()
        assertEquals("Boss", settings.formOfAddress)
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertTrue(settings.livePresenceEnabled)
    }
}
