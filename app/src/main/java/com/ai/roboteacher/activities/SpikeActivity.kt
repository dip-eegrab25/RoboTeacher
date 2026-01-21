package com.ai.roboteacher.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.style.CharacterStyle
import android.util.Log
import android.view.GestureDetector
import android.view.View
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ai.roboteacher.Api.Api
import com.ai.roboteacher.CustomSpikes
import com.ai.roboteacher.Direction

import com.ai.roboteacher.Models.AssignmentResponse
import com.ai.roboteacher.Models.ClassResponse
import com.ai.roboteacher.Models.SchoolData
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.RoboTeacher
import com.ai.roboteacher.SwipeGestureListener
import com.example.myapplication.PasswordView
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import okhttp3.internal.and
import retrofit2.Response
import java.io.IOException
import java.io.Serializable
import java.nio.charset.Charset

class SpikeActivity:AppCompatActivity() {

    var textView:TextView? = null
    lateinit var btnEnter:TextView
    lateinit var spikesView:CustomSpikes
    var assignmentData:AssignmentResponse.AssignmentData?=null
    var mainLayout:View?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode()
        setContentView(R.layout.activity_spikes)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        val pandaIcon: ImageView = findViewById(R.id.yellow_panda_menu)
        pandaIcon.setOnClickListener {

            finish()

        }

        btnEnter = findViewById(R.id.submit)
        spikesView = findViewById(R.id.spikes_view)
        mainLayout = findViewById(R.id.main)
//        val gestureDetector: GestureDetector = GestureDetector(this,
//            SwipeGestureListener({ direction, velocityX, velocityY ->
//
//                when(direction) {
//
//                    Direction.RIGHT-> {
//
//                        //finish()
//
//                    val intent = Intent(this, GeneralActivity::class.java)
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//
//                        finish()
//
//                    }
//
//                    Direction.TAP_UP->{
//
//                        //hideKeyBoard()
//
//                        //Toast.makeText(this@ChatActivity,"Tapped",Toast.LENGTH_SHORT).show()
//
//                    }
//
//                    else-> {
//
//
//                    }
//
//
//                }
//            })
//        )

        val layoutBack: LinearLayout = findViewById(R.id.back_layout)
        val passwordView:PasswordView = findViewById(R.id.pin_view)

        mainLayout!!.setOnClickListener {

            hideKeyBoard()
        }


//        layoutBack!!.setOnTouchListener({_,event->
//
//            gestureDetector.onTouchEvent(event)
//            true
//        })

        btnEnter.setOnClickListener {

            val userPin:String? = passwordView.getOtp()

            if (userPin == null) {

                showServiceAlert(getString(R.string.please_enter_otp))
//                Toast.makeText(this@SpikeActivity
//                    ,"Invalid pin"
//                    ,Toast.LENGTH_SHORT)
//                    .show()
            } else {

                Log.d(SpikeActivity::class.java.name, "Pin: " + userPin)

                getData(OkHttpClientInstance.otpDataUrl+"?otp=${userPin}")
            }

//            var intent = Intent(this@SpikeActivity,SelectClassActivity::class.java)
//            startActivity(intent)
        }

        //textView = findViewById<TextView>(R.id.text_view)

        //getData()
    }



    private fun showServiceAlert(msg:String) {


        runOnUiThread{

            var aBuilder = AlertDialog.Builder(this@SpikeActivity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Alert")
                .setMessage("Invalid Pin")
                .setPositiveButton("Ok",object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        dialog!!.dismiss()
                    }

                })

            var alertDialog = aBuilder.create()
            alertDialog.show()


        }






    }

    private fun enterFullScreenMode() {


        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R) {

            window.insetsController?.apply {

                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {


            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
        }

    }

    private fun getData(url:String) {



        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: okhttp3.Response?) {


                response?.let {resp->

                    response.body?.let {


                        val gson: Gson = Gson()
                        var clsResp = gson.fromJson(it.string(), AssignmentResponse::class.java)

                        clsResp?.let { data->

                            assignmentData = data.data

                            var intent = Intent(this@SpikeActivity,SelectClassActivity2::class.java)
                            intent.putExtra("data",assignmentData as Serializable)
                            startActivity(intent)

                            getBoardData(OkHttpClientInstance.schoolList)




                            Log.d(SpikeActivity::class.java.name, "onResult: ${data.message}")

                        }




                    }

                }


            }

            override fun onError(code: Int, response: okhttp3.Response?) {

                runOnUiThread {

                    showServiceAlert(getString(R.string.invalid_pin))

                    //Toast.makeText(this@SpikeActivity,"Error",Toast.LENGTH_SHORT).show()

                }
                

            }

            override fun onException(error: String?) {

                runOnUiThread {

                    showServiceAlert(getString(R.string.error_login))

                    //Toast.makeText(this@SpikeActivity,error,Toast.LENGTH_SHORT).show()

                }



            }


        })

        OkHttpClientInstance.get(url)





    }

    private suspend fun parseBytes(byteArray: ByteArray)  {

        var i = 0
        var codePoint = 0
        var numBytes = 0
        var sb:StringBuilder = StringBuilder()

        while (i<byteArray.size) {

            var unsignedFirst = byteArray[i].toInt() and (0xFF)

            if (unsignedFirst shr (7) == 0b0) {

                codePoint = unsignedFirst
                numBytes = 1
                i+=numBytes

            } else if (unsignedFirst shr (5) == 0b110) {

                var second = byteArray[i+1].toInt() and (0x3F)

                codePoint = ((unsignedFirst and (0x1F)) shl (6)) or (second)
                numBytes = 2
                i+=numBytes

            } else if (unsignedFirst shr (4) == 0b1110) {

                var second = byteArray[i+1].toInt() and (0x3F)
                var third = byteArray[i+2].toInt() and (0x3F)

                codePoint = ((unsignedFirst and (0x0F)) shl (12)) or
                        (second shl (6)) or
                        third


                numBytes = 3
                i+=numBytes

            } else if (unsignedFirst shr (3) == 0b11110) {

                var second = byteArray[i+1].toInt() and (0x3F)
                var third = byteArray[i+2].toInt() and (0x3F)
                var fourth = byteArray[i+3].toInt() and (0x3F)

                codePoint = ((unsignedFirst and (0x07)) shl (18)) or
                        (second shl (12)) or
                        (third shl (6)) or
                        fourth


                numBytes = 4
                i+=numBytes


            } else {

                throw IOException("InValid Encoding")
            }

                sb.append(Character.toChars(codePoint))

            runOnUiThread {

                textView?.setText(sb)

            }

            delay(50)


        }







        //Log.d(MainActivity::class.java.name, "parseBytes: " + sb.toString())


    }

