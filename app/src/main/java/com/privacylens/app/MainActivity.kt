package com.privacylens.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.enableEdgeToEdge(window)

        setContent {
            var darkMode by rememberSaveable {
                mutableStateOf(true)
            }

            PrivacyLensTheme(
                darkMode = darkMode
            ) {
                PrivacyLensApp(
                    darkMode = darkMode,
                    onToggleTheme = {
                        darkMode = !darkMode
                    }
                )
            }
        }
    }
}

private val DarkBackground = Color(0xFF080B12)
private val DarkSurface = Color(0xFF10151F)
private val DarkSurface2 = Color(0xFF151C28)
private val DarkCyan = Color(0xFF65E6FF)
private val DarkGreen = Color(0xFF62E6A7)
private val DarkAmber = Color(0xFFFFC857)
private val DarkText = Color(0xFFE8F0F7)
private val DarkMuted = Color(0xFF8D9AAA)
private val DarkBorder = Color(0xFF293342)

private val LightBackground = Color(0xFFF1F5F8)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurface2 = Color(0xFFE3EBF1)
private val LightCyan = Color(0xFF007A91)
private val LightGreen = Color(0xFF087A55)
private val LightAmber = Color(0xFF9A6500)
private val LightText = Color(0xFF14202B)
private val LightMuted = Color(0xFF455563)
private val LightBorder = Color(0xFFB9C7D1)

