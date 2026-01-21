package com.ai.roboteacher.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.ai.roboteacher.StatusApi
import com.ai.roboteacher.StatusService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit


class SplashActivity:AppCompatActivity() {

    var isSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {

        //val splashScreen = installSplashScreen()

        //splashScreen.setKeepOnScreenCondition { isSplash }

//        var statusIntent: Intent = Intent(this, StatusService::class.java)
//        startService(statusIntent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Thread.sleep(1500)

        val intent = Intent(this,GeneralActivity::class.java)
        startActivity(intent)
        //intent.addCategory(Intent.CATEGORY_HOME)



      //  checkAudioPermissionAndStart()
//
//        try {
//
//            val intent = Intent(Intent.ACTION_MAIN)
//            intent.addCategory(Intent.CATEGORY_HOME)
//            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//
//        } catch (e:Exception) {
//
//            e.printStackTrace()
//
//
//        }


        //startActivity(Intent(this,SampleActivity::class.java))
        //checkAudioPermissionAndStart()

    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    private fun checkAudioPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                ),

                100
            )
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, 1234)
                } else {

                    val i = Intent(this, GeneralActivity::class.java)
                    startActivity(i)
                }
            }

//            buttonWakeUp!!.visibility = View.VISIBLE
//
//            buttonWakeUp!!.setOnClickListener {
//
//                buttonWakeUp!!.visibility = View.GONE
//
//                bottomSheetRoot!!.post {
//
//                    bottomSheetWidth = bottomSheetRoot!!.width
//                    bottomSheetRoot!!.translationX =
//                        bottomSheetWidth.toFloat()
//                    bottomSheetRoot!!.visibility = View.VISIBLE
//
//                    animateBottomSheetRoot(isReset)
//                }


        }

        //startSpeechRecognizer()

        //initVoskModel()

//            bottomSheetRoot!!.post {
//
//                var width = bottomSheetRoot!!.width
//                bottomSheetRoot!!.translationX = width.toFloat()
//                bottomSheetRoot!!.visibility = View.VISIBLE
//
//                valueAnim = MyValueAnimator(width,0, animListener = object:MyValueAnimator.AnimListener{
//                    override fun onValueReceived(value: Int) {
//
//                        Log.d(TAG, "onValueReceived: " +value)
//
//                        bottomSheetRoot!!.translationX = value.toFloat()
//
//                        if (!isReset) {
//
//                            if (value < 30) {
//
//                                valueAnim!!.pause()
//                                startSpeechRecognizer()
//                                isReset = true
//                            }
//
//
//                        } else {
//
//                            if (value>1260) {
//
//                                valueAnim!!.pause()
//                                isReset = false
//                            }
//                        }
//
//
//                    }
//
//                })
//
//                valueAnim!!.start()
//            }


        //startSpeechRecognizer()
        //initModel()

//            Handler().postDelayed({
//
//                valueAnim!!.start()
//            },3000)

    }


    private fun getBoardData(url: String) {

        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("OkHttp", message)
            }
        })

        logging.level = HttpLoggingInterceptor.Level.BODY

//

        val connectionSpecs: MutableList<ConnectionSpec> = ArrayList()
        connectionSpecs.add(ConnectionSpec.COMPATIBLE_TLS)


        val okHttpClient = OkHttpClient.Builder().followRedirects(false)
            .addNetworkInterceptor(logging)
//                .connectionSpecs(connectionSpecs)
            .build()

        val request = Request.Builder()
            .url(OkHttpClientInstance.BASE_URL_STATUS + OkHttpClientInstance.statusURL)
            .get()
            .build()

        CoroutineScope(Dispatchers.IO).launch {


            while (true) {

                try {

                    var response = okHttpClient!!.newCall(request).execute()

                    Log.d(OkHttpClientInstance::class.java.name, "get: " + response)

//                    response.let {
//
//                        if (response.isSuccessful && response.code == 200) {
//
//                            response.body?.let {
//
//                                var statusJson: JSONObject = JSONObject(it.string())
//
//                                //Log.d(StatusService::class.java.name, "Status: "+statusJson.getString("status"))
//
//                                //statusCallback?.invoke(statusJson.getString("status"))
//
//                            }
//
//                        } else {
//
//                            //statusCallback!!.invoke("down")
//
//                        }
//
//
//                    }

                } catch (ex: Exception) {

                    //statusCallback?.invoke("down")

                    okHttpClient.connectionPool.evictAll()
                    okHttpClient.dispatcher.cancelAll()

                    Log.d(StatusService::class.java.name, "onStartCommand: " + ex.message)

                }
            }

        }

