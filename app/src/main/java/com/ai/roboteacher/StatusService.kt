package com.ai.roboteacher

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance.BASEURL
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance.okHttpClient
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance.resultReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.IOException
import java.net.ConnectException
import java.util.concurrent.TimeUnit

class StatusService:Service() {

    private var serviceJob:Job?=null
    private var serviceScope:CoroutineScope?=null

    var statusCallback:((String)->Unit)?=null



    inner class StatusBinder:Binder() {

        public fun setStatusCallback(cb:((String)->Unit)?) {

            statusCallback = cb

        }
    }

    var statusBinder = StatusBinder()

    override fun onCreate() {
        super.onCreate()



    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (serviceJob == null) {

            serviceJob = SupervisorJob()

            serviceScope = CoroutineScope(Dispatchers.IO + serviceJob!!)

            val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("OkHttp", message)
                }
            })

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .retryOnConnectionFailure(true)
                .connectionPool(ConnectionPool(5, 5, TimeUnit.SECONDS))
                .build()


            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.41:8015/") // Make sure this ends with a slash
                .client(okHttpClient)
                .build()

            val statusApi: StatusApi = retrofit.create(StatusApi::class.java)

            serviceScope!!.launch(Dispatchers.IO) {

                while (true) {
                    try {
                        val response = statusApi.getStatus()

                        //Log.d("StatusService", "Response: code=${response.code()}, success=${response.isSuccessful}")

                        if (response.isSuccessful && response.code() == 200) {
                            response.body()?.string()?.let {
                                val statusJson = JSONObject(it)
                                val status = statusJson.getString("status")
                                statusCallback?.invoke(status)
                                Log.d("StatusService", "Status: $status")
                            }
                        } else {
                            statusCallback?.invoke("down")
                            Log.w("StatusService", "Server responded with code ${response.code()}")
                        }

                    } catch (ex: IOException) {
                        statusCallback?.invoke("")
                        Log.e("StatusService", "Connection error", ex)
                        //okHttpClient.connectionPool.evictAll()
                    }

                    delay(5000)
                }


            }


        }





//        CoroutineScope(Dispatchers.IO).launch {
//            while (true) {
//                try {
//                    val response = statusApi.getStatus()
//
//                    //Log.d("StatusService", "Response: code=${response.code()}, success=${response.isSuccessful}")
//
//                    if (response.isSuccessful && response.code() == 200) {
//                        response.body()?.string()?.let {
//                            val statusJson = JSONObject(it)
//                            val status = statusJson.getString("status")
//                            statusCallback?.invoke(status)
//                            Log.d("StatusService", "Status: $status")
//                        }
//                    } else {
//                        statusCallback?.invoke("down")
//                        Log.w("StatusService", "Server responded with code ${response.code()}")
//                    }
//
//                } catch (ex: IOException) {
//                    statusCallback?.invoke("")
//                    Log.e("StatusService", "Connection error", ex)
//                    //okHttpClient.connectionPool.evictAll()
//                }
//
//                delay(5000)
//            }
//        }







        return START_STICKY

    }

    override fun onBind(intent: Intent?): IBinder? {

        return statusBinder
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(StatusService::class.java.name, "onDestroy: ")

        serviceScope!!.cancel()
    }
}