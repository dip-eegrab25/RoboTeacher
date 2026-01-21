package com.ai.roboteacher.activities

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorStateListDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler

import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.GestureDetector
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addPauseListener

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.contains

import androidx.lifecycle.lifecycleScope
import com.ai.roboteacher.Direction
import com.ai.roboteacher.Models.AssignmentResponse

import com.ai.roboteacher.Models.ClassResponse
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.RoboTeacher
import com.ai.roboteacher.SpeechRecognizerInstance
import com.ai.roboteacher.SwipeGestureListener
import com.anim.spectrumprogress.Spectrum
import com.google.gson.Gson
import io.ktor.util.Hash
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.Response

import kotlin.math.abs



class SelectClassActivity2:AppCompatActivity() {

    private val RECORD_AUDIO_PERMISSION_CODE: Int = 1
    private val SPEECH_CODE = 100
    private val indexArr = arrayListOf<Int>()
    private var apiCount = 0
    private val apiArr = ArrayList<String>()
    private val apiMap = HashMap<Int,String>()
    private var v:String?=null

    private var classItemList:ArrayList<ClassResponse.ClassItem>?=null

    val textViewArrs:ArrayList<TextView> = arrayListOf()
    var viewArr:ArrayList<View> = arrayListOf()
    var i = 0


    var micToggle:FrameLayout?=null
    var classLabelArr:ArrayList<String>?= ArrayList()

    lateinit var llRight:LinearLayout
    lateinit var llLeft:LinearLayout
    lateinit var objectAnimatorScale:ObjectAnimator
    lateinit var objectAnimatorTranslate:ObjectAnimator
    var isOpen:Boolean = false
    var booleanIsComplete:Boolean = false
    var isReset:Boolean = false

    var subCount = 0

    var classText:String?=null
    var subText:String?=null
    var mainLayout:ConstraintLayout?=null

    var isErrorDataFetch:Boolean = false
    var isAnimFlag:Boolean = false
    var dataIntent:Intent?=null
    var classApiData:AssignmentResponse.AssignmentData?=null
    var isFlow:Boolean = false
    var flowMap:MutableMap<String,Boolean> = mutableMapOf()
    private var animatorr:MyAnimator?=null
    //private var listener:MyAnimator?=null

    var leftCount = 0
    var rightCount = 0
    var classId = 0
    var subId = 0
    var isSubject:Boolean = false
    var isClass = false
    var isTopic = false
    var buttonBack:ImageView?=null


    private var API_COUNT = 3
    private var apiCounter = 0
//    private var apiArr:Array<String> = Array<String>{
//
//        "classes",
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode()
        setContentView(R.layout.activity_select_class)

        flowMap.put("isClass",false)
        flowMap.put("isSubject",false)

        dataIntent = getIntent()

        classApiData = dataIntent?.getSerializableExtra("data") as AssignmentResponse.AssignmentData


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        buttonBack = findViewById(R.id.button_back)
        val buttonSignOut:TextView = findViewById(R.id.btn_sign_out)

        buttonSignOut.setOnClickListener {

            var mainIntent = Intent(this,GeneralActivity::class.java)
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(mainIntent)

            finish()


        }

        buttonBack!!.visibility = View.INVISIBLE


        buttonBack!!.setOnClickListener {

            isBack = true

            apiCount--

            if (apiMap.containsKey(apiCount)) {

                toggleTextViewVisibility(false)
                getData1(apiMap.get(apiCount)!!)

            } else {

//                var mainIntent = Intent()
//
//                finish()
            }


        }

//        val gestureDetector: GestureDetector = GestureDetector(this,
//            SwipeGestureListener({ direction, velocityX, velocityY ->
//
//                when(direction) {
//
//                    Direction.RIGHT-> {
//
//                        isBack = true
//
//                        apiCount--
//
//                        if (apiMap.containsKey(apiCount)) {
//
//                            toggleTextViewVisibility(false)
//                            getData1(apiMap.get(apiCount)!!)
//
//                        } else {
//
//                            finish()
//                        }
//
//
//
//                        //recognitionListener = null
//
//
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
//
//        val layoutBack:LinearLayout = findViewById(R.id.back_layout)


//        layoutBack!!.setOnTouchListener({_,event->
//
//            gestureDetector.onTouchEvent(event)
//            true
//        })

        var eye1:ImageView = findViewById(R.id.eye_1)
        var eye2:ImageView = findViewById(R.id.eye_2)
        var smile:ImageView = findViewById(R.id.smile)
        val animatedImageBar: ImageView = findViewById(R.id.image_bar)

        micToggle = findViewById(R.id.microphone_toggle)


        micToggle?.setOnClickListener {

            if (!micToggle!!.isSelected) {

                if (SpeechRecognizer.isRecognitionAvailable(this@SelectClassActivity2)) {

                    checkAudioPermissionAndStart()

                } else {

                    micToggle!!.isSelected = false
                    //showAlertPromptForGoogleApp()

                }


            } else {

                micToggle!!.isSelected = false
                SpeechRecognizerInstance.destroyInstance()
            }

        }

