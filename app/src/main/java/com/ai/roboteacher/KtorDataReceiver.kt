package com.ai.roboteacher

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.activities.ChatActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.cancel
import io.ktor.utils.io.core.EOFException
import io.ktor.utils.io.core.isNotEmpty
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.ConnectException

 class KtorDataReceiver(name:String,var data:String,var cls:Int=6,var subName:String="English",var dataListener: KtorDataListener?,val url:String):Runnable {

     val TAG = KtorDataReceiver::class.java.name

    //var isStopped = false
     var httpClient:HttpClient?=null
     var isRestart = false

//    override fun run() {
//
//        CoroutineScope(Dispatchers.IO).launch {
//
//            Log.d(TAG, "Coroutine launched")
//
//            httpClient = HttpClient(CIO) {
//                install(ContentNegotiation) {
//                    json()
//                }
//
//                engine {
//                    requestTimeout = 0 // ✅ For streaming: disable total timeout
//                    endpoint {
//                        connectTimeout = 15_000
//                        socketTimeout = 60_000
//                    }
//                }
//            }
//
//            val json = JSONObject().apply {
//                put("query", data)
//                put("class_number",cls)
//                put("subject_name",subName)
//
//            }
//
//            httpClient?.preparePost(RetrofitInstanceBuilder.BASEURL + RetrofitInstanceBuilder.TEACHING_ASSISTANT) {
//
//                contentType(Json)
//
//                setBody(json.toString())
//            }?.execute {
//
//
//                val channel = it.bodyAsChannel()
//
//                while (!channel.isClosedForRead) {
//
//                    var line = channel.readUTF8Line()?.trim()
//
//                    if (Utils.isBlank(line)) {
//
//                        continue
//                    }
//
//                    Log.d(TAG, "Data"+line)
//
//                    if (line!!.contains("|-")||line!!.startsWith("|-")) {
//
//                        continue
//                    }
//
//                    if (!isStopped) {
//
//                        dataListener.onDataReceived(line)
//
//                    }
//
//                }
//
//                dataListener.onDataReceived("Ended")
//
//                Log.d(ChatActivity::class.java.name, "End Reached")
//
//            }
//
//
//        }
//    }

     override fun run() {

         CoroutineScope(Dispatchers.IO).launch {

             Log.d(TAG, "Coroutine launched")

             httpClient = HttpClient(CIO) {
                 install(ContentNegotiation) {
                     json()
                 }

                 install(Logging) {

                     logger = Logger.ANDROID
                     level = LogLevel.ALL
                 }

                 engine {
                     requestTimeout = 0 // ✅ For streaming: disable total timeout
                     endpoint {
                         connectTimeout = 15_000
                         socketTimeout = 60_000
                     }
                 }
             }

             val json = JSONObject().apply {
                 put("query", data)
                 put("class_number",cls)
                 put("subject_name",subName)

             }

             Log.d("abcde", json.toString())

             try {


                 httpClient?.preparePost(RetrofitInstanceBuilder.BASEURL + url) {

                     contentType(Json)

                     setBody(json.toString())
                 }?.execute {

                     val strBuilder = StringBuilder()

                     val channel = it.bodyAsChannel()

                     while (!channel.isClosedForRead) {

                         val packet = channel.readRemaining(2048)

                         while (packet.isNotEmpty) {

                             val chunk = packet.readText()



                             if (chunk.contains("{\"name\":\"extract_textbook_content_by_metadata\"")) {

                                 isRestart = true

//                                 strBuilder.append("Session Ended. Please rewrite your question.")
                                 //dataListener?.onRestart("Session Ended. Please rewrite your question.",true)
                                 restartService("Session Ended. Please rewrite your question.")
                                 channel.cancel()
                                 break


                             }

                             if (chunk.isNullOrEmpty()) {

                                 continue


                             }

//                             strBuilder.append(chunk)
//                             val s1 = strBuilder.toString()

                             dataListener?.onDataReceived(chunk,false)


                             Log.d(TAG, "run: "+chunk)

//                             if (!isStopped) {




//                             }



                             //strBuilder.clear()
                         }


                     }

//                     if (!isStopped) {

                     if (!isRestart) {

                         dataListener?.onDataReceived("Ended",true)


                     }



                     //}

                     Log.d(KtorDataReceiver::class.java.name, "End Reached")

                 }

             }

//             catch (ex:CancellationException) {
//
//
//             } catch (ex:EOFException) {
//
//
//             }

             catch (ex:Exception) {

                 Log.d(KtorDataReceiver::class.java.name, "run: "+ex.message)

//                 httpClient!!.cancel()
//                 httpClient!!.close()
//                 httpClient = null
//
                 ex.printStackTrace()
//
                dataListener?.onError(ex)

             }




         }
     }

     private suspend fun restartService(errStr:String) {

         val okHttpClient = OkHttpClient()

         val request = Request.Builder()
             .url(OkHttpClientInstance.BASE_URL_STATUS + OkHttpClientInstance.restarturl)
             .get()
             .build()

             try {

                 var response = okHttpClient!!.newCall(request).execute()

                 Log.d(OkHttpClientInstance::class.java.name, "get: " + response)

                 response.let {

                     if (response.isSuccessful && response.code == 200) {

                         dataListener?.onRestart(errStr)



//                         response.body?.let {
//
//                             dataListener?.onDataReceived(errStr)
//
////                             var statusJson:JSONObject = JSONObject(it.string())
////
////                             Log.d(StatusService::class.java.name, "Status: "+statusJson.getString("status"))
////
////                             statusCallback!!.invoke(statusJson.getString("status"))
//
//                         }

                     }


                 }



             } catch (ex:Exception) {

                 ex.printStackTrace()

             }



     }

     interface KtorDataListener {

         suspend fun onDataReceived(line:String?,isEnded:Boolean)
         suspend fun onError(ex:Exception)
         suspend fun onRestart(msg:String)
     }


}