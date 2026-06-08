package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.Lead

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val leadId = intent.getIntExtra("lead_id", 0)
        val leadName = intent.getStringExtra("lead_name") ?: "Khách hàng"
        val leadPhone = intent.getStringExtra("lead_phone") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O (API 26) and above
        val channelId = "lead_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lịch hẹn tiếp xúc lại",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo lịch hẹn gặp/tiếp xúc lại khách hàng tiềm năng"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action: Call client
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$leadPhone")
        }
        val callPendingIntent = PendingIntent.getActivity(
            context,
            leadId + 1000,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Open Main Screen
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            leadId + 2000,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification with modern design hierarchy
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System standard alarm icon
            .setContentTitle("Lịch hẹn tiếp xúc lại!")
            .setContentText("Đến giờ liên hệ khách hàng: $leadName - SĐT: $leadPhone")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(appPendingIntent)
            .addAction(android.R.drawable.ic_menu_call, "Gọi điện", callPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Mở ứng dụng", appPendingIntent)

        notificationManager.notify(leadId, builder.build())
    }

    companion object {
        fun scheduleNotification(context: Context, lead: Lead) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("lead_id", lead.id)
                putExtra("lead_name", lead.name)
                putExtra("lead_phone", lead.phone)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                lead.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = lead.recontactDate
            // Only schedule future alarms
            if (triggerTime > System.currentTimeMillis()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    // Fallback to non-exact alarm on Android 12+ if SCHEDULE_EXACT_ALARM is restricted
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun cancelNotification(context: Context, leadId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                leadId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            
            // Cancel notification if active
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(leadId)
        }
    }
}
