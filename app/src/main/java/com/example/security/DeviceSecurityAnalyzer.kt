package com.example.security

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class SecuritySeverity {
    SAFE,
    WARNING,
    CRITICAL
}

data class SecurityCheckItem(
    val key: String,
    val title: String,
    val description: String,
    val category: String, // "Seguridad de Pantalla", "Sistema", "Red", "Privacidad"
    val severity: SecuritySeverity,
    val recommendation: String,
    val actionType: String, // "SETTINGS_SECURITY", "SETTINGS_DEV", "SETTINGS_NOTIFICATIONS", "SETTINGS_UNKNOWN_SOURCES"
    val isIgnored: Boolean = false
)

data class DeviceInfoData(
    val modelName: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkInt: Int,
    val buildNumber: String,
    val securityPatch: String,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val batteryTemperatureCelsius: Float,
    val totalStorageGb: Double,
    val freeStorageGb: Double,
    val totalRamGb: Double,
    val freeRamGb: Double,
    val networkType: String,
    val isWifiConnected: Boolean
)

data class AppPermissionAudit(
    val packageName: String,
    val appName: String,
    val iconRes: Int = android.R.drawable.sym_def_app_icon,
    val isSystemApp: Boolean,
    val cameraGranted: Boolean,
    val locationGranted: Boolean,
    val microphoneGranted: Boolean,
    val contactsGranted: Boolean,
    val riskLevel: String // "Alto", "Medio", "Bajo"
)

object DeviceSecurityAnalyzer {

    fun analyzeDeviceSecurity(context: Context, ignoredKeys: Set<String>): List<SecurityCheckItem> {
        val list = mutableListOf<SecurityCheckItem>()

        // 1. Screen Lock Check
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isSecure = keyguardManager?.isDeviceSecure == true || keyguardManager?.isKeyguardSecure == true
        list.add(
            SecurityCheckItem(
                key = "screen_lock",
                title = "Bloqueo de Pantalla (PIN/Patrón/Biometría)",
                description = if (isSecure) "El dispositivo cuenta con una pantalla de bloqueo configurada." else "No se detectó un bloqueo de pantalla seguro (PIN, patrón o biometría).",
                category = "Pantalla y Acceso",
                severity = if (isSecure) SecuritySeverity.SAFE else SecuritySeverity.CRITICAL,
                recommendation = if (isSecure) "Protección activa. Nadie puede acceder sin autorización." else "Configura un PIN, patrón o huella digital de inmediato en Ajustes.",
                actionType = "SETTINGS_SECURITY",
                isIgnored = ignoredKeys.contains("screen_lock")
            )
        )

        // 2. Developer Options Check
        val devOptionsEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0

        list.add(
            SecurityCheckItem(
                key = "dev_options",
                title = "Opciones de Desarrollador",
                description = if (devOptionsEnabled) "Las Opciones de Desarrollador están activadas en el teléfono." else "Las Opciones de Desarrollador están desactivadas.",
                category = "Sistema",
                severity = if (devOptionsEnabled) SecuritySeverity.WARNING else SecuritySeverity.SAFE,
                recommendation = if (devOptionsEnabled) "Las opciones de desarrollo avanzadas pueden exponer el sistema a depuración externa." else "Configuración óptima para usuarios finales.",
                actionType = "SETTINGS_DEV",
                isIgnored = ignoredKeys.contains("dev_options")
            )
        )

        // 3. USB Debugging ADB Check
        val adbEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) != 0

        list.add(
            SecurityCheckItem(
                key = "adb_debugging",
                title = "Depuración USB (ADB)",
                description = if (adbEnabled) "La Depuración por USB está activada. Computadoras externas podrían enviar comandos." else "La Depuración USB está desactivada.",
                category = "Sistema",
                severity = if (adbEnabled) SecuritySeverity.CRITICAL else SecuritySeverity.SAFE,
                recommendation = if (adbEnabled) "Desactiva la Depuración USB cuando no estés programando." else "Tu dispositivo no aceptará comandos USB no autorizados.",
                actionType = "SETTINGS_DEV",
                isIgnored = ignoredKeys.contains("adb_debugging")
            )
        )

        // 4. Notification Permission Check
        val notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        list.add(
            SecurityCheckItem(
                key = "notification_permission",
                title = "Notificaciones de Seguridad",
                description = if (notificationPermissionGranted) "Los permisos de notificación están concedidos." else "Las notificaciones están desactivadas para el servicio de seguridad.",
                category = "Notificaciones",
                severity = if (notificationPermissionGranted) SecuritySeverity.SAFE else SecuritySeverity.WARNING,
                recommendation = if (notificationPermissionGranted) "Recibirás alertas en tiempo real sobre riesgos." else "Permite las notificaciones para recibir alertas oportunas sobre tu teléfono.",
                actionType = "SETTINGS_NOTIFICATIONS",
                isIgnored = ignoredKeys.contains("notification_permission")
            )
        )

