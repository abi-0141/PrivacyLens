package com.privacylens.app

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppScanner {

    suspend fun getInstalledApps(context: Context): List<AppInfo> {
        return withContext(Dispatchers.IO) {

            val packageManager = context.packageManager

            val apps = packageManager
                .getInstalledApplications(0)
                .filter { app ->
                    packageManager.getLaunchIntentForPackage(app.packageName) != null
                }
                .map { app ->

                    val icon: Drawable =
                        packageManager.getApplicationIcon(app)

                    AppInfo(
                        name = packageManager.getApplicationLabel(app).toString(),
                        packageName = app.packageName,
                        icon = icon.toBitmap()
                    )
                }
                .sortedBy { it.name.lowercase() }

            apps
        }
    }
}
