package com.privacylens.app

data class PrivacyRiskResult(
    val score: Int,
    val level: PrivacyRiskLevel,
    val grantedSensitive: Int,
    val declaredSensitive: Int,
    val specialAccessCount: Int,
    val reasons: List<String>
)

enum class PrivacyRiskLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

object PrivacyRiskEngine {

    fun analyze(
        permissions: List<PermissionInfo>,
        isSystemApp: Boolean = false
    ): PrivacyRiskResult {

        val sensitive = permissions.filter {
            it.category == PermissionCategory.SENSITIVE
        }

        val special = permissions.filter {
            it.category == PermissionCategory.SPECIAL
        }

        var score = 0
        val reasons = mutableListOf<String>()

        val grantedSensitive = sensitive.count { it.granted }

        sensitive.forEach { permission ->

            if (permission.granted) {
                score += grantedWeight(permission.permission)

                if (!isSystemApp) {
                    reasons += "${permission.name} access is granted"
                }
            } else {
                score += declaredWeight(permission.permission)
            }
        }

        special.forEach { permission ->

            score += if (isSystemApp) {
                systemSpecialWeight(permission.permission)
            } else {
                specialWeight(permission.permission)
            }

            if (!isSystemApp) {
                reasons += "${permission.name} capability is declared"
            }
        }

        /*
         * System apps are not automatically considered safe.
         * Their platform-level access is simply interpreted differently.
         */
        if (isSystemApp) {
            score = (score * 0.35).toInt()
        }

        score = score.coerceIn(0, 100)

        val level = when {
            score >= 70 -> PrivacyRiskLevel.VERY_HIGH
            score >= 45 -> PrivacyRiskLevel.HIGH
            score >= 20 -> PrivacyRiskLevel.MODERATE
            else -> PrivacyRiskLevel.LOW
        }

        return PrivacyRiskResult(
            score = score,
            level = level,
            grantedSensitive = grantedSensitive,
            declaredSensitive = sensitive.size,
            specialAccessCount = special.size,
            reasons = reasons.take(8)
        )
    }

    private fun grantedWeight(permission: String): Int {
        return when (permission) {
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.RECEIVE_MMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG" -> 18

            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION" -> 14

            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.GET_ACCOUNTS" -> 12

            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_PHONE_NUMBERS",
            "android.permission.CALL_PHONE",
            "android.permission.ANSWER_PHONE_CALLS",
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR" -> 9

            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO" -> 6

            "android.permission.ACTIVITY_RECOGNITION",
            "android.permission.BODY_SENSORS" -> 5

            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADVERTISE" -> 4

            "android.permission.POST_NOTIFICATIONS" -> 1

            else -> 0
        }
    }

    private fun declaredWeight(permission: String): Int {
        return when (permission) {
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.RECEIVE_MMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG" -> 3

            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.GET_ACCOUNTS" -> 2

            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_PHONE_NUMBERS",
            "android.permission.CALL_PHONE",
            "android.permission.ANSWER_PHONE_CALLS",
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.ACTIVITY_RECOGNITION",
            "android.permission.BODY_SENSORS",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADVERTISE" -> 1

            else -> 0
        }
    }

    private fun specialWeight(permission: String): Int {
        return when (permission) {
            "android.permission.BIND_ACCESSIBILITY_SERVICE" -> 5
            "android.permission.SYSTEM_ALERT_WINDOW" -> 4
            "android.permission.PACKAGE_USAGE_STATS" -> 3
            "android.permission.WRITE_SETTINGS" -> 3
            "android.permission.REQUEST_INSTALL_PACKAGES" -> 3
            "android.permission.REQUEST_DELETE_PACKAGES" -> 2
            "android.permission.SCHEDULE_EXACT_ALARM" -> 1
            "android.permission.USE_FULL_SCREEN_INTENT" -> 1
            else -> 0
        }
    }

    private fun systemSpecialWeight(permission: String): Int {
        return when (permission) {
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.WRITE_SETTINGS",
            "android.permission.PACKAGE_USAGE_STATS" -> 1

            else -> 0
        }
    }
}
