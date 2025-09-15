package com.notificationapp

// app/src/main/java/com/notificationapp/Config.kt 14.09.25 12:34
package com.notificationapp

import android.content.Context
import android.os.Build
import android.provider.Settings

object AppConfig {
    // TODO: Sæt din rigtige base – samme som Windows/Electron's config.backendUrl (typisk ".../api/devices")
    const val BACKEND_DEVICES_BASE = "https://YOUR-BACKEND-BASE/api/devices"

    // Default kanaler hvis ikke server sender andet ved registrering
    val DEFAULT_CHANNELS = listOf("default")

    const val APP_VERSION = "1.0.0-android"

    fun deviceId(ctx: Context): String {
        // Stabil ID: kombi af model + ANDROID_ID (ikke PII/navn)
        val androidId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        val model = Build.MODEL?.replace("\\s+".toRegex(), "-") ?: "Android"
        return "ANDR-${model}-${androidId?.takeLast(6) ?: "UNKNOWN"}"
    }

    fun deviceName(): String {
        // Samme idé som Windows "computerName" – her bruger vi model + manufacturer
        val manuf = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Android"
        val model = Build.MODEL ?: ""
        return "$manuf $model".trim()
    }
}