@Composable
fun PrivacyLensTheme(
    darkMode: Boolean,
    content: @Composable () -> Unit
) {

    val colors = if (darkMode) {
        darkColorScheme(
            primary = DarkCyan,
            secondary = DarkGreen,
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkSurface2,
            onBackground = DarkText,
            onSurface = DarkText,
            onSurfaceVariant = DarkMuted,
            outline = DarkBorder
        )
    } else {
        lightColorScheme(
            primary = LightCyan,
            secondary = LightGreen,
            tertiary = LightGreen,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightSurface2,
            onBackground = LightText,
            onSurface = LightText,
            onSurfaceVariant = LightMuted,
            outline = LightBorder
        )
    }

    val view = LocalView.current

    SideEffect {
        val controller = WindowCompat.getInsetsController(
            (view.context as android.app.Activity).window,
            view
        )

        controller.isAppearanceLightStatusBars = !darkMode
        controller.isAppearanceLightNavigationBars = !darkMode
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Composable
fun PrivacyLensApp(
    darkMode: Boolean,
    onToggleTheme: () -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    var apps by remember {
        mutableStateOf<List<AppInfo>>(emptyList())
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var selectedApp by remember {
        mutableStateOf<AppInfo?>(null)
    }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            AppScanner.getInstalledApps(context)
        }
        loading = false
    }

    if (selectedApp != null) {

        BackHandler {
            selectedApp = null
        }

        AppDetailsScreen(
            app = selectedApp!!,
            darkMode = darkMode,
            onToggleTheme = onToggleTheme,
            onBack = {
                selectedApp = null
            }
        )

        return
    }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 18.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(
                            id = R.drawable.ic_privacy_lens_mark
                        ),
                        contentDescription = "PrivacyLens",
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "PRIVACY",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Lens",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ThemeButton(
                    darkMode = darkMode,
                    onClick = onToggleTheme
                )

                Spacer(modifier = Modifier.width(10.dp))

                StatusDot(
                    active = !loading
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = if (loading) {
                                "SCANNING DEVICE"
                            } else {
                                "DEVICE SCANNED"
                            },
                            color = if (loading) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = if (loading) {
                                "Analyzing installed apps..."
                            } else {
                                "${apps.size} launchable applications"
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "SEC",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                label = {
                    Text("Search applications")
                },
                placeholder = {
                    Text("Name or package")
                },
                leadingIcon = {
                    Text(
                        text = "⌕",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 23.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (loading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            } else {

                Text(
                    text = "${filteredApps.size} RESULTS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(
                        items = filteredApps,
                        key = {
                            it.packageName
                        }
                    ) { app ->

                        AppRow(
                            app = app,
                            onClick = {
                                selectedApp = app
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeButton(
    darkMode: Boolean,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(13.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {

        Text(
            text = if (darkMode) "☀️" else "🌙",
            fontSize = 18.sp,
            modifier = Modifier.padding(
                horizontal = 11.dp,
                vertical = 7.dp
            )
        )
    }
}

@Composable
fun StatusDot(
    active: Boolean
) {

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(
                if (active) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
            )
    )
}

@Composable
fun AppRow(
    app: AppInfo,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                bitmap = app.icon.asImageBitmap(),
                contentDescription = app.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(13.dp))
            )

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = app.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = app.packageName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "›",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 27.sp
            )
        }
    }
}

@Composable
fun AppDetailsScreen(
    app: AppInfo,
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    var permissions by remember {
        mutableStateOf<List<PermissionInfo>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var showAll by remember {
        mutableStateOf(false)
    }

    var refreshKey by remember {
        mutableStateOf(0)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshKey++
    }

    LaunchedEffect(app.packageName, refreshKey) {

        loading = true

        permissions = withContext(Dispatchers.IO) {
            PermissionScanner.getPermissions(
                context,
                app.packageName
            )
        }

        loading = false
    }

    val sensitive = permissions
        .filter {
            it.category == PermissionCategory.SENSITIVE
        }
        .sortedWith(
            compareByDescending<PermissionInfo> { it.granted }
                .thenBy { it.name.lowercase() }
        )

    val special = permissions
        .filter {
            it.category == PermissionCategory.SPECIAL
        }
        .sortedWith(
            compareByDescending<PermissionInfo> { it.granted }
                .thenBy { it.name.lowercase() }
        )

    val other = permissions
        .filter {
            it.category == PermissionCategory.OTHER
        }
        .sortedWith(
            compareByDescending<PermissionInfo> { it.granted }
                .thenBy { it.name.lowercase() }
        )

    val grantedSensitive = sensitive.count {
        it.granted
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 18.dp)
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "‹  APPLICATIONS",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                ThemeButton(
                    darkMode = darkMode,
                    onClick = onToggleTheme
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            bitmap = app.icon.asImageBitmap(),
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(17.dp))
                        )

                        Spacer(modifier = Modifier.width(15.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "APPLICATION",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = app.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = app.packageName,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {

                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${app.packageName}")
                            )

                            settingsLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = if (darkMode) {
                                Color(0xFF061018)
                            } else {
                                Color.White
                            }
                        )
                    ) {
                        Text(
                            text = "MANAGE APP PERMISSIONS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (loading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    item {

                        PrivacySummary(
                            sensitiveCount = sensitive.size,
                            grantedSensitive = grantedSensitive,
                            otherCount = permissions.size -
                                sensitive.size -
                                special.size
                        )
                    }

                    if (sensitive.isNotEmpty()) {

                        item {
                            SectionHeader(
                                title = "SENSITIVE ACCESS",
                                count = sensitive.size
                            )
                        }

                        items(
                            items = sensitive,
                            key = {
                                it.permission
                            }
                        ) {
                            PermissionRow(it)
                        }
                    }

                    if (special.isNotEmpty()) {

                        item {
                            SectionHeader(
                                title = "SPECIAL ACCESS",
                                count = special.size
                            )
                        }

                        items(
                            items = special,
                            key = {
                                it.permission
                            }
                        ) {
                            PermissionRow(it)
                        }
                    }

                    item {

                        Button(
                            onClick = {
                                showAll = !showAll
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (showAll) {
                                    "HIDE OTHER PERMISSIONS"
                                } else {
                                    "SHOW ALL ${permissions.size} PERMISSIONS"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    if (showAll) {

                        item {
                            SectionHeader(
                                title = "OTHER DECLARED ACCESS",
                                count = other.size
                            )
                        }

                        items(
                            items = other,
                            key = {
                                it.permission
                            }
                        ) {
                            PermissionRow(it)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacySummary(
    sensitiveCount: Int,
    grantedSensitive: Int,
    otherCount: Int
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "PRIVACY EXPOSURE",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Permission overview",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "$grantedSensitive / $sensitiveCount",
                    color = if (grantedSensitive > 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Metric(
                    label = "SENSITIVE",
                    value = sensitiveCount.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )

                Metric(
                    label = "GRANTED",
                    value = grantedSensitive.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )

                Metric(
                    label = "OTHER",
                    value = otherCount.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Declared permissions and grant state do not prove that the app is actively using a capability.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun Metric(
    label: String,
    value: String,
    color: Color
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "%02d".format(count),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PermissionRow(
    permission: PermissionInfo
) {

    val statusColor = if (permission.granted) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(13.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = permission.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (permission.granted) {
                        "GRANTED"
                    } else {
                        "NOT GRANTED"
                    },
                    color = statusColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = permission.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = permission.permission,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                fontSize = 9.sp
            )
        }
    }
}
