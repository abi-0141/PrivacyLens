package com.privacylens.app

import android.graphics.Bitmap

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Bitmap,
    val isSystemApp: Boolean = false,
    val isUpdatedSystemApp: Boolean = false
)
