package com.example.security

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_ALERTS_ID = "channel_security_alerts"
    const val CHANNEL_REPORTS_ID = "channel_status_reports"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Alertas de Seguridad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de alertas críticas de seguridad del dispositivo"
                enableVibration(true)
            }

            val reportsChannel = NotificationChannel(
                CHANNEL_REPORTS_ID,
                "Estado del Teléfono y Revisiones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Informes del estado de seguridad y recordatorios de revisión"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(reportsChannel)
        }
    }

    fun sendStatusNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REPORTS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun sendSecurityAlertNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1002, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun sendReminderNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REPORTS_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🛡️ Es hora de revisar tu teléfono")
            .setContentText("Realiza un análisis rápido para verificar que tus configuraciones de seguridad estén actualizadas.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1003, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun sendSosNotification(context: Context, contactName: String, contactPhone: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val message = if (contactName.isNotBlank()) {
            "Simulación de Alerta SOS activada para contacto de emergencia: $contactName ($contactPhone)"
        } else {
            "Simulación de Alerta SOS de pánico ejecutada correctamente."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("🚨 ALERTA SOS SIMULADA")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1004, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
