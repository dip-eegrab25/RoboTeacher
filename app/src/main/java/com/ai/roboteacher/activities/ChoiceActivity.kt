package com.ai.roboteacher.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.QuestionDataReceiver
import com.ai.roboteacher.R
import com.ai.roboteacher.services.QuestionGeneratorService
import kotlinx.coroutines.launch
import okhttp3.Response
import org.json.JSONObject

class ChoiceActivity:AppCompatActivity() {

    var choiceArr:Array<String> = arrayOf("Study","Question Generator","Attendance","Others")
    var allMenu:ImageView?=null
    var bundle:Bundle?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode(window)
        setContentView(R.layout.activity_choice)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)


            insets
        }

        bundle = intent.extras

        allMenu = findViewById(R.id.all_menu)

        val subHeaderLabel:TextView = findViewById(R.id.sub_header_label)

        bundle?.let {

            subHeaderLabel.setText("Ready to read ${bundle!!.getString("subject")}")

        }


        allMenu!!.setOnClickListener {

            buildPopUpMenu()
        }

        var ll:LinearLayout = findViewById(R.id.choice_holder)

        for (i in 0 until ll.childCount) {

            var spStr:SpannableString = SpannableString(choiceArr[i])
            spStr.setSpan(object:ClickableSpan(){
                override fun onClick(widget: View) {

                    if(i == 0) {

                        if (!QuestionDataReceiver.isRunning.get()) {

                            val mintent:Intent = Intent(this@ChoiceActivity,ChatActivity::class.java)
                            mintent.putExtras(bundle!!)
                            startActivity(mintent)


                        } else {

                            Toast.makeText(this@ChoiceActivity,"Question Generator is already running",Toast.LENGTH_SHORT).show()
                        }

//                        val mintent:Intent = Intent(this@ChoiceActivity,ChatActivity::class.java)
//                        mintent.putExtras(bundle!!)
//                        startActivity(mintent)

                        //Toast.makeText(this@ChoiceActivity,"Study",Toast.LENGTH_SHORT).show()
                    } else if (i == 1) {

                        if (!QuestionDataReceiver.isRunning.get()) {

                            val mintent:Intent = Intent(this@ChoiceActivity,QuestionGeneratorActivity::class.java)
                            mintent.putExtras(bundle!!)
                            startActivity(mintent)


                        } else {

                            Toast.makeText(this@ChoiceActivity,"Question Generator is already running",Toast.LENGTH_SHORT).show()
                        }



                    } else if (i==2) {

                        startAttendance(bundle!!,"start")
                    }


                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)

                    if (i == 2) {

                        Thread(object : Runnable{
                            override fun run() {
                                if (QuestionGeneratorService.isRunning) {

                                    runOnUiThread {

                                        ds.color = Color.LTGRAY

                                    }



                                } else {

                                    runOnUiThread {

                                        ds.color = Color.BLUE

                                    }

                                }
                            }


                        }).start()


                    }

//                    lifecycleScope.launch {
//
//                        while (true) {
//
//                            if (QuestionGeneratorService.isRunning) {
//
//                                runOnUiThread {
//
//                                    ds.color = Color.LTGRAY
//
//                                }
//
//
//
//                            } else {
//
//                                runOnUiThread {
//
//                                    ds.color = Color.BLUE
//
//                                }
//
//                            }
//                        }
//                    }
                }
            },0,choiceArr[i].length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

            (ll.getChildAt(i) as ConstraintLayout).findViewById<TextView>(R.id.text_choice).apply {

                text = spStr
                movementMethod = LinkMovementMethod.getInstance()
                //highlightColor = Color.BLUE

            }


        }

        findViewById<ImageView>(R.id.yellow_panda_menu).setOnClickListener {

//            var i = Intent(this@ChoiceActivity,SelectClassActivity::class.java)
//            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(i)

            finish()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun buildPopUpMenu() {

        var pMenu = PopupMenu(this,allMenu)
        pMenu.menuInflater.inflate(R.menu.all_menu,pMenu.menu)
        pMenu.show()

        pMenu.setOnMenuItemClickListener {

            when(it.itemId) {

                R.id.pdf->{

                    var intent = Intent(this,PdfListActivity::class.java)
                    startActivity(intent)

                    true
                }

                else->{

                    true

                }
            }
        }
    }

    private fun enterFullScreenMode(window: Window) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

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

    private fun startAttendance(bundle: Bundle,type:String) {

        Log.d("abcde", "startAttendance: ")

        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: Response?) {

                response?.let {

                    if (response.isSuccessful && code == 200) {

                        response.body.let {

                            var json:JSONObject = JSONObject(it!!.string())

                            Log.d("abcde", json.getString("status"))

                            if (json.getString("status").equals("started")) {

                                var intent = Intent(this@ChoiceActivity,AttendanceActivity::class.java)
                                intent.putExtras(bundle)
                                startActivity(intent)


                            } else if (json.getString("status").equals("stopped")) {

                                finish()


                            } else {

                                runOnUiThread {

                                    Toast.makeText(this@ChoiceActivity,json.getString("status").capitalize(),Toast.LENGTH_SHORT).show()
                                }


                            }

                        }
                    } else {

                        runOnUiThread {

                            Toast.makeText(this@ChoiceActivity,"Error",Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

            override fun onError(code: Int, response: Response?) {

                Log.d("abcde", "onError: "+response?.message)

                runOnUiThread {

                    Toast.makeText(this@ChoiceActivity,"Error:${response?.message}",Toast.LENGTH_SHORT).show()
                }


            }

            override fun onException(error: String?) {

                Log.d("abcde", "Exception: "+error)

                runOnUiThread {

                    Toast.makeText(this@ChoiceActivity,"Exception:${error}",Toast.LENGTH_SHORT).show()
                }

            }


        })

        val m = mutableMapOf<String,String>()
        m.put("class_number",intent!!.extras!!.getString("class",""))

        if (type.equals("start")) {

            OkHttpClientInstance.postJson(OkHttpClientInstance.ATTBASEURL+OkHttpClientInstance.startAtt,m)

        } else if (type.equals("stop")) {

            OkHttpClientInstance.postJson(OkHttpClientInstance.ATTBASEURL+OkHttpClientInstance.stopAtt,m)

        }

    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }


}