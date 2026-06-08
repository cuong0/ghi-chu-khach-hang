package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.LeadDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val database = LeadDatabase.getDatabase(context)
            val leadDao = database.leadDao()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val allLeads = leadDao.getAllLeads().first()
                    val now = System.currentTimeMillis()
                    for (lead in allLeads) {
                        if (lead.recontactDate > now) {
                            ReminderReceiver.scheduleNotification(context, lead)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
