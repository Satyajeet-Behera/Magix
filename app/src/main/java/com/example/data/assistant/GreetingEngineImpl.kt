package com.example.data.assistant

import com.example.domain.contract.GreetingEngine
import com.example.domain.contract.TimeOfDay
import java.util.Calendar

class GreetingEngineImpl : GreetingEngine {

    override fun getTimeOfDay(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..22 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }

    override fun getSalutation(): String {
        return getTimeOfDay().salutation
    }

    override fun generateInvocationGreeting(formOfAddress: String): String {
        val address = formOfAddress.trim().ifBlank { "Boss" }
        val salutation = getSalutation()
        return "Hello $address. $salutation. How can I help you?"
    }

    override fun generateHeaderGreeting(name: String): String {
        val displayName = name.trim().ifBlank { "Boss" }
        return "${getSalutation()}, $displayName"
    }
}
