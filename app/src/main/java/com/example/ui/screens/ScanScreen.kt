package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.ScanHistoryEntity
import com.example.security.SecurityCheckItem
import com.example.security.SecuritySeverity
import com.example.ui.SecurityUiState
import com.example.ui.components.SecurityShieldHeader
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SecurityCyan
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanScreen(
    state: SecurityUiState,
    scanHistory: List<ScanHistoryEntity>,
    onRunScan: () -> Unit,
    onToggleIgnore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("Todos") } // "Todos", "Alertas", "Ignorados"

    val filteredChecks = state.checks.filter { item ->
        when (selectedFilter) {
            "Alertas" -> item.severity != SecuritySeverity.SAFE && !item.isIgnored
            "Ignorados" -> item.isIgnored
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SecurityShieldHeader(
                score = state.score,
                isScanning = state.isScanning,
                scanProgress = state.scanProgress,
                scanMessage = state.scanStepMessage,
                onRunScan = onRunScan
            )
        }

        // Filter chips bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "Todos",
                    onClick = { selectedFilter = "Todos" },
                    label = { Text("Todas las revisiones (${state.checks.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                val issuesCount = state.checks.count { it.severity != SecuritySeverity.SAFE && !it.isIgnored }
                FilterChip(
                    selected = selectedFilter == "Alertas",
                    onClick = { selectedFilter = "Alertas" },
                    label = { Text("Atención ($issuesCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarningAmber.copy(alpha = 0.2f),
                        selectedLabelColor = WarningAmber
                    )
                )

                val ignoredCount = state.checks.count { it.isIgnored }
                FilterChip(
                    selected = selectedFilter == "Ignorados",
                    onClick = { selectedFilter = "Ignorados" },
                    label = { Text("Ignorados ($ignoredCount)") }
                )
            }
        }

        // Section Title
        item {
            Text(
                text = "Diagnóstico de Seguridad del Teléfono",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        if (filteredChecks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SecurityGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay elementos en esta categoría",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        } else {
            items(filteredChecks, key = { it.key }) { check ->
                SecurityCheckCard(
                    item = check,
                    onActionClick = { launchSettingsAction(context, check.actionType) },
                    onToggleIgnore = { onToggleIgnore(check.key) }
                )
            }
        }

        // Scan History Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Historial de Escaneos Guardados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (scanHistory.isEmpty()) {
            item {
                Text(
                    text = "Presiona 'Revisar tu Teléfono' para guardar tu primer registro de seguridad.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(scanHistory.take(5), key = { it.id }) { history ->
                ScanHistoryCard(history = history)
            }
        }
    }
}

@Composable
fun SecurityCheckCard(
    item: SecurityCheckItem,
    onActionClick: () -> Unit,
    onToggleIgnore: () -> Unit
) {
    val statusColor = when {
        item.isIgnored -> MaterialTheme.colorScheme.onSurfaceVariant
        item.severity == SecuritySeverity.SAFE -> SecurityGreen
        item.severity == SecuritySeverity.WARNING -> WarningAmber
        else -> DangerRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("check_card_${item.key}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onToggleIgnore,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isIgnored) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Ignorar o Mostrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = when {
                        item.severity == SecuritySeverity.SAFE -> Icons.Default.CheckCircle
                        item.severity == SecuritySeverity.WARNING -> Icons.Default.Warning
                        else -> Icons.Default.Security
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "💡 ${item.recommendation}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (item.severity != SecuritySeverity.SAFE) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Configurar en Ajustes del Sistema",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ScanHistoryCard(history: ScanHistoryEntity) {
    val dateStr = remember(history.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(history.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = history.statusSummary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "$dateStr • ${history.totalChecks} verificaciones realizadas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = if (history.score >= 90) SecurityGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Text(
                    text = "${history.score} pts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (history.score >= 90) SecurityGreen else WarningAmber,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun launchSettingsAction(context: Context, actionType: String) {
    val intent = when (actionType) {
        "SETTINGS_SECURITY" -> Intent(Settings.ACTION_SECURITY_SETTINGS)
        "SETTINGS_DEV" -> Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        "SETTINGS_NOTIFICATIONS" -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        "SETTINGS_UNKNOWN_SOURCES" -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        "SETTINGS_DISPLAY" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
        else -> Intent(Settings.ACTION_SETTINGS)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to general settings
        try {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (e2: Exception) {
            // ignore
        }
    }
}
