package com.example.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.IgnoredWarningEntity
import com.example.data.ScanHistoryEntity
import com.example.data.SecurityDatabase
import com.example.data.SecuritySettingsEntity
import com.example.security.AppPermissionAudit
import com.example.security.DeviceInfoData
import com.example.security.DeviceSecurityAnalyzer
import com.example.security.NotificationHelper
import com.example.security.SecurityCheckItem
import com.example.security.SecuritySeverity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SecurityUiState(
    val isScanning: Boolean = false,
    val scanProgress: Float = 1.0f,
    val scanStepMessage: String = "Escaneo completado",
    val score: Int = 100,
    val checks: List<SecurityCheckItem> = emptyList(),
    val deviceInfo: DeviceInfoData? = null,
    val appAudits: List<AppPermissionAudit> = emptyList(),
    val notificationGranted: Boolean = true,
    val lastScanTimestamp: Long = System.currentTimeMillis()
)

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SecurityDatabase.getDatabase(application)
    private val dao = db.securityDao()

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    val scanHistory: StateFlow<List<ScanHistoryEntity>> = dao.getScanHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<SecuritySettingsEntity> = dao.getSettings()
        .map { it ?: SecuritySettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecuritySettingsEntity())

    val ignoredWarnings: StateFlow<Set<String>> = dao.getIgnoredWarnings()
        .map { list -> list.map { it.checkKey }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        NotificationHelper.createNotificationChannels(getApplication())
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            checkNotificationPermission()
            val context = getApplication<Application>()
            val ignored = ignoredWarnings.value
            val checks = DeviceSecurityAnalyzer.analyzeDeviceSecurity(context, ignored)
            val info = DeviceSecurityAnalyzer.getDeviceInfo(context)
            val audits = DeviceSecurityAnalyzer.auditInstalledApps(context)
            val score = calculateScore(checks)

            _uiState.value = _uiState.value.copy(
                checks = checks,
                deviceInfo = info,
                appAudits = audits,
                score = score
            )
        }
    }

    fun runFullScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, scanProgress = 0.05f)

            val steps = listOf(
                "Iniciando escáner de seguridad..." to 0.15f,
                "Verificando bloqueo de pantalla y biometría..." to 0.35f,
                "Auditando opciones de desarrollador y depuración ADB..." to 0.55f,
                "Analizando permisos de aplicaciones instaladas..." to 0.75f,
                "Evaluando conectividad de red y almacenamiento..." to 0.90f,
                "Finalizando diagnóstico de seguridad..." to 1.0f
            )

            for ((msg, progress) in steps) {
                _uiState.value = _uiState.value.copy(
                    scanProgress = progress,
                    scanStepMessage = msg
                )
                delay(350)
            }

            val context = getApplication<Application>()
            val ignored = ignoredWarnings.value
            val checks = DeviceSecurityAnalyzer.analyzeDeviceSecurity(context, ignored)
            val info = DeviceSecurityAnalyzer.getDeviceInfo(context)
            val audits = DeviceSecurityAnalyzer.auditInstalledApps(context)
            val score = calculateScore(checks)

            val issuesCount = checks.count { it.severity != SecuritySeverity.SAFE && !it.isIgnored }

            // Save scan record
            dao.insertScanHistory(
                ScanHistoryEntity(
                    score = score,
                    totalChecks = checks.size,
                    issuesFound = issuesCount,
                    statusSummary = if (issuesCount == 0) "Teléfono protegido y seguro" else "$issuesCount problemas detectados"
                )
            )

            _uiState.value = _uiState.value.copy(
                isScanning = false,
                scanProgress = 1.0f,
                scanStepMessage = "Análisis completado",
                score = score,
                checks = checks,
                deviceInfo = info,
                appAudits = audits,
                lastScanTimestamp = System.currentTimeMillis()
            )

            // Send status notification if enabled
            if (settings.value.notifyOnWarning) {
                val title = if (score >= 90) "🛡️ Estado de Seguridad: Excelente ($score/100)" else "⚠️ Revisión de Seguridad: Atencion requerida ($score/100)"
                val body = if (issuesCount == 0) "Tu teléfono pasó todas las verificaciones." else "Se encontraron $issuesCount advertencias en tu dispositivo."
                NotificationHelper.sendStatusNotification(context, title, body)
            }
        }
    }

    fun toggleIgnoreCheck(checkKey: String) {
        viewModelScope.launch {
            val currentIgnored = ignoredWarnings.value
            if (currentIgnored.contains(checkKey)) {
                dao.removeIgnoredWarning(checkKey)
            } else {
                dao.addIgnoredWarning(IgnoredWarningEntity(checkKey))
            }
            delay(100)
            refreshData()
        }
    }

    fun triggerNotification(type: String) {
        val context = getApplication<Application>()
        when (type) {
            "STATUS" -> {
                val score = _uiState.value.score
                NotificationHelper.sendStatusNotification(
                    context,
                    "📱 Estado del Teléfono: $score/100",
                    "Servicio de seguridad activo. Memoria y sistema funcionando óptimamente."
                )
            }
            "ALERT" -> {
                NotificationHelper.sendSecurityAlertNotification(
                    context,
                    "⚠️ Alerta de Seguridad Simulada",
                    "Se ha detectado una modificación simulada en las configuraciones de red."
                )
            }
            "REMINDER" -> {
                NotificationHelper.sendReminderNotification(context)
            }
            "SOS" -> {
                val st = settings.value
                NotificationHelper.sendSosNotification(
                    context,
                    st.emergencyContactName,
                    st.emergencyContactPhone
                )
            }
        }
    }

    fun saveEmergencyContact(name: String, phone: String) {
        viewModelScope.launch {
            val current = settings.value
            val updated = current.copy(
                emergencyContactName = name,
                emergencyContactPhone = phone
            )
            dao.saveSettings(updated)
        }
    }

    fun updateSettings(autoScan: Boolean, notifyOnWarn: Boolean, soundAlerts: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            val updated = current.copy(
                autoScanEnabled = autoScan,
                notifyOnWarning = notifyOnWarn,
                soundAlerts = soundAlerts
            )
            dao.saveSettings(updated)
        }
    }

    fun checkNotificationPermission() {
        val context = getApplication<Application>()
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        _uiState.value = _uiState.value.copy(notificationGranted = granted)
    }

    private fun calculateScore(checks: List<SecurityCheckItem>): Int {
        var baseScore = 100
        for (check in checks) {
            if (check.isIgnored) continue
            when (check.severity) {
                SecuritySeverity.CRITICAL -> baseScore -= 25
                SecuritySeverity.WARNING -> baseScore -= 10
                SecuritySeverity.SAFE -> {}
            }
        }
        return baseScore.coerceIn(0, 100)
    }
}
