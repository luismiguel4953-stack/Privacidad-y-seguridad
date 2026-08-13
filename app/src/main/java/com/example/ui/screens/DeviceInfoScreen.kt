package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecuritySettingsEntity
import com.example.security.DeviceInfoData
import com.example.ui.SecurityUiState
import com.example.ui.theme.SecurityCyan
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.WarningAmber

@Composable
fun DeviceInfoScreen(
    state: SecurityUiState,
    settings: SecuritySettingsEntity,
    onSaveEmergencyContact: (String, String) -> Unit,
    onUpdateSettings: (Boolean, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val info = state.deviceInfo

    var sosName by remember(settings.emergencyContactName) { mutableStateOf(settings.emergencyContactName) }
    var sosPhone by remember(settings.emergencyContactPhone) { mutableStateOf(settings.emergencyContactPhone) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Información del Dispositivo y Avanzado",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Especificaciones del hardware, versión de Android y parámetros avanzados de seguridad.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Device Info Card (Nombre del Dispositivo y Versión)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_info_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Especificaciones del Teléfono",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (info != null) {
                        InfoRow(label = "Nombre del Dispositivo", value = "${info.manufacturer} ${info.modelName}")
                        InfoRow(label = "Versión de Android", value = "Android ${info.androidVersion} (API ${info.sdkInt})")
                        InfoRow(label = "Parche de Seguridad", value = info.securityPatch)
                        InfoRow(label = "Número de Compilación", value = info.buildNumber)
                        InfoRow(label = "Tipo de Red Activa", value = "${info.networkType} ${if (info.isWifiConnected) "📶" else "📱"}")
                    } else {
                        Text("Cargando datos del dispositivo...", fontSize = 13.sp)
                    }
                }
            }
        }

        // 2. Hardware Resources (RAM, Storage, Battery)
        if (info != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Recursos del Hardware",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Storage Bar
                        val usedStorageGb = (info.totalStorageGb - info.freeStorageGb).coerceAtLeast(0.0)
                        val storagePct = if (info.totalStorageGb > 0) (usedStorageGb / info.totalStorageGb).toFloat() else 0.5f

                        ResourceGaugeBar(
                            icon = Icons.Default.SdCard,
                            title = "Almacenamiento Interno",
                            subtitle = "${String.format("%.1f", usedStorageGb)} GB usados de ${info.totalStorageGb} GB",
                            percentage = storagePct,
                            barColor = SecurityCyan
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // RAM Bar
                        val usedRamGb = (info.totalRamGb - info.freeRamGb).coerceAtLeast(0.0)
                        val ramPct = if (info.totalRamGb > 0) (usedRamGb / info.totalRamGb).toFloat() else 0.5f

                        ResourceGaugeBar(
                            icon = Icons.Default.Memory,
                            title = "Memoria RAM",
                            subtitle = "${String.format("%.1f", usedRamGb)} GB usados de ${info.totalRamGb} GB",
                            percentage = ramPct,
                            barColor = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Battery
                        ResourceGaugeBar(
                            icon = Icons.Default.BatteryChargingFull,
                            title = "Batería y Temperatura",
                            subtitle = "${info.batteryLevel}% • ${if (info.isCharging) "Cargando" else "Descargando"} • ${info.batteryTemperatureCelsius}°C",
                            percentage = info.batteryLevel / 100f,
                            barColor = if (info.batteryLevel > 20) SecurityGreen else WarningAmber
                        )
                    }
                }
            }
        }

        // 3. Configuración Avanzada - Emergency SOS Contact
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sos_contact_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contacto de Emergencia SOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Configura el contacto que recibirá alertas SOS simuladas en caso de pánico.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sosName,
                        onValueChange = { sosName = it },
                        label = { Text("Nombre del Contacto") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sosPhone,
                        onValueChange = { sosPhone = it },
                        label = { Text("Número Telefónico") },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onSaveEmergencyContact(sosName, sosPhone)
                            Toast.makeText(context, "Contacto SOS guardado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Contacto de Emergencia")
                    }
                }
            }
        }

        // 4. Configuración Avanzada - General Options & Exporting
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("advanced_config_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Configuración Avanzada del Servicio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Análisis Automático en Antesegundo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Ejecuta un escáner de seguridad al abrir la aplicación.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.autoScanEnabled,
                            onCheckedChange = { checked ->
                                onUpdateSettings(checked, settings.notifyOnWarning, settings.soundAlerts)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            if (info != null) {
                                val report = buildString {
                                    appendLine("=== REPORTE DE AUDITORÍA DE SEGURIDAD ===")
                                    appendLine("Nombre Dispositivo: ${info.manufacturer} ${info.modelName}")
                                    appendLine("Android Version: ${info.androidVersion} (API ${info.sdkInt})")
                                    appendLine("Puntaje de Seguridad: ${state.score}/100")
                                    appendLine("Revisiones Pasadas: ${state.checks.count { it.severity == com.example.security.SecuritySeverity.SAFE }}")
                                    appendLine("Alertas Pendientes: ${state.checks.count { it.severity != com.example.security.SecuritySeverity.SAFE && !it.isIgnored }}")
                                    appendLine("Apps Auditadas: ${state.appAudits.size}")
                                    appendLine("=========================================")
                                }

                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Reporte Seguridad", report))
                                Toast.makeText(context, "Reporte copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_report_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copiar Reporte de Diagnóstico")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Divider(
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ResourceGaugeBar(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    percentage: Float,
    barColor: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = barColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
