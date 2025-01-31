package com.example.musicroom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (TelephonyManager.EXTRA_STATE_RINGING == state) {
            // Call is incoming - pause music or the activity
            Log.d("IncomingCallReceiver", "Incoming call detected")
            if (context is RoomActivity) {
                // Trigger pause music
                context.onPause()  // This triggers onPause in RoomActivity
            }
        }

        if (TelephonyManager.EXTRA_STATE_IDLE == state) {
            // Call ended - resume music or activity
            Log.d("IncomingCallReceiver", "Call ended")
            if (context is RoomActivity) {
                // Trigger resume music
                context.onResume()  // This triggers onResume in RoomActivity
            }
        }
    }
}
