package com.ai.roboteacher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.WebView
import com.ai.roboteacher.Models.AssignmentResponse
import com.ai.roboteacher.Models.SchoolData
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.google.gson.Gson
import okhttp3.Response

class RoboTeacher: Application() {

    var classApiData: AssignmentResponse.AssignmentData?=null


    var board:String?=null

    override fun onCreate() {
        super.onCreate()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                channelName,
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//
//
//        var statusIntent:Intent = Intent(this,StatusService::class.java)
//        startService(statusIntent)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//        }


    }


}