package com.ai.roboteacher.activities

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.media.Image
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

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get

import androidx.lifecycle.lifecycleScope
import com.ai.roboteacher.Direction
import com.ai.roboteacher.Models.AssignmentResponse

import com.ai.roboteacher.Models.ClassResponse
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.ai.roboteacher.RoboTeacher
import com.ai.roboteacher.SpeechRecognizerInstance
import com.ai.roboteacher.SwipeGestureListener
import com.google.gson.Gson
import io.ktor.util.Hash
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.Response

import kotlin.math.abs



class SelectClassActivity:AppCompatActivity() {

    private val RECORD_AUDIO_PERMISSION_CODE: Int = 1
    private val SPEECH_CODE = 100
    private val indexArr = arrayListOf<Int>()

    private var classItemList:ArrayList<ClassResponse.ClassItem>?=null

    val textViewArrs:ArrayList<TextView> = arrayListOf()
    var viewArr:ArrayList<View> = arrayListOf()
    var i = 0
    var recognitionListener:RecognitionListener?=null

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





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode()
        setContentView(R.layout.activity_select_class)

        flowMap.put("isClass",false)
        flowMap.put("isSubject",false)

        dataIntent = getIntent()

        if (dataIntent?.getSerializableExtra("data")!=null) {

            classApiData = dataIntent?.getSerializableExtra("data") as AssignmentResponse.AssignmentData

            (applicationContext as RoboTeacher).classApiData = classApiData

        } else {

            classApiData = (applicationContext as RoboTeacher).classApiData

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }


//        val gestureDetector: GestureDetector = GestureDetector(this,
//            SwipeGestureListener({ direction, velocityX, velocityY ->
//
//            when(direction) {
//
//                Direction.RIGHT-> {
//
//                    recognitionListener = null
//
//
//
//                    val intent = Intent(this, SpikeActivity::class.java)
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//
//                    finish()
//
//                }
//
//                Direction.TAP_UP->{
//
//                    //hideKeyBoard()
//
//                    //Toast.makeText(this@ChatActivity,"Tapped",Toast.LENGTH_SHORT).show()
//
//                }
//
//                else-> {
//
//
//                }
//
//
//            }
//        })
//        )
//
//        val layoutBack:LinearLayout = findViewById(R.id.back_layout)
//
//
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

            micToggle?.isSelected = true

            if (SpeechRecognizer.isRecognitionAvailable(this@SelectClassActivity)) {

                checkAudioPermissionAndStart()

            } else {

                showAlertPromptForGoogleApp()

            }

        }

        llRight = findViewById(R.id.layout_right)
        llLeft = findViewById(R.id.layout_left)