//    private fun parseResponse(byteArray: ByteArray) {
//
//        var sBuilder:StringBuilder = StringBuilder()
//
//        var byteCount = 0
//        var codePoint = 0
//        var i = 0
//
//        while (i<byteArray.size) {
//
//            var unsignedfirst = byteArray[i].toInt() and(0xFF)
//
//            if (unsignedfirst.shr(7) == 0b0) {
//
//                byteCount = 1
//                codePoint = unsignedfirst
//
//            } else if (unsignedfirst.shr(5) == 0b110) {
//
//                var second = byteArray[i+1].toInt() and(0x3F)
//                codePoint = (unsignedfirst and(0x1f)).shl(6).or(second)
//                byteCount = 2
//
//
//            } else if (unsignedfirst.shr(4) == 0b1110) {
//
//                var second =  byteArray[i+1].toInt() and(0x3F)
//                var third = byteArray[i+2].toInt() and(0x3F)
//
//                codePoint = (unsignedfirst and (0x0F)).shl(12)
//                    .or(second.shl(6))
//                    .or(third)
//
//                byteCount = 3
//
//            } else if (unsignedfirst.shr(3) == 0b11110) {
//
//                var second =  byteArray[i+1].toInt() and(0x3F)
//                var third = byteArray[i+2].toInt() and(0x3F)
//                var fourth = byteArray[i+3].toInt() and(0x3F)
//
//                codePoint = (unsignedfirst and (0x0F)).shl(18)
//                    .or(second.shl(12))
//                    .or(third.shl(6)
//                        .or(fourth))
//
//                byteCount = 4
//
//
//            } else {
//
//                throw IllegalStateException("Invalid Encoding")
//            }
//
//            i+=byteCount
//
//            sBuilder.append(Character.toChars(codePoint))
//
//
//
//
//        }
//
//
//
//        Log.d(SpikeActivity::class.java.name, "parseResponse: " + sBuilder.toString())
//
//
//
//
//
//
//    }

    override fun onPause() {
        super.onPause()

        spikesView.pauseAnim()
    }

    override fun onResume() {
        super.onResume()

        spikesView.resumeAnim()


    }

    private fun getBoardData(url: String) {

        OkHttpClientInstance.getInstance(object: OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: okhttp3.Response?) {

                Log.d("abcde", "Code: ${code}")

                response?.let {

                    if (response.isSuccessful && response.code==200) {

                        response.body?.let {

                            val gson = Gson()

                            val schoolData: SchoolData = gson.fromJson(it.string(), SchoolData::class.java)

                            (applicationContext as RoboTeacher).board = schoolData.data.get(0).board


                            if ((applicationContext as RoboTeacher).board!!.lowercase().equals("icse")) {

                                if (!RetrofitInstanceBuilder.TEACHING_ASSISTANT.contains("icse")) {

                                    RetrofitInstanceBuilder.TEACHING_ASSISTANT = RetrofitInstanceBuilder.TEACHING_ASSISTANT+"icse"


                                }


                                //RetrofitInstanceBuilder.TEACHING_ASSISTANT = RetrofitInstanceBuilder.TEACHING_ASSISTANT+"icse"
                                RetrofitInstanceBuilder.QUESTION_GENERATOR = RetrofitInstanceBuilder.TEACHING_ASSISTANT

                            } else {

                                RetrofitInstanceBuilder.TEACHING_ASSISTANT = "/teaching-assistant/"
                                RetrofitInstanceBuilder.QUESTION_GENERATOR = "/question-generator/"
                            }

                            var intent = Intent(this@SpikeActivity,SelectClassActivity2::class.java)
                            intent.putExtra("data",assignmentData as Serializable)
                            startActivity(intent)






                        }




                    }
                }

            }

            override fun onError(code: Int, response: okhttp3.Response?) {

            }

            override fun onException(error: String?) {

            }


        })

        OkHttpClientInstance.post(url)

    }

    private fun hideKeyBoard() {

        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(mainLayout?.windowToken,0)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {


    }


}