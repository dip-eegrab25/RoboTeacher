package com.ai.roboteacher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ai.roboteacher.activities.SampleActivity2

class StartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d(StartReceiver::class.java.name, "onReceive: ")


        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.action) ) {

            Log.d(StartReceiver::class.java.name, "onReceive: ")

            Log.d(StartReceiver::class.java.name, "onReceived: ")

            Handler(Looper.getMainLooper()).postDelayed({
                val i = Intent(context, SampleActivity2::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }, 5000)
        }
    }
}