//        OkHttpClientInstance.getInstance(object: OkHttpClientInstance.ResultReceiver{
//            override fun onResult(code: Int, response: Response?) {
//
//                Log.d("abcde", "Code: ${code}")
//
//                response?.let {
//
//                    if (response.isSuccessful && response.code==200) {
//
//                        response.body?.let {
//
//                            val gson = Gson()
//
//                            val schoolData:SchoolData = gson.fromJson(it.string(), SchoolData::class.java)
//
//                            var b:String = schoolData.data.get(0).board!!
//
//                            (applicationContext as RoboTeacher).board = b.lowercase()
//
//                            Log.d(SplashActivity::class.java.name, "Board: ${(applicationContext as RoboTeacher).board}")
//
//                            if ((applicationContext as RoboTeacher).board!!.lowercase().equals("icse")) {
//
//
//                                RetrofitInstanceBuilder.TEACHING_ASSISTANT = RetrofitInstanceBuilder.TEACHING_ASSISTANT+"icse"
//                                RetrofitInstanceBuilder.QUESTION_GENERATOR = RetrofitInstanceBuilder.TEACHING_ASSISTANT
//
//                            } else {
//
//                                RetrofitInstanceBuilder.TEACHING_ASSISTANT = "/teaching-assistant/"
//                                RetrofitInstanceBuilder.QUESTION_GENERATOR = "/question-generator/"
//                            }
//
//                            var i = Intent(this@SplashActivity,GeneralActivity::class.java)
//                            startActivity(i)
//                            finish()
//
//
//
//
//
//
//                        }
//
//
//
//
//                    }
//                }
//
//
//
//            }
//
//            override fun onError(code: Int, response: Response?) {
//
//                //Log.d(TAG, "onError: ")
//
//            }
//
//            override fun onException(error: String?) {
//
//            }
//
//
//        })
//
//        OkHttpClientInstance.post(url)

    }

    private fun getBoardData1(url: String) {

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

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val response = statusApi.getStatus()

                    //Log.d("StatusService", "Response: code=${response.code()}, success=${response.isSuccessful}")

                    if (response.isSuccessful && response.code() == 200) {
                        response.body()?.string()?.let {
                            val statusJson = JSONObject(it)
                            val status = statusJson.getString("status")
                            //statusCallback?.invoke(status)
                            Log.d("StatusService", "Status: $status")
                        }
                    } else {
                        //statusCallback?.invoke("down")
                        Log.w("StatusService", "Server responded with code ${response.code()}")
                    }

                } catch (ex: IOException) {
                    Log.e("StatusService", "Connection error", ex)
                    //okHttpClient.connectionPool.evictAll()
                }

                delay(5000)
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {

            if (grantResults.size > 0) {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(this)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            startActivityForResult(intent, 1234)
                        }
                    }

//                    buttonWakeUp!!.visibility = View.VISIBLE
//
//                    buttonWakeUp!!.setOnClickListener {
//
//                        buttonWakeUp!!.visibility = View.GONE
//
//                        bottomSheetRoot!!.post {
//
//                            bottomSheetWidth = bottomSheetRoot!!.width
//                            bottomSheetRoot!!.translationX =
//                                bottomSheetWidth.toFloat()
//                            bottomSheetRoot!!.visibility = View.VISIBLE
//
//                            animateBottomSheetRoot(isReset)
//                        }
//
//
//                    }


                }
            }
        }
    }

    override fun onResume() {
        super.onResume()



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1234) {
            // Give it a small delay to ensure system updates the permission state
            Handler(Looper.getMainLooper()).postDelayed({
                if (Settings.canDrawOverlays(this)) {
                    val i = Intent(this, GeneralActivity::class.java)
                    startActivity(i)
                } else {
                    Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }, 500)
        }


    }
}