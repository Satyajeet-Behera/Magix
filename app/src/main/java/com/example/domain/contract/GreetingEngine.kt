package com.example.domain.contract

enum class TimeOfDay(val salutation: String) {
    MORNING("Good morning"),
    AFTERNOON("Good afternoon"),
    EVENING("Good evening"),
    NIGHT("Good evening") // Prompt: "Night: Hello Boss. Good evening. How can I help you?"
}

interface GreetingEngine {
    /**
     * Determines current device time of day.
     */
    fun getTimeOfDay(): TimeOfDay

    /**
     * Returns the salutation for the current time of day (e.g., "Good morning").
     */
    fun getSalutation(): String

    /**
     * Generates time-sensitive natural conversational invocation greeting.
     * e.g., "Hello Boss. Good morning. How can I help you?"
     */
    fun generateInvocationGreeting(formOfAddress: String = "Boss"): String

    /**
     * Generates time-sensitive dashboard greeting header.
     * e.g., "Good morning, Commander"
     */
    fun generateHeaderGreeting(name: String): String
}
