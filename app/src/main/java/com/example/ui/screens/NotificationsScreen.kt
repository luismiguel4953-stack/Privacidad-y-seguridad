package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecuritySettingsEntity
import com.example.ui.SecurityUiState
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SecurityCyan
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.WarningAmber

@Composable
fun NotificationsScreen(
    state: SecurityUiState,
    settings: SecuritySettingsEntity,
    onTriggerNotification: (String) -> Unit,
    onUpdateSettings: (Boolean, Boolean, Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Centro de Notificaciones y Alertas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Emisión de notificaciones push, alertas de estado y configuración de avisos del teléfono.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Notification Permission Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_permission_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.notificationGranted) MaterialTheme.colorScheme.surface else WarningAmber.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.notificationGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (state.notificationGranted) SecurityGreen else WarningAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (state.notificationGranted) "Canal de Alertas Activo" else "Permiso de Notificación Pendiente",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        if (!state.notificationGranted) {
                            Button(
                                onClick = onRequestPermission,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Conceder")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "El servicio utiliza 2 canales principales en Android: 'Alertas de Seguridad' y 'Estado del Teléfono'. Puedes probar su funcionamiento abajo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Interactive Notification Triggers
        item {
            Text(
                text = "Simulador de Notificaciones en Tiempo Real",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Presiona cualquier botón para enviar una notificación directa a la barra de estado de Android.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            NotificationActionCard(
                title = "📱 Reporte de Estado del Teléfono",
                description = "Envía una notificación con el resumen actual de seguridad (${state.score}/100) y salud de memoria/batería.",
                buttonText = "Enviar Notificación de Estado",
                tag = "btn_notify_status",
                buttonColor = SecurityCyan,
                onClick = { onTriggerNotification("STATUS") }
            )
        }

        item {
            NotificationActionCard(
                title = "⚠️ Alerta de Seguridad Crítica",
                description = "Genera una notificación de máxima prioridad alertando sobre una posible vulnerabilidad o puerto expuesto.",
                buttonText = "Enviar Alerta de Seguridad",
                tag = "btn_notify_alert",
                buttonColor = WarningAmber,
                onClick = { onTriggerNotification("ALERT") }
            )
        }

        item {
            NotificationActionCard(
                title = "⏰ Recordatorio para Revisar tu Teléfono",
                description = "Notificación periódica para sugerir al usuario ejecutar el escáner de diagnóstico de seguridad.",
                buttonText = "Enviar Recordatorio de Revisión",
                tag = "btn_notify_reminder",
                buttonColor = MaterialTheme.colorScheme.primary,
                onClick = { onTriggerNotification("REMINDER") }
            )
        }

        item {
            NotificationActionCard(
                title = "🚨 Alerta SOS de Pánico",
                description = "Envía una notificación de emergencia simulando un envío de ayuda a tus contactos de seguridad.",
                buttonText = "Ejecutar Alerta SOS",
                tag = "btn_notify_sos",
                buttonColor = DangerRed,
                onClick = { onTriggerNotification("SOS") }
            )
        }

        // Notification Preferences
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ajustes de Notificaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notificar en cada Análisis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Envía un informe automático al completar la revisión del teléfono.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.notifyOnWarning,
                            onCheckedChange = { checked ->
                                onUpdateSettings(settings.autoScanEnabled, checked, settings.soundAlerts)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alertas con Sonido y Vibración",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Activa respuestas hápticas y sonido en notificaciones críticas.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.soundAlerts,
                            onCheckedChange = { checked ->
                                onUpdateSettings(settings.autoScanEnabled, settings.notifyOnWarning, checked)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abrir Configuración de Canales de Android")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationActionCard(
    title: String,
    description: String,
    buttonText: String,
    tag: String,
    buttonColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag(tag),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = buttonText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
