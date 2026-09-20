package com.privacylens.app

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PermissionScanner {

    suspend fun getPermissions(
        context: Context,
        packageName: String
    ): List<PermissionInfo> = withContext(Dispatchers.IO) {

        val pm = context.packageManager

        val packageInfo = pm.getPackageInfo(
            packageName,
            PackageManager.GET_PERMISSIONS
        )

        val permissions = packageInfo.requestedPermissions
            ?: return@withContext emptyList()

        val flags = packageInfo.requestedPermissionsFlags
            ?: IntArray(permissions.size)

        permissions.mapIndexed { index, permission ->

            val granted =
                index < flags.size &&
                (flags[index] and 2) != 0

            val category = classify(permission)

            PermissionInfo(
                permission = permission,
                name = permissionName(permission),
                description = permissionDescription(permission),
                granted = granted,
                category = category
            )

        }.sortedWith(
            compareBy<PermissionInfo> {
                when (it.category) {
                    PermissionCategory.SENSITIVE -> 0
                    PermissionCategory.SPECIAL -> 1
                    PermissionCategory.OTHER -> 2
                }
            }.thenBy {
                it.name.lowercase()
            }
        )
    }

    private fun classify(permission: String): PermissionCategory {

        return when {

            sensitivePermissions.contains(permission) ->
                PermissionCategory.SENSITIVE

            specialPermissions.contains(permission) ->
                PermissionCategory.SPECIAL

            else ->
                PermissionCategory.OTHER
        }
    }

    private val sensitivePermissions = setOf(

        // Camera / microphone
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",

        // Location
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",

        // Contacts
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.GET_ACCOUNTS",

        // Phone
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_PHONE_NUMBERS",
        "android.permission.CALL_PHONE",
        "android.permission.ANSWER_PHONE_CALLS",
        "android.permission.READ_PHONE_STATE",

        // Call history
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",

        // SMS
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.RECEIVE_MMS",
        "android.permission.SEND_SMS",

        // Calendar
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR",

        // Sensors / activity
        "android.permission.ACTIVITY_RECOGNITION",
        "android.permission.BODY_SENSORS",

        // Nearby devices
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.BLUETOOTH_CONNECT",
        "android.permission.BLUETOOTH_ADVERTISE",

        // Media
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO",

        // Notifications
        "android.permission.POST_NOTIFICATIONS"
    )

    private val specialPermissions = setOf(

        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.REQUEST_INSTALL_PACKAGES",
        "android.permission.PACKAGE_USAGE_STATS",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.WRITE_SETTINGS",
        "android.permission.REQUEST_DELETE_PACKAGES",
        "android.permission.SCHEDULE_EXACT_ALARM",
        "android.permission.USE_FULL_SCREEN_INTENT"
    )

    private fun permissionName(permission: String): String {

        return when (permission) {

            "android.permission.CAMERA" ->
                "Camera"

            "android.permission.RECORD_AUDIO" ->
                "Microphone"

            "android.permission.ACCESS_FINE_LOCATION" ->
                "Precise Location"

            "android.permission.ACCESS_COARSE_LOCATION" ->
                "Approximate Location"

            "android.permission.ACCESS_BACKGROUND_LOCATION" ->
                "Background Location"

            "android.permission.READ_CONTACTS" ->
                "Read Contacts"

            "android.permission.WRITE_CONTACTS" ->
                "Modify Contacts"

            "android.permission.GET_ACCOUNTS" ->
                "Accounts"

            "android.permission.READ_PHONE_STATE" ->
                "Phone Information"

            "android.permission.READ_PHONE_NUMBERS" ->
                "Phone Number"

            "android.permission.CALL_PHONE" ->
                "Make Phone Calls"

            "android.permission.ANSWER_PHONE_CALLS" ->
                "Answer Phone Calls"

            "android.permission.READ_CALL_LOG" ->
                "Read Call History"

            "android.permission.WRITE_CALL_LOG" ->
                "Modify Call History"

            "android.permission.READ_SMS" ->
                "Read SMS"

            "android.permission.RECEIVE_SMS" ->
                "Receive SMS"

            "android.permission.RECEIVE_MMS" ->
                "Receive MMS"

            "android.permission.SEND_SMS" ->
                "Send SMS"

            "android.permission.READ_CALENDAR" ->
                "Read Calendar"

            "android.permission.WRITE_CALENDAR" ->
                "Modify Calendar"

            "android.permission.ACTIVITY_RECOGNITION" ->
                "Activity Recognition"

            "android.permission.BODY_SENSORS" ->
                "Body Sensors"

            "android.permission.BLUETOOTH_SCAN" ->
                "Nearby Bluetooth Devices"

            "android.permission.BLUETOOTH_CONNECT" ->
                "Bluetooth Connection"

            "android.permission.BLUETOOTH_ADVERTISE" ->
                "Bluetooth Advertising"

            "android.permission.READ_MEDIA_IMAGES" ->
                "Photos"

            "android.permission.READ_MEDIA_VIDEO" ->
                "Videos"

            "android.permission.READ_MEDIA_AUDIO" ->
                "Music and Audio"

            "android.permission.POST_NOTIFICATIONS" ->
                "Notifications"

            "android.permission.SYSTEM_ALERT_WINDOW" ->
                "Display Over Other Apps"

            "android.permission.REQUEST_INSTALL_PACKAGES" ->
                "Install Unknown Apps"

            "android.permission.PACKAGE_USAGE_STATS" ->
                "Usage Access"

            "android.permission.BIND_ACCESSIBILITY_SERVICE" ->
                "Accessibility Service"

            "android.permission.WRITE_SETTINGS" ->
                "Modify System Settings"

            "android.permission.REQUEST_DELETE_PACKAGES" ->
                "Request App Uninstallation"

            "android.permission.SCHEDULE_EXACT_ALARM" ->
                "Exact Alarms"

            "android.permission.USE_FULL_SCREEN_INTENT" ->
                "Full-Screen Notifications"

            "android.permission.INTERNET" ->
                "Internet Access"

            "android.permission.ACCESS_NETWORK_STATE" ->
                "Network Information"

            "android.permission.ACCESS_WIFI_STATE" ->
                "Wi-Fi Information"

            "android.permission.CHANGE_WIFI_STATE" ->
                "Change Wi-Fi State"

            "android.permission.VIBRATE" ->
                "Vibration"

            "android.permission.WAKE_LOCK" ->
                "Wake Lock"

            else ->
                permission
                    .substringAfterLast('.')
                    .lowercase()
                    .replace('_', ' ')
                    .replaceFirstChar {
                        it.uppercase()
                    }
        }
    }

    private fun permissionDescription(permission: String): String {

        return when (permission) {

            "android.permission.CAMERA" ->
                "Allows access to the device camera."

            "android.permission.RECORD_AUDIO" ->
                "Allows access to the device microphone."

            "android.permission.ACCESS_FINE_LOCATION" ->
                "Allows the app to request precise device location."

            "android.permission.ACCESS_COARSE_LOCATION" ->
                "Allows the app to request approximate device location."

            "android.permission.ACCESS_BACKGROUND_LOCATION" ->
                "Allows location access while the app is running in the background."

            "android.permission.READ_CONTACTS" ->
                "Allows the app to read contacts stored on the device."

            "android.permission.WRITE_CONTACTS" ->
                "Allows the app to modify contacts stored on the device."

            "android.permission.READ_SMS" ->
                "Allows the app to read SMS messages."

            "android.permission.SEND_SMS" ->
                "Allows the app to send SMS messages."

            "android.permission.READ_CALL_LOG" ->
                "Allows the app to read call history."

            "android.permission.CALL_PHONE" ->
                "Allows the app to initiate phone calls."

            "android.permission.ACTIVITY_RECOGNITION" ->
                "Allows access to physical activity information."

            "android.permission.BODY_SENSORS" ->
                "Allows access to supported body sensor data."

            "android.permission.BLUETOOTH_SCAN" ->
                "Allows discovery of nearby Bluetooth devices."

            "android.permission.BLUETOOTH_CONNECT" ->
                "Allows communication with paired Bluetooth devices."

            "android.permission.READ_MEDIA_IMAGES" ->
                "Allows access to photos selected through the applicable media permissions."

            "android.permission.READ_MEDIA_VIDEO" ->
                "Allows access to videos through the applicable media permissions."

            "android.permission.POST_NOTIFICATIONS" ->
                "Allows the app to post notifications."

            "android.permission.SYSTEM_ALERT_WINDOW" ->
                "Allows the app to display content over other apps."

            "android.permission.REQUEST_INSTALL_PACKAGES" ->
                "Allows the app to request installation of packages."

            "android.permission.PACKAGE_USAGE_STATS" ->
                "Allows access to usage statistics when the user grants the special access."

            "android.permission.BIND_ACCESSIBILITY_SERVICE" ->
                "Used by accessibility services that can interact with device UI."

            "android.permission.INTERNET" ->
                "Allows the app to open network connections."

            "android.permission.ACCESS_NETWORK_STATE" ->
                "Allows the app to inspect network connectivity."

            "android.permission.ACCESS_WIFI_STATE" ->
                "Allows the app to inspect Wi-Fi information."

            "android.permission.CHANGE_WIFI_STATE" ->
                "Allows the app to change Wi-Fi state."

            "android.permission.VIBRATE" ->
                "Allows the app to control vibration."

            "android.permission.WAKE_LOCK" ->
                "Allows the app to keep the device awake for certain operations."

            else ->
                "The app declares this Android capability."
        }
    }
}