        llRight = findViewById(R.id.layout_right)
        llLeft = findViewById(R.id.layout_left)

        val eye1Drawable:Drawable = eye1.drawable
        val eye2Drawable:Drawable = eye2.drawable
        val smileDrawable:Drawable = smile.drawable
        val animatedImageBarDrawable: Drawable = animatedImageBar.drawable

        if (eye1Drawable is AnimatedVectorDrawable) {

            eye1Drawable.start()
        }

        if (eye2Drawable is AnimatedVectorDrawable) {

            eye2Drawable.start()
        }

        if (smileDrawable is AnimatedVectorDrawable) {

            smileDrawable.start()
        }

        if (animatedImageBarDrawable is AnimatedVectorDrawable) {

            animatedImageBarDrawable.start()
        }

        (llLeft.getChildAt(0) as ConstraintLayout).post {

            viewWidth = llLeft.getChildAt(0).width
            pViewWidth = (viewWidth* 0.65f).toInt()

            Log.d(SelectClassActivity2::class.java.name, "onCreate: " + viewWidth+" "+pViewWidth)

            apiCount++

            apiMap.put(apiCount,OkHttpClientInstance.classURL)

            //apiArr.add(OkHttpClientInstance.classURL)

            getData1(apiMap.get(apiCount)!!)

            //fetchSubjects("classes")

        }

    }

    var isIntentInvoked = false

    override fun onResume() {
        super.onResume()

//        if (isIntentInvoked) {
//
//            isBack = true
//
//            getData1(OkHttpClientInstance.subURL+"class_id=${classId}")
//
//        }

    }

    override fun onPause() {
        super.onPause()

        SpeechRecognizerInstance.destroyInstance()
    }

    override fun onStop() {
        super.onStop()

        SpeechRecognizerInstance.destroyInstance()
    }

    override fun onDestroy() {
        super.onDestroy()

        SpeechRecognizerInstance.destroyInstance()

    }


    private fun showAlertPromptForGoogleApp() {

        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_android_black_24dp)
            .setTitle("App not installed")
            .setCancelable(false)
            .setMessage("Google App needs to be installed for voice mode. Do you want to install it now?")
            .setPositiveButton("Ok",{ dialogInterface, i ->

                dialogInterface.dismiss()
                installGoogleApp()

            })
            .setNegativeButton("Cancel",{dialogInterface,i:Int->

                dialogInterface.dismiss()


            })
            .create()
            .show()
    }

    private fun installGoogleApp() {

        var intent:Intent = Intent(Intent.ACTION_VIEW).apply {

            data = Uri.parse("market://details?id=com.google.android.googlequicksearchbox")
            setPackage("com.android.vending")
        }

        try {

            startActivity(intent)

        } catch (ex: ActivityNotFoundException) {

            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox")
            }
            startActivity(webIntent)
        }
    }

    lateinit var valueAnim: ValueAnimator
    lateinit var alphaAnim: ValueAnimator

    private fun enterFullScreenMode() {


        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.R) {

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

    private fun disableAllSelectionText() {

        textViewArrs.forEach {

            it.isSelected = false
        }

    }

    private fun checkAudioPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            startSpeechRecognizer() // Your method to launch recognizer
        }
    }

    var vStrBuilder = StringBuilder()

    private fun startSpeechRecognizer() {

        micToggle!!.isSelected = true

            SpeechRecognizerInstance.getInstance(this,object : RecognitionListener{
                override fun onReadyForSpeech(params: Bundle?) {

//                    Toast.makeText(this@SelectClassActivity2
//                        ,"Speak Now..."
//                        ,Toast.LENGTH_SHORT).show()

                }

                override fun onBeginningOfSpeech() {

                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onEndOfSpeech() {

                    Handler().postDelayed({

                        micToggle!!.isSelected = false
                        SpeechRecognizerInstance.destroyInstance()
                    },2000)

                }

                override fun onError(error: Int) {

                    Toast.makeText(this@SelectClassActivity2,"Error",Toast.LENGTH_SHORT).show()

                    Log.d(SelectClassActivity::class.java.name, "onError: ${error}")

                    SpeechRecognizerInstance.destroyInstance()
                    micToggle!!.isSelected = false

                    //speechRecognizer.stopListening()

                }

                override fun onResults(results: Bundle?) {

                    Log.d(SelectClassActivity2::class.java.name, "onResults: ")

//                    var speechResultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                    Log.d(SelectClassActivity::class.java.name, "Results: ${speechResultList?.get(0)}")
//
//                    speechResultList?.let {
//
//
//                        if (classLabelArr!!.contains(speechResultList.get(0).lowercase())
//                        ) {
//
//                            //apiCount++
//
////                            subCount++
//                            isReset = false
//
//                            //var mIndex = classLabelArr!!.indexOf(speechResultList?.get(0))
//                            var index = classLabelArr!!.indexOf(speechResultList?.get(0)?.lowercase())
//
//                            if (index!=-1) {
//
//                                if (classLabelArr!!.get(index).lowercase().equals(v)) {
//
//                                    if (apiCount == 1) {
//
//                                        classId = dataList!!.get(index).id
//
//                                        Log.d(SelectClassActivity2::class.java.name, "ClassId "+classId)
//
//                                    } else if (apiCount == 2) {
//
//                                        subId = dataList!!.get(index).id
//                                        subText = dataList!!.get(index).name
//
//
//                                    }
//
//                                    toggleClick()
//
//
//                                }
//
//
//
//                            }
//
//
//
////                            if (mIndex != -1 || sIndex!=-1) {
////
////                                var index = -1
////
////                                if (mIndex != -1) {
////
////                                    index = mIndex
////
////                                } else {
////
////                                    index = sIndex
////                                }
////
////
////
////                                disableAllSelectionText()
////
////
////                                textViewArrs.get(index).isSelected = true
////
////                                Log.d(SelectClassActivity2::class.java.name, "Index: "+index)
////
////                                toggleClick()
////
////                                //toggleTextViewVisibility(false)
////
////
////
//////                                if (indexArr.contains(classApiData?.`class`?.id)) {
//////
//////                                    flowMap.put("isClass",true)
//////
//////                                } else if (indexArr.contains(classApiData?.subject?.id)) {
//////
//////                                    flowMap.put("isSubject",true)
//////                                }
////
////
//////                            if (subCount < 2) {
//////
//////                                classText = textViewArrs.get(index).text.toString()
//////
//////                            } else {
//////
//////                                subText = textViewArrs.get(index).text.toString()
//////
//////                            }
//////
//////
//////
//////                            Handler().postDelayed({
//////
//////                                valueAnim.start()
//////
//////                            }, 1000)
////
//////                                if (!isSubject) {
//////
//////                                    isSubject = true
//////
//////                                    classText = textViewArrs[index].text.toString()
//////                                    classId = classItemList?.get(index)?.id!!
//////
//////                                    if (indexArr.contains(classId)) {
//////
//////                                        flowMap.put("isSubject",true)
//////                                    }
//////
//////                                } else {
//////
//////                                    subText = textViewArrs[index].text.toString()
//////                                    subId = classItemList?.get(index)?.id!!
//////
//////                                    if (indexArr.contains(subId)) {
//////
//////                                        flowMap.put("isSubject",true)
//////                                    }
//////
//////
//////                                }
////
//////                                Handler().postDelayed({
//////
//////                                    if (subCount>1) {
//////
//////                                        valueAnim.resume()
//////
//////                                    } else {
//////
//////                                        valueAnim.start()
//////                                    }
//////
////////
//////
//////
//////                                },1000)
////
////
////                            } else {
////
////                                Toast.makeText(
////                                    this@SelectClassActivity2, "No class found", Toast.LENGTH_SHORT
////                                ).show()
////
////                            }
//
//                        }
//                    }


                }

                override fun onPartialResults(partialResults: Bundle?) {



//                    var speechResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                    var speechResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)


                    speechResultList.let {

                        if (speechResultList!!.isNotEmpty()) {

                            vStrBuilder.clear()

                            vStrBuilder.append(speechResultList.firstOrNull())

                            Log.d(SelectClassActivity2::class.java.name, "onPartialResults: "+ vStrBuilder.toString())



                            if (classLabelArr!!.contains(vStrBuilder.toString().trim().lowercase())
                                || mClassLabelArr.contains(vStrBuilder.toString().trim().lowercase())) {

                                Log.d(SelectClassActivity2::class.java.name, "Contains")


                                isReset = false

                                //var mIndex = classLabelArr!!.indexOf(speechResultList?.get(0))
                                var index = classLabelArr!!.indexOf(vStrBuilder.toString().lowercase())
                                var mIndex = mClassLabelArr.indexOf(vStrBuilder.toString().lowercase())

                                if (index!=-1 || mIndex!=-1) {

                                    var indx = if (index!=-1) {

                                         index

                                    } else if (mIndex!=-1) {

                                         mIndex

                                    } else {

                                        0
                                    }

                                    if (classLabelArr!!.get(indx).lowercase().equals(v)
                                        || mClassLabelArr!!.get(indx).lowercase().equals(v)) {

                                        micToggle!!.isSelected = false
                                        SpeechRecognizerInstance.destroyInstance()

                                        if (apiCount == 1) {

                                            classId = dataList!!.get(indx).id

                                            Log.d(SelectClassActivity2::class.java.name, "ClassId "+classId)

                                        } else if (apiCount == 2) {

                                            subId = dataList!!.get(indx).id
                                            subText = dataList!!.get(indx).name


                                        }

                                        toggleClick()

                                    } else {

                                        micToggle!!.isSelected = false
                                        SpeechRecognizerInstance.destroyInstance()

                                    }

                                } else {

                                    //micToggle!!.isSelected = false
                                    //SpeechRecognizerInstance.destroyInstance()
                                }

                            } else {

                                //micToggle!!.isSelected = false
                                //SpeechRecognizerInstance.destroyInstance()
                            }


                        } else {

                            //micToggle!!.isSelected = false
                            //SpeechRecognizerInstance.destroyInstance()
                        }
                    }

                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }


            }
            ,2000)

        SpeechRecognizerInstance.startSpeech()



    }

    var viewWidth:Int = 0
    var pViewWidth:Int = 0
    var transDiff:Int = 0


    private fun initAnims() {

        viewWidth = textViewArrs[0].width.toInt()
        pViewWidth = (textViewArrs[0].width*0.65f).toInt()

        valueAnim = ValueAnimator.ofInt(viewWidth
            , pViewWidth,viewWidth)

        valueAnim.interpolator = LinearInterpolator()
        valueAnim.repeatCount = ValueAnimator.INFINITE


        valueAnim.duration = 800
        valueAnim.addUpdateListener {

            transDiff = Math.abs(viewWidth - it.getAnimatedValue() as Int)

            leftCount = 0
            rightCount = 0

            for (i in 0 until textViewArrs.size) {

                if (i%2 == 0) {

                    runOnUiThread {

                        val index = leftCount++

                        //llLeft.getChildAt(index).translationX = transDiff

                        if (!isReset) {

                            llLeft.getChildAt(index).translationX = transDiff.toFloat()

                        } else {

                            llLeft.getChildAt(index).translationX = -transDiff.toFloat()

                        }


                    }


                } else {

                    runOnUiThread {

                        val index = rightCount++

                        //llRight.getChildAt(index).translationX = -transDiff

                        if (!isReset) {

                            llRight.getChildAt(index).translationX = -transDiff.toFloat()

                        } else {

                            llRight.getChildAt(index).translationX = transDiff.toFloat()

                        }


                    }


                }
            }




                if ((it.animatedValue as Int) == pViewWidth
                    || (it.animatedValue as Int) == viewWidth) {

                    isReset = !isReset
                    valueAnim.pause()
                }






//

        }

    }

    private fun fetchSubjects(value:String) {

        leftCount = 0
        rightCount = 0
        textViewArrs.clear()

        val topics:Array<String> = arrayOf<String>(
            "a",
            "b",
            "c",
            "d"

        )

        //isReset = true

        val classes:Array<String> = arrayOf<String>(
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8"
        )

        val subjects:Array<String> = arrayOf<String>(
            "arts",
            "hindi",
            "physical_education",
            "social_science",
            "english",
            "mathematics",
            "science",
            "vocational_education"
        )

        if (value == "subjects") {

            for (i in subjects.indices) {

                if (i%2 == 0) {

                    (llLeft.getChildAt(leftCount) as ConstraintLayout).apply {

                        textViewArrs.add(findViewById<TextView>(R.id.class_text).apply {

                            text = subjects.get(i)

                        })
                        isClickable = true
                        leftCount++
                        setOnClickListener {

                            subCount++

                            if (!animatorr!!.isStarted) {

                                animatorr!!.start()

                            } else {

                                Log.d(SelectClassActivity2::class.java.name, "Resume")

                                animatorr!!.resume()
                            }


                        }


                    }


                } else {

                    (llRight.getChildAt(rightCount) as ConstraintLayout)
                        .apply {

                            textViewArrs.add(findViewById<TextView>(R.id.class_text).apply {

                                text = subjects.get(i)

                            })


                        rightCount++
                        isClickable = true
                        setOnClickListener {

                            if (!animatorr!!.isStarted) {

                                animatorr!!.start()

                            } else {

                                toggleTextViewVisibility(true)

                                animatorr!!.resume()
                            }


                        }

                    }

                }
            }



        } else if (value == "classes") {

            for (i in classes.indices) {

                if (i%2 == 0) {

                    (llLeft.getChildAt(leftCount) as ConstraintLayout)
                        .apply {

                            textViewArrs.add(findViewById<TextView>(R.id.class_text).apply {

                                text = classes.get(i)

                            })

                        leftCount++
                            isClickable = true
                        setOnClickListener {

                            Log.d(SelectClassActivity2::class.java.name, "Clicked")

                            subCount++

                            if (!animatorr!!.isRunning) {

                                animatorr!!.start()

                            } else {

                                animatorr!!.resume()
                            }


                        }


                    }


                } else {

                    (llRight.getChildAt(rightCount) as ConstraintLayout)
                        .apply {

                            textViewArrs.add(findViewById<TextView>(R.id.class_text).apply {

                                text = classes.get(i)

                            })

                        rightCount++
                            isClickable = true
                        setOnClickListener {

                            if (!animatorr!!.isStarted) {

                                animatorr!!.start()

                            } else {

                                toggleTextViewVisibility(true)

                                animatorr!!.resume()
                            }


                        }

                    }

                }
            }

        } else if (value == "topics") {

            for (i in topics.indices) {

                if (i%2 == 0) {

                    (llLeft.getChildAt(leftCount) as ConstraintLayout)
                        .apply {

                            textViewArrs.add(findViewById<TextView>(R.id.class_text).apply {

                                text = topics.get(i)

                            })

                            leftCount++
                            isClickable = true
                            setOnClickListener {

                                Log.d(SelectClassActivity2::class.java.name, "Clicked")

                                subCount++

                                if (!animatorr!!.isRunning) {

                                    animatorr!!.start()

                                } else {

                                    animatorr!!.resume()
                                }


                            }


                        }


                } else {

                    (llRight.getChildAt(rightCount) as ConstraintLayout)
                        .apply {

                            textViewArrs.add(findViewById<TextView>(R.id.class_text).apply {

                                text = topics.get(i)

                            })

                            rightCount++
                            isClickable = true
                            setOnClickListener {

                                if (!animatorr!!.isStarted) {

                                    animatorr!!.start()

                                } else {

                                    toggleTextViewVisibility(true)

                                    animatorr!!.resume()
                                }


                            }

                        }

                }
            }

        }
    }

    private fun goToChatActivity() {

        Log.d("abcde", "goToChatActivity: "+RetrofitInstanceBuilder.TEACHING_ASSISTANT)

        var bundle:Bundle = Bundle()

        if ((applicationContext as RoboTeacher).board.equals("icse")) {

            Log.d("abcde", "goToChatActivityyyy: "+RetrofitInstanceBuilder.TEACHING_ASSISTANT)

            if (subText!!.trim().equals("Robotics and AI")) {

                RetrofitInstanceBuilder.TEACHING_ASSISTANT = "/teaching-assistant/"

                Log.d("abcde", "goToChatActivity: "+RetrofitInstanceBuilder.TEACHING_ASSISTANT)

            } else {

                RetrofitInstanceBuilder.TEACHING_ASSISTANT = "/teaching-assistant/icse"

            }

        }


            val intent = Intent(this@SelectClassActivity2, ChoiceActivity::class.java)

            bundle.putString("class", classId.toString())
            bundle.putString("subject", subText)
            bundle.putString("subId",subId.toString())


            bundle.putString("start_time" , classApiData?.start_time)
            bundle.putString("end_time" , classApiData?.end_time)
            bundle.putString("date",classApiData?.date)
            bundle.putString("assignmentId",classApiData?.id.toString())
            bundle.putString("teacherId",classApiData?.teacher?.id.toString())
            bundle.putBoolean("isFlow",true)
            bundle.putString("teacher_name",classApiData?.teacher?.name)
            bundle.putString("subject_name",classApiData?.subject?.name)


//            intent.putExtra("class", classId.toString()) //5
//            intent.putExtra("subject", subText) //computer

//            if (flowMap.containsValue(false)) {
//
//                bundle.putString("start_time" , "0")
//                bundle.putString("end_time" , "0")
//                bundle.putBoolean("isFlow",false)
//
//            } else {
//
//                //Log.d(SelectClassActivity::class.java.name, "goToChatActivity: " +classApiData?.teacher?.id)
//
//                bundle.putString("start_time" , classApiData?.start_time)
//                bundle.putString("end_time" , classApiData?.end_time)
//                bundle.putString("date",classApiData?.date)
//                bundle.putString("assignmentId",classApiData?.id.toString())
//                bundle.putString("teacherId",classApiData?.teacher?.id.toString())
//                bundle.putBoolean("isFlow",true)
//                bundle.putString("teacher_name",classApiData?.teacher?.name)
//                bundle.putString("subject_name",classApiData?.subject?.name)
//
//
//            }

            intent.putExtras(bundle)
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            //finish()


    }

    var isBack = false
    var dataList:ArrayList<ClassResponse.ClassItem>?=null

    private fun getData2(url:String) {

        textViewArrs.clear()
        leftCount = 0
        rightCount = 0
        classLabelArr?.clear()
        indexArr.clear()



        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: Response?) {

                isErrorDataFetch = false

                response?.let {

                    response.body?.let { responseBody ->

                        val gson:Gson = Gson()
                        var clsResp = gson.fromJson(responseBody.string(),ClassResponse::class.java)


                        clsResp.data?.let { data->

                            runOnUiThread {

                                dataList = ArrayList(data)

                                assignLabelsAndClicks(data)


                                //textViewArrs[0].animate().translationX(120f).alpha(1f)

//                                if (!isBack) {
//
//                                    if (apiCount>1) {
//
//                                        toggleTextViewVisibility(true)
//                                        animatorr!!.resume()
//
//                                        Log.d(SelectClassActivity2::class.java.name, "True and Resumed")
//
//
//                                    }
//
//
//                                } else {
//
//                                    //toggleTextViewVisibility(true)
//                                    animatorr!!.resume()
//
//
//                                }





                            }



                        }





                    }


                }




            }

            override fun onError(code: Int, response: Response?) {

                isErrorDataFetch = true

                Log.d(SelectClassActivity2::class.java.name, "onError:${code}")

                for (index in 0 until  llRight.childCount) {

                    llRight.getChildAt(index).visibility = View.INVISIBLE
                    (llRight.getChildAt(index)).findViewById<TextView>(R.id.class_text).isSelected = false
                }

                for (index in 0 until  llLeft.childCount) {

                    llLeft.getChildAt(index).visibility = View.INVISIBLE
                    (llLeft.getChildAt(index)).findViewById<TextView>(R.id.class_text).isSelected = false
                }





            }

            override fun onException(error: String?) {

                runOnUiThread {

                    Toast.makeText(this@SelectClassActivity2,error,Toast.LENGTH_SHORT).show()

                }


            }


        })

        try {

            if (url.equals(OkHttpClientInstance.classURL)) {

                OkHttpClientInstance.get("classList.php")

            } else {

                Log.d(SelectClassActivity2::class.java.name, "Url:"+ url)

                OkHttpClientInstance.get(url)

            }

        } catch (ex:Exception) {

            Toast.makeText(this@SelectClassActivity2,"Server Error",Toast.LENGTH_SHORT).show()


        }





    }

    private fun getData1(url:String) {

        textViewArrs.clear()
        leftCount = 0
        rightCount = 0
        classLabelArr?.clear()
        indexArr.clear()



        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: Response?) {

                isErrorDataFetch = false

                response?.let {

                    response.body?.let { responseBody ->

                        val gson:Gson = Gson()
                        var clsResp = gson.fromJson(responseBody.string(),ClassResponse::class.java)


                        clsResp.data?.let { data->



                            runOnUiThread {

                                if (apiCount==2) {

                                    buttonBack!!.visibility = View.VISIBLE

                                } else {

                                    buttonBack!!.visibility = View.INVISIBLE

                                }

                                dataList = ArrayList(data)

                                assignLabelsAndClicks(data)

                                if (!isBack) {

                                    if (apiCount>1) {



                                        toggleTextViewVisibility(true)

                                    }


                                } else {

                                    toggleTextViewVisibility(true)
                                    isBack = false


                                }




//                                if (!isBack) {
//
//                                    if (apiCount>1) {
//
//                                        toggleTextViewVisibility(true)
//                                        animatorr!!.resume()
//
//                                        Log.d(SelectClassActivity2::class.java.name, "True and Resumed")
//
//
//                                    }
//
//
//                                } else {
//
//                                    //toggleTextViewVisibility(true)
//                                    animatorr!!.resume()
//
//
//                                }





                            }



                        }





                    }


                }




            }

            override fun onError(code: Int, response: Response?) {

                isErrorDataFetch = true

                Log.d(SelectClassActivity2::class.java.name, "onError:${code}")

                for (index in 0 until  llRight.childCount) {

                    llRight.getChildAt(index).visibility = View.INVISIBLE
                    (llRight.getChildAt(index)).findViewById<TextView>(R.id.class_text).isSelected = false
                }

                for (index in 0 until  llLeft.childCount) {

                    llLeft.getChildAt(index).visibility = View.INVISIBLE
                    (llLeft.getChildAt(index)).findViewById<TextView>(R.id.class_text).isSelected = false
                }





            }

            override fun onException(error: String?) {

                runOnUiThread {

                    Toast.makeText(this@SelectClassActivity2,error,Toast.LENGTH_SHORT).show()

                }


            }


        })

        try {

            if (url.equals(OkHttpClientInstance.classURL)) {

                OkHttpClientInstance.get("classList.php")

            } else {

                Log.d(SelectClassActivity2::class.java.name, "Url:"+ url)

                OkHttpClientInstance.get(url)

            }

        } catch (ex:Exception) {

            Toast.makeText(this@SelectClassActivity2,"Server Error",Toast.LENGTH_SHORT).show()


        }

    }

    var mClassLabelArr = ArrayList<String>()

    private fun assignLabelsAndClicks(data: List<ClassResponse.ClassItem>) {

        leftCount = 0
        rightCount = 0

        for (i in 0 until data.size) {

            if (i%2 == 0) {

                runOnUiThread {

                        llLeft.getChildAt(leftCount).visibility = View.VISIBLE

                    (llLeft.getChildAt(leftCount) as ConstraintLayout).apply {

                        textViewArrs.add(findViewById<TextView?>(R.id.class_text).apply {

                            text = data.get(i).name
                            classLabelArr!!.add(text.trim().toString().toLowerCase())

                            if (apiCount == 1) {

                                val tClass = text.trim().substring(6)

                                Log.d(SelectClassActivity2::class.java.name, "WordClass: ${tClass}")

                                val n = when(tClass){

                                    "1"->"one"
                                    "2"->"two"
                                    "3"->"three"
                                    "4"->"four"
                                    "5"->"five"
                                    "6"->"six"
                                    "7"->"seven"
                                    "8"->"eight"


                                    else->"0"



                                }

                                mClassLabelArr.add("class ${n}")


                            }


                        })

                        if (apiCount == 1) {

                            if (data.get(i).id == classApiData!!.`class`!!.id) {
//
                                isSelected = true
                                isClickable = true
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.WHITE)
                                v = textViewArrs.get(textViewArrs.size-1).text.toString().lowercase()
                                Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicks: "+v)

                                setOnClickListener {

                                    var index = classLabelArr!!.indexOf(data.get(i).name.toLowerCase())

                                    if (index!=-1) {

                                        if (apiCount == 1) {

                                            classId = data.get(index).id

                                            Log.d(SelectClassActivity2::class.java.name, "ClassId "+classId)

                                        } else if (apiCount == 2) {

                                            subId = data.get(index).id
                                            subText = data.get(index).name


                                        }

                                    }

                                    toggleClick()
                                }

                            } else {

                                isSelected = false
                                isClickable = false
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.GRAY)

                                setOnClickListener {


                                }
                            }


                        } else if (apiCount == 2) {

                            if (data.get(i).id == classApiData!!.subject!!.id) {
//
                                isSelected = true
                                isClickable = true
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.WHITE)
                                v = textViewArrs.get(textViewArrs.size-1).text.toString().lowercase()
                                Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicks: "+v)

                                setOnClickListener {

                                    var index = classLabelArr!!.indexOf(data.get(i).name.toLowerCase())

                                    if (index!=-1) {

                                        if (apiCount == 1) {

                                            classId = data.get(index).id

                                            Log.d(SelectClassActivity2::class.java.name, "ClassId "+classId)

                                        } else if (apiCount == 2) {

                                            subId = data.get(index).id
                                            subText = data.get(index).name


                                        }

                                        toggleClick()

                                    }


                                }

                            } else {

                                isSelected = false
                                isClickable = false
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.GRAY)

                                setOnClickListener {


                                }
                            }


                        }

                        leftCount++



                    }

                }


            } else {

                runOnUiThread {



                    llRight.getChildAt(rightCount).visibility = View.VISIBLE

                    (llRight.getChildAt(rightCount) as ConstraintLayout).apply {

                        textViewArrs.add(findViewById<TextView?>(R.id.class_text).apply {

                            text = data.get(i).name

                            Log.d(SelectClassActivity2::class.java.name, "Class: "+text)
                            classLabelArr!!.add(text.toString().toLowerCase())

                            if (apiCount == 1) {

                                val tClass = text.trim().substring(6)

                                Log.d(SelectClassActivity2::class.java.name, "WordClass: ${tClass}")

                                val n = when(tClass){

                                    "1"->"one"
                                    "2"->"two"
                                    "3"->"three"
                                    "4"->"four"
                                    "5"->"five"
                                    "6"->"six"
                                    "7"->"seven"
                                    "8"->"eight"


                                    else->"0"



                                }

                                mClassLabelArr.add("class ${n}")


                            }


                        })

                        if (apiCount == 1) {

                            if (data.get(i).id == classApiData!!.`class`!!.id) {
//
                                isSelected = true
                                isClickable = true
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.WHITE)
                                v = textViewArrs.get(textViewArrs.size-1).text.toString().lowercase()
                                Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicks: "+v)

                                setOnClickListener {

                                    var index = classLabelArr!!.indexOf(data.get(i).name.toLowerCase())

                                    Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicksIndex: "+ index)

                                    if (index!=-1) {

                                        if (apiCount == 1) {

                                            classId = data.get(index).id

                                            Log.d(SelectClassActivity2::class.java.name, "ClassId "+classId)

                                        } else if (apiCount == 2) {

                                            subId = data.get(index).id
                                            subText = data.get(index).name

                                            Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicks: "+subText)


                                        }

                                    }

                                    toggleClick()
                                }

                            } else {

                                isSelected = false
                                isClickable = false
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.GRAY)
                                setOnClickListener {


                                }
                            }


                        } else if (apiCount == 2) {

                            if (data.get(i).id == classApiData!!.subject!!.id) {
//
                                isSelected = true
                                isClickable = true
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.WHITE)
                                v = textViewArrs.get(textViewArrs.size-1).text.toString().lowercase()
                                Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicks: "+v)

                                setOnClickListener {

                                    var index = classLabelArr!!.indexOf(data.get(i).name.toLowerCase())

                                    Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicks: "+ index)

                                    if (index!=-1) {

                                        if (apiCount == 1) {

                                            classId = data.get(index).id

                                            Log.d(SelectClassActivity2::class.java.name, "ClassId "+classId)

                                        } else if (apiCount == 2) {

                                            subId = data.get(index).id
                                            subText = data.get(index).name

                                            Log.d(SelectClassActivity2::class.java.name, "assignLabelsAndClicks: " + subText)


                                        }

                                    }

                                    toggleClick()
                                }

                            } else {

                                isSelected = false
                                isClickable = false
                                textViewArrs.get(textViewArrs.size-1).setTextColor(Color.GRAY)
                                setOnClickListener{


                                }
                            }


                        }

                        rightCount++