        // 5. Unknown Sources Check
        val installNonMarketApps = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.INSTALL_NON_MARKET_APPS,
                    0
                ) != 0
            }
        } catch (e: Exception) {
            false
        }

        list.add(
            SecurityCheckItem(
                key = "unknown_sources",
                title = "Instalación de Aplicaciones Desconocidas",
                description = if (installNonMarketApps) "Se permite instalar aplicaciones fuera de tiendas oficiales." else "La instalación desde fuentes externas no verificadas está bloqueada.",
                category = "Privacidad y Apps",
                severity = if (installNonMarketApps) SecuritySeverity.WARNING else SecuritySeverity.SAFE,
                recommendation = if (installNonMarketApps) "Te sugerimos deshabilitar la instalación fuera de la Play Store." else "Protección contra malware de origenes sospechosos.",
                actionType = "SETTINGS_UNKNOWN_SOURCES",
                isIgnored = ignoredKeys.contains("unknown_sources")
            )
        )

        // 6. Network Connection Check
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        val netStatus = when {
            isWifi -> "Conectado a Red Wi-Fi"
            isCellular -> "Conectado a Datos Móviles"
            else -> "Sin conexión de red activa"
        }

        list.add(
            SecurityCheckItem(
                key = "network_security",
                title = "Estado de Red y Conectividad",
                description = netStatus,
                category = "Red",
                severity = SecuritySeverity.SAFE,
                recommendation = "Evita realizar transacciones bancarias en redes Wi-Fi públicas no cifradas.",
                actionType = "SETTINGS_NETWORK",
                isIgnored = ignoredKeys.contains("network_security")
            )
        )

        // 7. Screen Timeout Check
        val screenTimeoutMs = try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 30000)
        } catch (e: Exception) {
            30000
        }
        val timeoutMinutes = screenTimeoutMs / 60000

        list.add(
            SecurityCheckItem(
                key = "screen_timeout",
                title = "Tiempo de Apagado de Pantalla",
                description = "El tiempo de apagado es de ${if (timeoutMinutes > 0) "$timeoutMinutes min" else "${screenTimeoutMs/1000} seg"}.",
                category = "Pantalla y Acceso",
                severity = if (timeoutMinutes > 5) SecuritySeverity.WARNING else SecuritySeverity.SAFE,
                recommendation = if (timeoutMinutes > 5) "Un tiempo muy largo de pantalla encendida expone tu teléfono si lo dejas desatendido." else "Tiempo de suspensión adecuado.",
                actionType = "SETTINGS_DISPLAY",
                isIgnored = ignoredKeys.contains("screen_timeout")
            )
        )

        return list
    }

    fun getDeviceInfo(context: Context): DeviceInfoData {
        val model = Build.MODEL ?: "Desconocido"
        val manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Android"
        val version = Build.VERSION.RELEASE ?: "N/A"
        val sdk = Build.VERSION.SDK_INT
        val buildNum = Build.DISPLAY ?: "N/A"
        val patchLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A"

        // Battery info
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 100

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = tempTenths / 10.0f

        // Storage Info
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalStorageBytes = totalBlocks * blockSize
        val freeStorageBytes = availableBlocks * blockSize
        val bytesInGb = 1024.0 * 1024.0 * 1024.0

        val totalStorageGb = (totalStorageBytes / bytesInGb)
        val freeStorageGb = (freeStorageBytes / bytesInGb)

        // RAM Info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamGb = (memInfo.totalMem / bytesInGb)
        val freeRamGb = (memInfo.availMem / bytesInGb)

        // Network Info
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val netType = when {
            isWifi -> "Wi-Fi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Red Móvil"
            else -> "Sin Red"
        }

        return DeviceInfoData(
            modelName = model,
            manufacturer = manufacturer,
            androidVersion = version,
            sdkInt = sdk,
            buildNumber = buildNum,
            securityPatch = patchLevel,
            batteryLevel = batteryPct,
            isCharging = isCharging,
            batteryTemperatureCelsius = tempCelsius,
            totalStorageGb = String.format("%.1f", totalStorageGb).toDoubleOrNull() ?: totalStorageGb,
            freeStorageGb = String.format("%.1f", freeStorageGb).toDoubleOrNull() ?: freeStorageGb,
            totalRamGb = String.format("%.1f", totalRamGb).toDoubleOrNull() ?: totalRamGb,
            freeRamGb = String.format("%.1f", freeRamGb).toDoubleOrNull() ?: freeRamGb,
            networkType = netType,
            isWifiConnected = isWifi
        )
    }

    fun auditInstalledApps(context: Context): List<AppPermissionAudit> {
        val pm = context.packageManager
        val installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val result = mutableListOf<AppPermissionAudit>()

        for (pkg in installedPackages) {
            val appInfo = pkg.applicationInfo ?: continue
            // Skip pure system apps unless requested, but inspect standard user installed & updated apps
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystem && pkg.packageName != "com.aistudio.securityservice.vzkqa") continue

            val requestedPerms = pkg.requestedPermissions ?: emptyArray()
            val requestedFlags = pkg.requestedPermissionsFlags ?: intArrayOf()

            var camera = false
            var location = false
            var mic = false
            var contacts = false

            for (i in requestedPerms.indices) {
                val perm = requestedPerms[i]
                val isGranted = (requestedFlags.getOrNull(i) ?: 0 and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0

                if (isGranted) {
                    when (perm) {
                        android.Manifest.permission.CAMERA -> camera = true
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION -> location = true
                        android.Manifest.permission.RECORD_AUDIO -> mic = true
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.WRITE_CONTACTS -> contacts = true
                    }
                }
            }

            val appLabel = pm.getApplicationLabel(appInfo).toString()
            val countGranted = (if (camera) 1 else 0) + (if (location) 1 else 0) + (if (mic) 1 else 0) + (if (contacts) 1 else 0)

            val risk = when {
                countGranted >= 3 -> "Alto"
                countGranted >= 1 -> "Medio"
                else -> "Bajo"
            }

            result.add(
                AppPermissionAudit(
                    packageName = pkg.packageName,
                    appName = appLabel,
                    isSystemApp = isSystem,
                    cameraGranted = camera,
                    locationGranted = location,
                    microphoneGranted = mic,
                    contactsGranted = contacts,
                    riskLevel = risk
                )
            )
        }

        return result.sortedByDescending { 
            when (it.riskLevel) {
                "Alto" -> 3
                "Medio" -> 2
                else -> 1
            }
        }
    }
}
