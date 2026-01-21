package com.ai.roboteacher

import android.content.Context
import android.content.Intent
import android.util.Log
import com.ai.roboteacher.Notification.SampleNotification
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.activities.QuestionViewActivity
import com.ai.roboteacher.services.QuestionGeneratorService
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
import io.ktor.utils.io.core.EOFException
import io.ktor.utils.io.core.isNotEmpty
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

class QuestionDataReceiver {

    var httpClient:HttpClient?=null
    var questionsList:ArrayList<String> = ArrayList()
    var pdfIntent:Intent?=null
    var isStopped:AtomicBoolean = AtomicBoolean(false)
    var thinkRegex = Regex("<think>(.*?)</think>|\\*+|#+",RegexOption.DOT_MATCHES_ALL)
    var callback:((String)->Unit)?=null
    var c:Context?=null

    companion object{

        var isRunning:AtomicBoolean = AtomicBoolean(false)
    }

    constructor(c:Context,query:String,jsonString: String,cb: (String) -> Unit) {

        this.callback = cb
        this.c = c

        pdfIntent = Intent(c, QuestionViewActivity::class.java)
        pdfIntent!!.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        pdfIntent!!.putStringArrayListExtra("dataList",questionsList)

        if (!isRunning.get()) {

            startStream(jsonString)
        }


    }





    private fun startStream(jsonString:String) {

        processData1(jsonString)

    }

    private fun processData1(data:String) {

        var isFirst = true

        isRunning.set(true)

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



                                    if (!chunk.equals("") && !isStopped.get()) {

                                        val strBuilder = StringBuilder()

                                        strBuilder.append(chunk.replace(thinkRegex,""))

                                        var qList = strBuilder.split(Regex("\\s{2,}",RegexOption.DOT_MATCHES_ALL))

                                        questionsList.addAll(qList)

                                        Log.d(QuestionGeneratorService::class.java.name, "Data: "+strBuilder.toString())

                                        callback?.invoke(strBuilder.toString())


                                    }

                                    //strBuilder.clear()

                                    //dataListener.onDataReceived(strBuilder.toString())

                                    //strBuilder.clear()
                                }

                            }

                            if (!isStopped.get()) {

                                callback?.invoke("Ended")

                                withContext(Dispatchers.Main) {

                                    val n: SampleNotification = SampleNotification(c!!
                                        ,jsonObject.getString("query"),object : SampleNotification.NotificationClickListener{
                                            override fun onNotificationClicked() {

                                                c!!.startActivity(pdfIntent)

                                            }


                                        })


                                }



                            }

                            isRunning.set(false)



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

                } catch (ex:CancellationException) {

                    ex.printStackTrace()

                    isStopped.set(true)
                    isRunning.set(false)

                    callback?.invoke("abdfg: ${ex.message}")


                } catch (ex:EOFException) {

                    ex.printStackTrace()

                    isStopped.set(true)
                    isRunning.set(false)

                    callback?.invoke("abdfg: ${ex.message}")


                }

                catch (ex:Exception) {

                    ex.printStackTrace()

//                    httpClient!!.cancel()
//                    httpClient!!.close()
//                    httpClient = null

                    isStopped.set(true)
                    isRunning.set(false)

                    callback?.invoke("abdfg: ${ex.message}")

                }




            }


        }

    }
}