package com.ai.roboteacher.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.ai.roboteacher.Notification.SampleNotification
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.RoboTeacher
import com.ai.roboteacher.activities.QuestionViewActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.isNotEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class QuestionGeneratorService : Service() {

    private val TAG = QuestionGeneratorService::class.java.name

    var callback:((String)->Unit)?=null
    var notifMAnager:NotificationManager?=null
    var thinkRegex = Regex("<think>(.*?)</think>|\\*+|#+",RegexOption.DOT_MATCHES_ALL)
    var questionsList:ArrayList<String> = ArrayList()
    var pdfIntent:Intent?=null

    companion object {

        var isRunning = false

    }


    inner class StreamBinder : Binder() {
        fun setCallback(cb: (String) -> Unit) {
            callback = cb
        }

    }

    val binder:StreamBinder = StreamBinder()

    override fun onCreate() {
        super.onCreate()

        pdfIntent = Intent(applicationContext,QuestionViewActivity::class.java)
        pdfIntent!!.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pdfIntent!!.putStringArrayListExtra("dataList",questionsList)

        Log.d(QuestionGeneratorService::class.java.name, "Service Started")

//        notifMAnager = getSystemService(NotificationManager::class.java)
//
//        notificationBuilder = Notification.Builder(applicationContext,(applicationContext as RoboTeacher).channelId)
//            .setContentTitle("Generating Questions")
//            .setContentText("PLease wait while we generate questions...")
//            .setSmallIcon(R.mipmap.ic_launcher_round)
//            .setAutoCancel(false)
//            .setOngoing(true)
//
//        notification = notificationBuilder!!.build()
//
//
//        notifMAnager!!.notify(1,notification)
//
//        startForeground(1,notification)
    }
    override fun onBind(intent: Intent?): IBinder? {

        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        isRunning  = true

        val json = intent?.getStringExtra("json")
        if (json != null) {
            startStream(json)
        }
        // Service will be restarted if killed
        return START_STICKY
    }



    var httpClient:HttpClient?=null

    private fun startStream(jsonString:String) {

        processData1(jsonString)

    }

    private fun processData1(data:String) {

        var isFirst = true

        var jsonObject = JSONObject(data)

        Log.d(QuestionGeneratorService::class.java.name, "Json: "+jsonObject.toString())
//        callback!!.invoke(jsonObject.getString("query"))



        CoroutineScope(Dispatchers.IO).launch {



            if (httpClient == null) {

                httpClient = HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json()
                    }

                    engine {
                        requestTimeout = 0 // ✅ For streaming: disable total timeout
                        endpoint {
                            connectTimeout = 15_000
                            socketTimeout = 60_000
                        }
                    }
                }

                try {

                    Log.d(TAG, "Board: "+(applicationContext as RoboTeacher).board)

                    Log.d(TAG, "Url "+RetrofitInstanceBuilder.QUESTION_GENERATOR)

                    httpClient?.preparePost(RetrofitInstanceBuilder.BASEURL + RetrofitInstanceBuilder.QUESTION_GENERATOR) {

                        contentType(Json)

                        setBody(jsonObject.toString())
                    }
                        ?.execute {


                            val channel = it.bodyAsChannel()

//                        while (!channel.isClosedForRead) {
//
//                            var line = channel.readUTF8Line()?.trim()
//
//                            if (isBlank(line)) {
//
//                                continue
//                            }
//
//                            callback!!.invoke(line!!)
//
//
//                        }

                            while (!channel.isClosedForRead) {

                                val packet = channel.readRemaining(2048)

                                while (packet.isNotEmpty) {

                                    val chunk = packet.readText()

                                    val strBuilder = StringBuilder()

                                    strBuilder.append(chunk.replace(thinkRegex,""))

                                    var qList = strBuilder.split(Regex("\\s{2,}",RegexOption.DOT_MATCHES_ALL))

                                    questionsList.addAll(qList)

                                    Log.d(QuestionGeneratorService::class.java.name, "Data: "+strBuilder.toString())

                                    callback!!.invoke(strBuilder.toString())

                                    //strBuilder.clear()

                                    //dataListener.onDataReceived(strBuilder.toString())

                                    //strBuilder.clear()
                                }

                            }

                            callback!!.invoke("Ended")

                            isRunning = false

                            val n:SampleNotification = SampleNotification(applicationContext
                                ,jsonObject.getString("query"),object : SampleNotification.NotificationClickListener{
                                    override fun onNotificationClicked() {

                                        startActivity(pdfIntent)

                                    }


                                })

//                            val pendingIntent = PendingIntent.getActivity(applicationContext
//                                ,0
//                            ,pdfIntent
//                            ,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//                            notificationBuilder!!.setOngoing(false)
//                            notificationBuilder!!.setAutoCancel(true)
//                            notificationBuilder!!.setContentText("Completed")
//                            notificationBuilder!!.setContentIntent(pendingIntent)
//
//                            stopForeground(STOP_FOREGROUND_DETACH)
//                            notifMAnager!!.notify(1,notificationBuilder!!.build())


                            //Utils.generatePDF(applicationContext,questionsList)


                            //stopForeground(STOP_FOREGROUND_REMOVE)

                            //stopSelf()
                        }

                } catch (ex:Exception) {

                    Log.d(TAG, "Error: ${ex.message}")

                    callback!!.invoke("abdfg: ${ex.message}")

                    isRunning  = false

                    notifMAnager!!.cancel(1)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()



                }




            }


        }

    }


}