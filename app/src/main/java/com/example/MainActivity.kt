package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.SecurityViewModel
import com.example.ui.screens.DeviceInfoScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.theme.SecurityServiceTheme

enum class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    SCAN("scan", "Revisar Teléfono", Icons.Default.Shield),
    PERMISSIONS("permissions", "Permisos", Icons.Default.Security),
    NOTIFICATIONS("notifications", "Notificaciones", Icons.Default.Notifications),
    DEVICE("device", "Dispositivo", Icons.Default.PhoneAndroid)
}

class MainActivity : ComponentActivity() {

    private val viewModel: SecurityViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.checkNotificationPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Prompt notification permission if on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SecurityServiceTheme {
                SecurityServiceApp(
                    viewModel = viewModel,
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SecurityServiceApp(
    viewModel: SecurityViewModel,
    onRequestNotificationPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var currentDestination by remember { mutableStateOf(NavDestination.SCAN) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavDestination.values().forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_item_${destination.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            modifier = Modifier.padding(innerPadding),
            label = "ScreenTransition"
        ) { destination ->
            when (destination) {
                NavDestination.SCAN -> ScanScreen(
                    state = uiState,
                    scanHistory = scanHistory,
                    onRunScan = { viewModel.runFullScan() },
                    onToggleIgnore = { key -> viewModel.toggleIgnoreCheck(key) }
                )

                NavDestination.PERMISSIONS -> PermissionsScreen(
                    state = uiState,
                    onRequestNotificationPermission = onRequestNotificationPermission
                )

                NavDestination.NOTIFICATIONS -> NotificationsScreen(
                    state = uiState,
                    settings = settings,
                    onTriggerNotification = { type -> viewModel.triggerNotification(type) },
                    onUpdateSettings = { autoScan, notifyWarn, sound ->
                        viewModel.updateSettings(autoScan, notifyWarn, sound)
                    },
                    onRequestPermission = onRequestNotificationPermission
                )

                NavDestination.DEVICE -> DeviceInfoScreen(
                    state = uiState,
                    settings = settings,
                    onSaveEmergencyContact = { name, phone ->
                        viewModel.saveEmergencyContact(name, phone)
                    },
                    onUpdateSettings = { autoScan, notifyWarn, sound ->
                        viewModel.updateSettings(autoScan, notifyWarn, sound)
                    }
                )
            }
        }
    }
}
