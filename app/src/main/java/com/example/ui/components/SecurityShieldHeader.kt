package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SecurityCyan
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.WarningAmber

@Composable
fun SecurityShieldHeader(
    score: Int,
    isScanning: Boolean,
    scanProgress: Float,
    scanMessage: String,
    onRunScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "scoreAnimation"
    )

    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(isScanning) {
        if (isScanning) {
            rotationAnim.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotationAnim.snapTo(0f)
        }
    }

    val scoreColor = when {
        isScanning -> SecurityCyan
        score >= 90 -> SecurityGreen
        score >= 70 -> WarningAmber
        else -> DangerRed
    }

    val statusText = when {
        isScanning -> "Escaneando Teléfono..."
        score >= 90 -> "Teléfono Protegido"
        score >= 70 -> "Atención Requerida"
        else -> "Riesgo de Seguridad"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shield Ring Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                // Background Canvas Arc
                Canvas(modifier = Modifier.size(160.dp)) {
                    drawCircle(
                        color = scoreColor.copy(alpha = 0.15f),
                        style = Stroke(width = 12.dp.toPx())
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = if (isScanning) scanProgress * 360f else (animatedScore / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Rotating Scanner Ring Overlay
                if (isScanning) {
                    Canvas(
                        modifier = Modifier
                            .size(160.dp)
                            .rotate(rotationAnim.value)
                    ) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    SecurityCyan.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                }

                // Center Score or Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isScanning) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Escaneando",
                            tint = SecurityCyan,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "${(scanProgress * 100).toInt()}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecurityCyan
                        )
                    } else {
                        Text(
                            text = score.toString(),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = scoreColor
                        )
                        Text(
                            text = "Score / 100",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            Surface(
                color = scoreColor.copy(alpha = 0.15f),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isScanning -> Icons.Default.Refresh
                            score >= 90 -> Icons.Default.CheckCircle
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = scoreColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isScanning) scanMessage else "Auditoría integral en tiempo real del sistema",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = SecurityCyan,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = onRunScan,
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("scan_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScanning) "Analizando Dispositivo..." else "Revisar tu Teléfono Ahora",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