//        try {
//
//            getData(OkHttpClientInstance.classURL)
//
//        }
//
//        catch (ex:Exception) {
//
//            Toast.makeText(this,"Server Error",Toast.LENGTH_SHORT).show()
//        }




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

    }

    override fun onResume() {
        super.onResume()

        try {

            getData(OkHttpClientInstance.classURL)

        }

        catch (ex:Exception) {

            Toast.makeText(this,"Server Error",Toast.LENGTH_SHORT).show()
        }
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

    private fun startSpeechRecognizer() {

        if (recognitionListener == null) {

            recognitionListener = object : RecognitionListener{
                override fun onReadyForSpeech(params: Bundle?) {

                    Toast.makeText(this@SelectClassActivity
                        ,"Speak Now..."
                        ,Toast.LENGTH_SHORT).show()

                }

                override fun onBeginningOfSpeech() {

                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onEndOfSpeech() {

                }

                override fun onError(error: Int) {

                    Toast.makeText(this@SelectClassActivity,"Error",Toast.LENGTH_SHORT).show()

                    Log.d(SelectClassActivity::class.java.name, "onError: ${error}")

                    //speechRecognizer.stopListening()

                }

                override fun onResults(results: Bundle?) {

                    var speechResultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d(SelectClassActivity::class.java.name, "Results: ${speechResultList?.get(0)}")

                    speechResultList?.let {


                        if (classLabelArr!!.contains(speechResultList.get(0).lowercase())
                        ) {

                            subCount++
                            isReset = false

                            var mIndex = classLabelArr!!.indexOf(speechResultList?.get(0))
                            var sIndex = classLabelArr!!.indexOf(speechResultList?.get(0)?.lowercase())

                            if (mIndex != -1 || sIndex!=-1) {

                                var index = -1

                                if (mIndex != -1) {

                                    index = mIndex

                                } else {

                                    index = sIndex
                                }



                                disableAllSelectionText()


                                textViewArrs.get(index).isSelected = true

//                                if (indexArr.contains(classApiData?.`class`?.id)) {
//
//                                    flowMap.put("isClass",true)
//
//                                } else if (indexArr.contains(classApiData?.subject?.id)) {
//
//                                    flowMap.put("isSubject",true)
//                                }


//                            if (subCount < 2) {
//
//                                classText = textViewArrs.get(index).text.toString()
//
//                            } else {
//
//                                subText = textViewArrs.get(index).text.toString()
//
//                            }
//
//
//
//                            Handler().postDelayed({
//
//                                valueAnim.start()
//
//                            }, 1000)

                                if (!isSubject) {

                                    isSubject = true

                                    classText = textViewArrs[index].text.toString()
                                    classId = classItemList?.get(index)?.id!!

                                    if (indexArr.contains(classId)) {

                                        flowMap.put("isClass",true)
                                    }

                                } else {

                                    subText = textViewArrs[index].text.toString()
                                    subId = classItemList?.get(index)?.id!!

                                    if (indexArr.contains(subId)) {

                                        flowMap.put("isSubject",true)
                                    }


                                }

                                Handler().postDelayed({

                                    if (subCount>1) {

                                        valueAnim.resume()

                                    } else {

                                        valueAnim.start()
                                    }

//


                                },1000)


                            } else {

                                Toast.makeText(
                                    this@SelectClassActivity, "No class found", Toast.LENGTH_SHORT
                                ).show()

                            }

                        }
                    }

                    //speechResultList = null

                }

                override fun onPartialResults(partialResults: Bundle?) {

                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }


            }

            SpeechRecognizerInstance.getInstance(applicationContext,recognitionListener!!)
            //SpeechRecognizerInstance.setSpeechListener(recognitionListener!!)
        }

        SpeechRecognizerInstance.startSpeech()

//        if (SpeechRecognizer.isRecognitionAvailable(this@SelectClassActivity)) {
//
//            Log.d(SelectClassActivity::class.java.name, "startSpeechRecognizerAvailable")
//
//            val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
//
//        var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
//        //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)
//        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak Now...")
//
//            speechRecognizer.setRecognitionListener(
//
//                object : RecognitionListener{
//            override fun onReadyForSpeech(params: Bundle?) {
//
//                Toast.makeText(this@SelectClassActivity
//                    ,"Speak Now..."
//                ,Toast.LENGTH_SHORT).show()
//
//            }
//
//            override fun onBeginningOfSpeech() {
//
//            }
//
//            override fun onRmsChanged(rmsdB: Float) {
//
//            }
//
//            override fun onBufferReceived(buffer: ByteArray?) {
//
//            }
//
//            override fun onEndOfSpeech() {
//
//            }
//
//            override fun onError(error: Int) {
//
//                Toast.makeText(this@SelectClassActivity,"Error",Toast.LENGTH_SHORT).show()
//
//                Log.d(SelectClassActivity::class.java.name, "onError: ${error}")
//
//                //speechRecognizer.stopListening()
//
//            }
//
//            override fun onResults(results: Bundle?) {
//
//                var speechResultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                Log.d(SelectClassActivity::class.java.name, "Results: ${speechResultList?.get(0)}")
//
//                speechResultList?.let {
//
//
//
//                    if (classLabelArr!!.contains(speechResultList.get(0)) or
//                        classLabelArr!!.contains(speechResultList.get(0).lowercase())
//                    ) {
//
//                        subCount++
//                        isReset = false
//
//                        var mIndex = classLabelArr!!.indexOf(speechResultList?.get(0))
//                        var sIndex = classLabelArr!!.indexOf(speechResultList?.get(0)?.lowercase())
//
//                        if (mIndex != -1 || sIndex!=-1) {
//
//                            var index = -1
//
//                            if (mIndex != -1) {
//
//                                index = mIndex
//
//                            } else {
//
//                                index = sIndex
//                            }
//
//
//
//                            disableAllSelectionText()
//
//
//                            textViewArrs.get(index).isSelected = true
//
//
////                            if (subCount < 2) {
////
////                                classText = textViewArrs.get(index).text.toString()
////
////                            } else {
////
////                                subText = textViewArrs.get(index).text.toString()
////
////                            }
////
////
////
////                            Handler().postDelayed({
////
////                                valueAnim.start()
////
////                            }, 1000)
//
//                            if (!isSubject) {
//
//                                isSubject = true
//
//                                classText = textViewArrs[index].text.toString()
//                                classId = classItemList?.get(index)?.id!!
//
//                            } else {
//
//                                subText = textViewArrs[index].text.toString()
//
//                            }
//
//                            Handler().postDelayed({
//
//                                if (subCount>1) {
//
//                                    valueAnim.resume()
//
//                                } else {
//
//                                    valueAnim.start()
//                                }
//
////
//
//
//                            },1000)
//
//
//                        } else {
//
//                            Toast.makeText(
//                                this@SelectClassActivity, "No class found", Toast.LENGTH_SHORT
//                            ).show()
//
//                        }
//
//                    }
//                }
//
//                //speechResultList = null
//
//            }
//
//            override fun onPartialResults(partialResults: Bundle?) {
//
//            }
//
//            override fun onEvent(eventType: Int, params: Bundle?) {
//
//            }
//
//
//        })
//
//            speechRecognizer.startListening(intent)
//
//
//        }


    }

    var viewWidth = 0f
    var pViewWidth = 0f
    var transDiff = 0f


    private fun initAnims() {

        viewWidth = textViewArrs[0].width.toFloat()
        pViewWidth = textViewArrs[0].width*0.65f

        valueAnim = ValueAnimator.ofFloat(viewWidth
            , pViewWidth,viewWidth)

        valueAnim.interpolator = LinearInterpolator()
        valueAnim.repeatCount = ValueAnimator.INFINITE


        valueAnim.duration = 800
        valueAnim.addUpdateListener {

            transDiff = Math.abs(viewWidth - it.getAnimatedValue() as Float)

            leftCount = 0
            rightCount = 0

            if (!booleanIsComplete) {

                for (i in 0 until textViewArrs.size) {

                    if (i%2 == 0) {

                        runOnUiThread {

                            val index = leftCount++

                            //llLeft.getChildAt(index).translationX = transDiff

                            if (!isReset) {

                                llLeft.getChildAt(index).translationX = transDiff

                            } else {

                                llLeft.getChildAt(index).translationX = -transDiff

                            }


                        }


                    } else {

                        runOnUiThread {

                            val index = rightCount++

                            //llRight.getChildAt(index).translationX = -transDiff

                            if (!isReset) {

                                llRight.getChildAt(index).translationX = -transDiff

                            } else {

                                llRight.getChildAt(index).translationX = transDiff

                            }


                        }


                    }
                }

//
                if (abs((it.animatedValue as Float) - pViewWidth) < 10f) {
                    valueAnim.pause()
                }




             } else {

                for (i in 0 until textViewArrs.size) {

                    if (i%2 == 0) {

                        runOnUiThread {

                            val index = leftCount++

                            //llLeft.getChildAt(index).translationX = -transDiff

                            if (!isReset) {

                                llLeft.getChildAt(index).translationX = -transDiff

                            } else {

                                llLeft.getChildAt(index).translationX = transDiff

                            }


                        }


                    } else {

                        runOnUiThread {

                            val index = rightCount++

                            //llRight.getChildAt(index).translationX = transDiff

                            if (!isReset) {

                                llRight.getChildAt(index).translationX = transDiff

                            } else {

                                llRight.getChildAt(index).translationX = -transDiff

                            }


                        }


                    }
                }

                Log.d(SelectClassActivity::class.java.name, "initAnims2:  + ${viewWidth.toInt()-(it.animatedValue as Float).toInt()}")

                if ((viewWidth-(it.animatedValue as Float)) < 10f) {

                    if (subCount<2) {

                        valueAnim.pause()

                    }

                    booleanIsComplete = false



//                    booleanIsComplete = false
//                    valueAnim.cancel()

                    //valueAnim.pause()

                }

            }

        }



        valueAnim.addPauseListener(object : Animator.AnimatorPauseListener{
            override fun onAnimationPause(animation: Animator) {

                if (!booleanIsComplete) {

                    toggleVisibilityTxtViews(false)

//                    if (subCount<2) {
//
//                        toggleVisibilityTxtViews(false)
//
//                    } else {
//
//                        goToChatActivity()
//                    }






                    //alphaAnim.start()

                } else {

                    //valueAnim.cancel()

                    //goToChatActivity()

//                    booleanIsComplete = !booleanIsComplete
                }
            }

            override fun onAnimationResume(animation: Animator) {

            }


        })


//        alphaAnim = ValueAnimator.ofFloat(1f,0f)
//
//        alphaAnim.interpolator = LinearInterpolator()
//        alphaAnim.duration = 500
//        alphaAnim.addUpdateListener {
//
//
//            for (i in textViewArrs.indices) {
//
//                if (!isOpen) {
//
//                    viewArr[i].alpha = it.animatedValue as Float
//
//                    textViewArrs[i].alpha = it.animatedValue as Float
//
//
//                } else {
//
//                    if (it.animatedValue == 1f) {
//
//                        viewArr[i].alpha = 1f
//
//                        textViewArrs[i].alpha = 1f
//
//
//                    } else {
//
//                        val a = 1f-it.animatedValue as Float
//
//                        viewArr[i].alpha = a
//
//                        textViewArrs[i].alpha = a
//
//
//                    }
//
//
//
//                }
//
//
//
//            }
//
//
//
//
//
//        }
//
//        alphaAnim.addListener(object : Animator.AnimatorListener{
//
//            override fun onAnimationStart(animation: Animator) {
//
//            }
//
//            override fun onAnimationEnd(animation: Animator) {
//
//                isOpen = !isOpen
//
//                if (isOpen) {
//
//                    //fetch subjects from Api
//
//
//
//                    if (subCount<2) {
//
//                        fetchSubjects()
//                        alphaAnim.start()
//
//                    } else {
//
//                        goToChatActivity()
//                    }
//
//
//
//
//                } else {
//
//                    booleanIsComplete = true
//
//                    //valueAnim.cancel()
//
//                    valueAnim.resume()
//                    //valueAnim.reverse()
//
//                    //valueAnim.start()
//
//                    //valueAnim.start()
//
//                    //start the other
//
//
//                }
//
//
//
//            }
//
//            override fun onAnimationCancel(animation: Animator) {
//
//            }
//
//            override fun onAnimationRepeat(animation: Animator) {
//
//            }
//
//
//        })



    }

    private fun fetchSubjects() {

        isReset = true

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

        //classLabelArr = subjects

        for (index in subjects.indices) {

            textViewArrs[index].isSelected = false
            textViewArrs[index].text = subjects[index]
        }

    }

    private fun goToChatActivity() {

        var bundle:Bundle = Bundle()

        if (classText != null && subText != null) {
            val intent = Intent(this@SelectClassActivity, ChoiceActivity::class.java)

            bundle.putString("class", classId.toString())
            bundle.putString("subId",subId.toString())
            bundle.putString("subject", subText)


//            intent.putExtra("class", classId.toString()) //5
//            intent.putExtra("subject", subText) //computer

            if (flowMap.containsValue(false)) {

                bundle.putString("start_time" , "0")
                bundle.putString("end_time" , "0")
                bundle.putBoolean("isFlow",false)

            } else {

                //Log.d(SelectClassActivity::class.java.name, "goToChatActivity: " +classApiData?.teacher?.id)

                bundle.putString("start_time" , classApiData?.start_time)
                bundle.putString("end_time" , classApiData?.end_time)
                bundle.putString("date",classApiData?.date)
                bundle.putString("assignmentId",classApiData?.id.toString())
                bundle.putString("teacherId",classApiData?.teacher?.id.toString())
                bundle.putBoolean("isFlow",true)
                bundle.putString("teacher_name",classApiData?.teacher?.name)
                bundle.putString("subject_name",classApiData?.subject?.name)


            }

            intent.putExtras(bundle)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

    }

    private fun toggleVisibilityTxtViews(visibility:Boolean) {

        lifecycleScope.launch {

            for (index in textViewArrs.indices) {

                if (!visibility) {

                    textViewArrs[index].animate()
                        .alpha(0f)                     // Animate alpha to 0 (fully transparent)
                        .setDuration(300)             // Duration in ms
                        .withEndAction {
                            textViewArrs[index].visibility = View.INVISIBLE
                            textViewArrs[index].alpha = 1f // Reset alpha for future use

                            if (subCount==2) {

                                if (index == textViewArrs.size-1) {

                                    valueAnim.cancel()

                                    goToChatActivity()

                                    //return@withEndAction


                                }


                            }
                        }
                        .start()



                    //textViewArrs[index].visibility = View.INVISIBLE
                    viewArr[index].visibility = View.INVISIBLE

                } else {

                    textViewArrs[index].alpha = 0f

                    textViewArrs[index].animate()
                        .alpha(1f)                     // Animate alpha to 0 (fully transparent)
                        .setDuration(300)             // Duration in ms
                        .withEndAction {
                            textViewArrs[index].visibility = View.VISIBLE
                            textViewArrs[index].alpha = 1f          // Reset alpha for future use
                        }
                        .start()

                    //textViewArrs[index].visibility = View.VISIBLE
                    viewArr[index].visibility = View.VISIBLE

                }

                delay(50)


            }

            if (subCount<2) {

                if (!isReset) {

                    isReset = true
                    booleanIsComplete = true
                    toggleVisibilityTxtViews(true)

                    Handler().postDelayed({

                        valueAnim.resume()

                        try {

                            getData(OkHttpClientInstance.subURL)

                        } catch (ex:Exception) {

                            Toast.makeText(this@SelectClassActivity,"Server Error",Toast.LENGTH_SHORT).show()
                        }



                    },1000)





//                isSubject = true
//                fetchSubjects()
//                toggleVisibilityTxtViews(true)
//                booleanIsComplete = true
//
//                Handler().postDelayed({
//
//                    valueAnim.resume()
//
//
//                },1000)


                }


            }



//            if (subCount == 2) {
//
//                goToChatActivity()
//            }

//            else {
//
//                goToChatActivity()
//
//            }




        }


    }

    var leftCount = 0
    var rightCount = 0
    var classId = 0
    var subId = 0
    var isSubject:Boolean = false

    private fun getData(url:String) {

        classLabelArr?.clear()
        indexArr.clear()
        //textViewArrs.clear()


        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: Response?) {

                isErrorDataFetch = false

                response?.let {resp->

                    response.body?.let {


                        val gson:Gson = Gson()
                        var clsResp = gson.fromJson(it.string(),ClassResponse::class.java)

                        clsResp?.let { classData->

                            classData.data?.let { it->



                                classItemList = ArrayList(it)

                                for (cls in it) {

                                    classLabelArr?.add(cls.name.toLowerCase())

                                    if (!isSubject) {

                                        if (cls.id == classApiData?.`class`?.id) {

                                            indexArr.add(classApiData?.`class`?.id!!)
                                        }

                                    } else {

                                        Log.d(SelectClassActivity::class.java.name, "Else")

                                        if (cls.id == classApiData?.subject?.id) {

                                            indexArr.add(classApiData?.subject?.id!!)
                                        }
                                    }


//                                        if (cls.equals(classApiData)) {
//
//                                            indexArr.add(cls.id)
//
//                                            Log.d(SelectClassActivity::class.java.name, "Contains ${classApiData?.`class`?.id}")
//
//                                        } else {
//
//                                            Log.d(SelectClassActivity::class.java.name, "Doesn't contain ${classData}")
//                                        }



//                                    else {
//
//                                        if (cls.equals(classApiData)) {
//
//                                            indexArr.add(cls.id)
//
//                                            Log.d(SelectClassActivity::class.java.name, "Contains ${classApiData?.`class`?.id}")
//
//                                        } else {
//
//                                            Log.d(SelectClassActivity::class.java.name, "Doesn't contain ${classData}")
//                                        }
//
//
//                                    }


                                }



                                leftCount = 0
                                rightCount = 0

                                textViewArrs.clear()
                                viewArr.clear()

                                //assignTextViewLabels

                                runOnUiThread {

                                    assignLabels(it)

                                    if (!isAnimFlag) {

                                        isAnimFlag = true

                                        textViewArrs[0].post {

                                            initAnims()


                                        }

                                    }



                                    if (subCount == 0) {

                                        textViewArrs.forEach {txtView->

                                            txtView.setOnClickListener {v->


                                                subCount++
                                                isReset = false

                                                disableAllSelectionText()
                                                txtView.isSelected = true

                                                if (!isSubject) {

                                                    isSubject = true

                                                    classText = (txtView as TextView).text.toString()
                                                    classId = it.get(textViewArrs.indexOf(txtView)).id

                                                    if (indexArr.contains(classId)) {

                                                        flowMap.put("isClass",true)
                                                    }

                                                    Log.d(SelectClassActivity::class.java.name, "FlowMap: " + flowMap.toString())

                                                } else {

                                                    Log.d(SelectClassActivity::class.java.name, "onResult: " + textViewArrs.indexOf(txtView))

                                                    subText = (txtView as TextView).text.toString()
                                                    subId = classItemList!!.get(textViewArrs.indexOf(txtView)).id

                                                    Log.d(SelectClassActivity::class.java.name, "onSubIdS: " + subId)

                                                    if (indexArr.contains(subId)) {

                                                        flowMap.put("isSubject",true)
                                                    }

                                                    Log.d(SelectClassActivity::class.java.name, "FlowMap: " + flowMap.toString())

                                                }

                                                Handler().postDelayed({

                                                    if (subCount>1) {

                                                        valueAnim.resume()

                                                    } else {

                                                        valueAnim.start()
                                                    }

//


                                                },1000)





                                            }

                                        }



                                    } else {


                                    }

                                }


                            }




                        }




                    }

                }


            }

            override fun onError(code: Int, response: Response?) {

                isErrorDataFetch = true

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

                Log.d(SelectClassActivity::class.java.name, "onException: " + error)

                runOnUiThread {

                    Toast.makeText(this@SelectClassActivity,error,Toast.LENGTH_SHORT).show()

                }


            }


        })

        try {

            if (url.equals(OkHttpClientInstance.classURL)) {

                OkHttpClientInstance.get("classList.php")

            } else {

                OkHttpClientInstance.get("subjectList.php?class_id=${classId}")

            }

        } catch (ex:Exception) {

            Toast.makeText(this@SelectClassActivity,"Server Error",Toast.LENGTH_SHORT).show()


        }





    }

    private fun assignLabels(it: List<ClassResponse.ClassItem>) {

        for (index in 0 until  llRight.childCount) {

            llRight.getChildAt(index).visibility = View.INVISIBLE
            (llRight.getChildAt(index)).findViewById<TextView>(R.id.class_text).isSelected = false
        }

        for (index in 0 until  llLeft.childCount) {

            llLeft.getChildAt(index).visibility = View.INVISIBLE
            (llLeft.getChildAt(index)).findViewById<TextView>(R.id.class_text).isSelected = false
        }

        leftCount = 0
        rightCount = 0

        for (i in 0 until it.size) {

            if (i%2 == 0) {

                runOnUiThread {

                    val index = leftCount++
                    Log.d(SelectClassActivity::class.java.name, "assignLabels: " + index)

                    if (!isReset) {

                        llLeft.getChildAt(index)?.let { child->

                            child.visibility = View.VISIBLE

                            textViewArrs.add(
                                (child as ConstraintLayout)
                                    .findViewById<TextView?>(R.id.class_text)
                                    .apply {

                                        text = it.get(i).name

                                        Log.d(SelectClassActivity::class.java.name, "assignLabels: " + text.toString())

                                        if (indexArr.contains(it.get(i).id)) {

                                            isSelected = true

                                        } else {

                                            isSelected = false
                                        }
                                    })

                            viewArr.add(
                                (child as ConstraintLayout)
                                    .findViewById(R.id.view)
                            )


                        }






                    } else {

                        llLeft.getChildAt(index)?.let { child->

                            llLeft.getChildAt(index).visibility = View.VISIBLE

                            textViewArrs.add(
                                (child as ConstraintLayout)
                                    .findViewById<TextView?>(R.id.class_text)
                                    .apply {

                                        text = it.get(i).name

                                        Log.d(SelectClassActivity::class.java.name, "assignLabels: " + text.toString())

                                        if (indexArr.contains(it.get(i).id)) {

                                            isSelected = true
                                        } else {

                                            isSelected = false
                                        }
                                    })

                            viewArr.add(
                                (child as ConstraintLayout)
                                    .findViewById(R.id.view)
                            )


                        }



//                        try {
//
//                            textViewArrs.get(index).visibility = View.VISIBLE
//
//                            textViewArrs.get(index).setText(it.get(i).name)
//
//                        }catch (ex:Exception) {
//
//                            llLeft.getChildAt(index).visibility = View.VISIBLE
//
//                            textViewArrs.add(
//                                (llLeft.getChildAt(index) as ConstraintLayout)
//                                    .findViewById<TextView?>(R.id.class_text)
//                                    .apply {
//
//                                        text = it.get(i).name
//                                        translationX = transDiff
//                                    })
//
//                            viewArr.add(
//                                (llLeft.getChildAt(index) as ConstraintLayout)
//                                    .findViewById(R.id.view)
//                            )
//
//
//                        }




                    }


                }


            } else {

                runOnUiThread {

                    val index = rightCount++

                    if (!isReset) {

                        llRight.getChildAt(index)?.let { child->

                            child.visibility =
                                View.VISIBLE
                            textViewArrs.add(
                                (child as ConstraintLayout)
                                    .findViewById<TextView?>(R.id.class_text)
                                    .apply {

                                        text = it.get(i).name

                                        Log.d(SelectClassActivity::class.java.name, "assignLabels: " + text.toString())

                                        if (indexArr.contains(it.get(i).id)) {

                                            isSelected = true
                                        } else {

                                            isSelected = false
                                        }
                                    })

                            viewArr.add(
                                (child as ConstraintLayout)
                                    .findViewById(R.id.view)
                            )


                        }



                    } else {

                        llRight.getChildAt(index)?.let { child->

                            child.visibility =
                                View.VISIBLE
                            textViewArrs.add(
                                (child as ConstraintLayout)
                                    .findViewById<TextView?>(R.id.class_text)
                                    .apply {

                                        text = it.get(i).name

                                        Log.d(SelectClassActivity::class.java.name, "assignLabels: " + text.toString())

                                        if (indexArr.contains(it.get(i).id)) {

                                            isSelected = true

                                        } else {

                                            isSelected = false


                                        }
                                    })

                            viewArr.add(
                                (child as ConstraintLayout)
                                    .findViewById(R.id.view)
                            )


                        }




//                        try {
//
//                            textViewArrs.get(index).visibility = View.VISIBLE
//
//                            textViewArrs.get(index).setText(it.get(i).name)
//
//                        }catch (ex:Exception) {
//
//                            llRight.getChildAt(index).visibility = View.VISIBLE
//
//                            textViewArrs.add(
//                                (llRight.getChildAt(index) as ConstraintLayout)
//                                    .findViewById<TextView?>(R.id.class_text)
//                                    .apply {
//
//                                        text = it.get(i).name
//                                        translationX = -transDiff
//                                    })
//
//                            viewArr.add(
//                                (llRight.getChildAt(index) as ConstraintLayout)
//                                    .findViewById(R.id.view)
//                            )
//
//
//                        }


                    }


                }


            }
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

    override fun onDestroy() {
        super.onDestroy()

        SpeechRecognizerInstance.destroyInstance()


    }

}