//                        setOnClickListener {
//
//                                var index = classLabelArr!!.indexOf(data.get(i).name)
//
//                            if (index!=-1) {
//
//                                if (apiCount == 1) {
//
//                                    classId = data.get(index).id
//
//                                    Log.d(SelectClassActivity2::class.java.name, "ClassId "+classId)
//
//                                } else if (apiCount == 2) {
//
//                                    subId = data.get(index).id
//                                    subText = data.get(index).name
//
//
//                                }
//
//                            }
//
//                            toggleClick()
//
//                        }

                    }

                }

            }
        }

        //toggleTextViewVisibility(true)

    }

    private fun toggleClick() {

        Log.d(SelectClassActivity2::class.java.name, "toggleClick: ")

        if (apiCount<2) {

            apiCount++

            for (index in textViewArrs.indices) {

                if (index%2 == 0) {

                    textViewArrs[index].animate()
                        .translationX(120f).alpha(0f)
                        .setDuration(600)
                        .start()

                } else {

                    textViewArrs[index].animate()
                        .translationX(-120f).alpha(0f)
                        .setDuration(600)
                        .start()


                }

                //Thread.sleep(500)

            }

            if (apiCount == 2) {

                apiMap.put(apiCount,"${OkHttpClientInstance.subURL}?class_id=${classId}")

                getData1(apiMap.get(apiCount)!!)

            }
        } else {

            goToChatActivity()
        }






    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_CODE && resultCode == RESULT_OK) {

            var speechResultList = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            var text = speechResultList?.get(0)

            Log.d(SelectClassActivity::class.java.name, "Result: " + text)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognizer()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required for speech recognition",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    private inner class MyAnimator : ValueAnimator,ValueAnimator.AnimatorUpdateListener,Animator.AnimatorPauseListener {

        constructor(vWidth:Int = 0,pWidth:Int = 0):super() {

            Log.d(SelectClassActivity2::class.java.name, "${vWidth} ${pWidth}")

            setIntValues(vWidth,pWidth)
            duration = 1000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener(this)
            addPauseListener(this)


        }

        override fun onAnimationPause(animation: Animator) {

            if (!isReset) {

                toggleTextViewVisibility(false)

            } else {

                toggleTextViewVisibility(true)


            }

            isReset = !isReset



        }

        override fun onAnimationResume(animation: Animator) {

        }

        override fun onAnimationUpdate(animation: ValueAnimator) {

            var leftCount = 0
            var rightCount = 0

            Log.d(SelectClassActivity2::class.java.name, "Value: "+animation.animatedValue as Int)

            transDiff = Math.abs(viewWidth - animation.getAnimatedValue() as Int)

            for (i in 0 until textViewArrs.size) {

                if (i%2==0) {

                    Log.d(SelectClassActivity2::class.java.name, "LeftCount: "+leftCount)


                    if (!isReset) {

                        llLeft.getChildAt(leftCount).translationX = transDiff.toFloat()

                    } else {

                        llLeft.getChildAt(leftCount).translationX = transDiff.toFloat()


                    }

                    leftCount++

                } else {

                    if (!isReset) {

                        llRight.getChildAt(rightCount).translationX = -transDiff.toFloat()

                    } else {

                        llRight.getChildAt(rightCount).translationX = -transDiff.toFloat()


                    }
                    rightCount++


                }

            }


            if (!isReset) {

                if ((animation.animatedValue as Int) < 400) {

                    animatorr!!.pause()
                }
            } else {

                if (animation.animatedValue as Int > viewWidth-30) {

                    animatorr!!.pause()
                }


            }



        }


    }

    private fun toggleTextViewVisibility(visibility: Boolean) {

        for (index in textViewArrs.indices) {

            if (index%2 == 0) {

                textViewArrs[index].animate()
                    .translationX(0f).alpha(1f)
                    .setDuration(300)
                    .start()

            } else {

                textViewArrs[index].animate()
                    .translationX(0f).alpha(1f)
                    .setDuration(300)
                    .start()

            }

        }


    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

//    private fun launchWorkerThread() {
//
//        if (!isSubject) {
//
//            fetchSubjects()
//
//
//        }
//    }



}