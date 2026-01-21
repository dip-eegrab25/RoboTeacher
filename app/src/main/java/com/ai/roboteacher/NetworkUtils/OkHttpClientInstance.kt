package com.ai.roboteacher.NetworkUtils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

object OkHttpClientInstance {


    var BASEURL = "http://192.168.1.41/panda/API/"

    var BASE_URL_STATUS = "http://192.168.1.41:8015/"

    var ATTBASEURL = "http://192.168.1.41:8001/"


    val classURL = "classList.php"
    val subURL = "subjectList.php"
    val otpDataUrl = "getAssignment.php"
    val feedBackUrl = "feedback.php"
    val curriculumUrl = "CurriculumList.php"
    val getTopicUrl = "getCurriculum.php"
    val pfdUrl = "http://192.168.1.41/pdf/%d/%s/%s.pdf"
    val statusURL = "status"
    val restarturl = "restart"
    val schoolList = "schoolList.php"
    val startAtt = "start"
    val stopAtt = "stop"


    var okHttpClient: OkHttpClient?=null
    var resultReceiver:ResultReceiver?=null

    fun getInstance(resultReceiver: ResultReceiver):OkHttpClientInstance {

        if (okHttpClient == null) {

            okHttpClient = OkHttpClient()

        }

        this.resultReceiver = resultReceiver

        return this

    }

     fun get(url:String) {

        CoroutineScope(Dispatchers.IO).launch {

            val request = Request.Builder()
                .url(BASEURL+url)
                .get()
                .build()

            try {

                var response = okHttpClient!!.newCall(request).execute()

                Log.d(OkHttpClientInstance::class.java.name, "get: " + response)

                response.let {

                    if (response.isSuccessful && response.code == 200) {

                        resultReceiver!!.onResult(response.code,response)


                    } else {

                        resultReceiver!!.onError(response.code,response)
                    }


                }



            } catch (ex:Exception) {

                Log.d(OkHttpClientInstance::class.java.name, "get: " + ex.message)

                resultReceiver?.onException("Server Error: ${ex.message}")

                ex.printStackTrace()




            }




        }

    }

     fun post(url: String,params:MutableMap<String,String>?) {

         CoroutineScope(Dispatchers.IO).launch {

                 var reqBody:RequestBody = FormBody.Builder().apply {

                     if (params!=null) {

                         val keySet:MutableSet<String> = params.keys

                         for (key in keySet ) {

                             Log.d("abcde", "$key:${params.get(key).toString()}")

                             add(key,params.get(key).toString())

                         }


                     }

                 } .build()


             val request:Request = Request.Builder()
                 .url(url)
                 .post(reqBody)
                 .build()

             try {

                 var response = okHttpClient!!.newCall(request).execute()


                 response.use {

                     if (response.isSuccessful && response.code == 200) {

                         resultReceiver!!.onResult(response.code,response)

                     } else {

                         resultReceiver!!.onError(response.code,response)
                     }


                 }

             } catch (ex:Exception) {

                 resultReceiver?.onException("Server Error: ${ex.message}")




             }
         }




     }

    fun postJson(url: String,params:MutableMap<String,String>) {

        CoroutineScope(Dispatchers.IO).launch {

            val json = JSONObject(params as Map<*, *>).toString()
            val JSON = "application/json; charset=utf-8".toMediaType()
            val reqBody = json.toRequestBody(JSON)

            val request:Request = Request.Builder()
                .url(url)
                .post(reqBody)
                .build()

            try {

                var response = okHttpClient!!.newCall(request).execute()


                response.use {

                    if (response.isSuccessful && response.code == 200) {

                        resultReceiver!!.onResult(response.code,response)

                    } else {

                        resultReceiver!!.onError(response.code,response)
                    }


                }

            } catch (ex:Exception) {

                resultReceiver?.onException("Server Error: ${ex.message}")




            }
        }




    }

    fun post(url: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val emptyJson = "{}".toRequestBody("application/json; charset=utf-8".toMediaType())

            val request:Request = Request.Builder()
                .url(BASEURL+url)
                .post(FormBody.Builder().build())
                .build()

            try {

                var response = okHttpClient!!.newCall(request).execute()
                Log.d(OkHttpClientInstance::class.java.name, "post: "+ response)

                response.use {

                    if (response.isSuccessful && response.code == 200) {

                        resultReceiver!!.onResult(response.code,response)

                    } else {

                        resultReceiver!!.onError(response.code,response)
                    }


                }

            } catch (ex:Exception) {

                resultReceiver?.onException("Server Error: ${ex.message}")




            }
        }




    }

    interface ResultReceiver{

        fun onResult(code:Int,response:Response?)
        fun onError(code:Int,response:Response?)
        fun onException(error:String?)
    }
}