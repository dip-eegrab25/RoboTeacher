package com.ai.roboteacher.activities


import CustomDialogBox
import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.ai.roboteacher.DataSet
import com.ai.roboteacher.Direction
import com.ai.roboteacher.FakeShutdownOverlay
import com.ai.roboteacher.KtorDataReceiver
import com.ai.roboteacher.LinearLayoutManagerWrapper
import com.ai.roboteacher.ModelDownloadTask
import com.ai.roboteacher.MyDeviceAdminReceiver
import com.ai.roboteacher.MySpannableStringBuilder
import com.ai.roboteacher.MyValueAnimator
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.SpeechRecognizerInstance
import com.ai.roboteacher.StatusService
import com.ai.roboteacher.SwipeGestureListener
import com.ai.roboteacher.TextToSpeechInstance
import com.ai.roboteacher.ThinkingViewRoot
import com.ai.roboteacher.Utils
import com.ai.roboteacher.miko.RoboExpressionView
import com.ai.roboteacher.miko.StarsView

import com.anim.spectrumprogress.Spectrum
import com.anim.spectrumprogress.Type



import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.movement.MovementMethodPlugin

import okhttp3.Response

import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern


class GeneralActivity : AppCompatActivity()
    ,KtorDataReceiver.KtorDataListener
    ,ModelDownloadTask.ModelDownloadListener{

    private val TAG = GeneralActivity::class.java.name
    var thinkPattern:Pattern? = null
    var modePattern:Pattern? = null
    var bottomSheetRoot:RelativeLayout?=null
    var editTextQuery:EditText?=null
    var markWon:Markwon?=null
    var starsView1: StarsView?=null
    var starsView2:StarsView?=null
    var lineCount = 0
    var mLineCount = 0
    var isSpectrumStarted = false
    var drawer:DrawerLayout?=null
    var totalModelSize = 79348845
    var filesDownloaded:Int = 0
    var modelArr = arrayOf("http://192.168.1.41/panda/aimodel/whisper-base.tflite"
        ,"http://192.168.1.41/panda/aimodel/filters_vocab_multilingual.bin")


    var mCount = 0
    var spectrum:Spectrum?=null
    var micToggle:ImageView?=null
    var mainLayout:ConstraintLayout?=null
    var valueAnim:MyValueAnimator?=null
    var isReset:Boolean = false


    var expView: RoboExpressionView?=null
    var talkMode:RoboExpressionView.Expressions=RoboExpressionView.Expressions.NEUTRAL_TALKING
    var neutralMode:RoboExpressionView.Expressions=RoboExpressionView.Expressions.NEUTRAL
    var speechRunnable:java.lang.Runnable?=null
    var speechHandler = Handler()
    var btnStop:ImageView?=null
    var respProgress:ProgressBar?=null
    private var dataReceiver:KtorDataReceiver?=null
    private var bottomSheetWidth = 0
    private var buttonWakeUp:ImageView?=null
    var dataSet: ArrayList<DataSet> = ArrayList<DataSet>()
    private var dataRecycler:RecyclerView?=null
    private var chatAdapter:ChatAdapter?=null
    private var dropDownArrow:ImageView?=null

    var index = 0
    var quesNo = 0
    var pdfMap = mutableMapOf<Int,Pair<String,StringBuilder>?>()
    var powerOff:ImageView?=null
    private var statusServiceConnection:StatusServiceConnection?=null
    var isTurnedOff = false
    var scWidth:Float = 0f
    var langDropDown:ImageView?=null
    var langDialog:Dialog?=null
    var selectedLangId = -1
    var thinkingRoot:ThinkingViewRoot?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // enable edgeToEdge functionality(full screen)
        enterFullScreenMode(window)
        isTurnedOff = true
        //isWakeUp = false

        //Log.d(TAG, "onCreate: "+isTurnedOff)

        checkOverlayPerms()

        //screen off(will turn on when status service true. See StatusService.kt)
        //simulateShutdown(this)
        setContentView(R.layout.activity_general)

        //check for downloads and load model
        //downloadModel()

        //enable kiosk mode(needs commands to be passed through cmd)
        startKiosk()


        scWidth = Utils.getScreenWidth(this).toFloat()

        statusServiceConnection = StatusServiceConnection() //to check service status

        //swipe gesture from right to show/hide poweroff button
        val gestureDetector: GestureDetector = GestureDetector(this,
            SwipeGestureListener(
                { direction, velocityX, velocityY ->

                when(direction) {

                    Direction.RIGHT-> {

                        powerOff!!.animate().translationX(scWidth).start()
                        Log.d(TAG, "Swiped")

                    }

                    Direction.LEFT->{

                        Log.d(TAG, "Swiped Left")
                        powerOff!!.animate().translationX(0f).start()


                    }

                    Direction.TAP_UP -> {

                        Log.d(TAG, "onTouchUp: ")

                        val i = Intent(this, SpikeActivity::class.java)
                            startActivity(i)

                    }


                    else-> {


                    }


                }
            },applicationContext)
        )

        //markwon instance for markdown

        markWon = Markwon.builder(this)
            .usePlugin(MarkwonInlineParserPlugin.create())
//            .usePlugin(ImagesPlugin.create(ImagesPlugin.))
            .usePlugin(TablePlugin.create(this)) // <-- important
            .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
            .usePlugin((JLatexMathPlugin.create(16f,object:JLatexMathPlugin.BuilderConfigure{
                override fun configureBuilder(builder: JLatexMathPlugin.Builder) {

                    builder.inlinesEnabled(true)


                    builder.blocksEnabled(true);
                }


            })))
            .build()


        //regex to eliminate <think>...</think> tag
        thinkPattern = Pattern.compile(thinkRegex)

        //regex for <mode>happy</mode>(not to be eliminated.Will be used for robo expressions)
        modePattern = Pattern.compile(modeRegex)


        setupTTS()//setup TextToSpeech

        mainLayout = findViewById(R.id.main)

        dataRecycler = findViewById(R.id.data_recycler)
        powerOff = findViewById(R.id.power_off)
        langDropDown = findViewById(R.id.lang_selector)
        thinkingRoot = findViewById(R.id.t_root)

        //langDropDown to select different models(Inflating a dialogbox)

        langDropDown!!.setOnClickListener {

            if (langDialog == null) {

                langDialog = CustomDialogBox()
                    .buildDialog(this@GeneralActivity,R.layout.layout_select_lang)
                    .setSize((Utils.getScreenWidth(applicationContext)*0.50f).toInt(),(Utils.getScreenHeight(applicationContext)*0.30f).toInt())
                    .createDialog()

            }

            langDialog!!.show()

            val langRadGroup:RadioGroup = langDialog!!.findViewById(R.id.lang_rad_group)




            langRadGroup.setOnCheckedChangeListener(object :RadioGroup.OnCheckedChangeListener{
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

                    selectedLangId = langRadGroup.checkedRadioButtonId
                }


            })

        }


        dataRecycler!!.layoutManager = LinearLayoutManagerWrapper(this)

        drawer = findViewById(R.id.drawer)

        dropDownArrow = findViewById(R.id.dropdown_arrow)

        //navigation drawer(left) to view responses
        dropDownArrow!!.setOnClickListener {

            if (drawer!!.isDrawerOpen(GravityCompat.START)) {

                drawer!!.closeDrawer(GravityCompat.START)

            } else {

                drawer!!.openDrawer(GravityCompat.START)
            }
        }

        //hiding poweroff initially to the right

        powerOff!!.post {


            powerOff!!.animate().translationX(scWidth).start()

        }

        //turn screen off
        powerOff!!.setOnClickListener {

            var b:androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog
                .Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Alert")
                .setMessage("Are you sure you want to switch off?")
                .setCancelable(false)
                .setPositiveButton("Ok",object :DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        turnDeviceOff() // turn screen off function
                    }


                })
                .setNegativeButton("Cancel",object:DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        dialog?.dismiss()
                    }


                })

            var alertDialog = b.create()
            alertDialog.show()


        }

        //adding gestureDetector to swipelayout for turnoff button
        mainLayout!!.setOnTouchListener({_,event->

            gestureDetector.onTouchEvent(event)
            true
        })

        buttonWakeUp = findViewById(R.id.btn_wake_up)
        buttonWakeUp!!.visibility = View.VISIBLE
        expView = findViewById(R.id.expression_view)
        micToggle = findViewById(R.id.mic_toggle)
        starsView1 = findViewById(R.id.stars_view1)
        starsView2 = findViewById(R.id.stars_view2)
        bottomSheetRoot = findViewById(R.id.btm_sheet)
        spectrum = bottomSheetRoot!!.findViewById(R.id.spectrum)
        editTextQuery = findViewById(R.id.edt_query)
        btnStop = findViewById(R.id.btn_stop)
        respProgress = findViewById(R.id.resp_progress)
        respProgress!!.visibility = View.GONE
        btnStop!!.visibility = View.GONE

        bottomSheetRoot!!.visibility = View.INVISIBLE

        editTextQuery!!.isFocusable = false
        editTextQuery!!.showSoftInputOnFocus = false

        //adapter for recyclerview for responses
        chatAdapter = ChatAdapter()
        chatAdapter!!.setHasStableIds(true)
        dataRecycler!!.adapter = chatAdapter



        //wakeUp button functionality
        buttonWakeUp!!.setOnClickListener {

            buttonWakeUp!!.visibility = View.GONE
            //isWakeUp = false

            bottomSheetRoot!!.post {

                bottomSheetWidth = bottomSheetRoot!!.width
                bottomSheetRoot!!.translationX =
                    bottomSheetWidth.toFloat() //initially hiding
                bottomSheetRoot!!.visibility = View.VISIBLE

                isReset = false // update flag to false to show again(see animateBottomSheetRoot function)

                animateBottomSheetRoot(isReset)
            }


        }



        //stop button functionality(will stop TTS and responses)

        btnStop!!.setOnClickListener {

//            dataReceiver?.isStopped = true
           stopStreaming()


        }


        speechRunnable = object :java.lang.Runnable{
            override fun run() {

                SpeechRecognizerInstance.destroyInstance()

                respBuilder.clear()


                if (!editTextQuery!!.text.toString().isEmpty()) {

                    if (status.equals("up")) { //only send request when service is up

                        thinkingRoot!!.post {

                            thinkingRoot!!.startAnims()
                        }

                        nStrBuilder.clear()

                        isServiceAlert = false

                        quesNo++

                        //pdfMap.put(quesNo, StringBuilder()) //Map to store the question and respective answers which will be used in pdf

                        pdfMap.put(quesNo,Pair<String,StringBuilder>("${editTextQuery!!.text.toString()}?",
                            StringBuilder()
                        ))

                        btnStop!!.visibility = View.VISIBLE
                        mCount = 0 // reset mode flag

                        //lineCount and mLineCount which will be
                        // used to keep track of lines spoken(see utteranceProgressListener)
                        lineCount = 0
                        mLineCount = 0

                        animateBottomSheetRoot(isReset)

                        respProgress!!.visibility = View.VISIBLE //show circular indeterminate progressbar

                        //add the request params to dataset
                        dataSet.add(DataSet(System.currentTimeMillis(), MySpannableStringBuilder(editTextQuery!!.text.toString()),false,null,true,true,isAnim = false))

                        chatAdapter!!.notifyItemInserted(dataSet.size - 1)


                        //add Thinking anim while waiting
                        dataSet.add(DataSet(System.currentTimeMillis(),
                            MySpannableStringBuilder("Thinking"),false,null,false,true, quesNo = quesNo
                            ,/*set isAnim to true(will change later when response arrives)*/ isAnim = true))


                        index = dataSet.size - 1 // update index

                        chatAdapter!!.notifyItemInserted(index)

                        //send the request(launch coroutine,dataListener will give the responses)
                        dataReceiver = KtorDataReceiver(System.currentTimeMillis().toString(),editTextQuery!!.text.toString()
                            , /*to get responses*/dataListener = this@GeneralActivity
                            , url = RetrofitInstanceBuilder.GENERAL_PURPOSE_ASSISTANT)
                        dataReceiver!!.run()


                        editTextQuery!!.setText("") // clear editText


                    } else {

                        //Dont send request while service is down

                        micToggle!!.isSelected = false

                        //spectrum!!.pauseAnims()

                        Toast.makeText(this@GeneralActivity,"Service not available.Please try again",
                            Toast.LENGTH_SHORT).show()


                    }


                } else {

                    micToggle!!.isSelected = false
                }

                Log.d(TAG, "Speech Sent")
            }


        }

//
//        micToggle!!.setOnTouchListener { v, event ->
//
//            when(event.action) {
//
//                MotionEvent.ACTION_DOWN-> {
//
//                    if (!micToggle!!.isSelected) {
//
////
//
//                        if (!isSpectrumStarted) {
//
//                            spectrum!!.resumeAnims()
//                            isSpectrumStarted = true
//                        }
//
//                        micToggle!!.isSelected = true
//
//                        WhisperInstance.startRecording()
//
//                        //startRecording()
//
//
//                    }
//
//                    true
//                }
//
//                MotionEvent.ACTION_UP->{
//
//                    micToggle!!.isSelected = false
//
//                    WhisperInstance.stopRecording()
//
//                    //stopRecording()
//
//                    true
//
//
//                }
//
//                else->{
//
//                    super.onTouchEvent(event)
//
//
//                }
//
//
//            }
//
//
//        }

        //turn on mic for speech
        micToggle!!.setOnClickListener {


            startStopMic()

//
        }

        //configure spectrum initially
//        spectrum!!.post {
//
//            spectrum!!.startAnim(Type.VOICE)
//            Thread.sleep(1000)
//
//            if (spectrum!!.isRunning) {
//
//                spectrum!!.pauseAnims()
//                isSpectrumStarted = false
//
//            }
//
//        }

        //set robo exp to neutral initially

        expView!!.post {

            expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL)

        }



    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterFullScreenMode(window)


    }

    //kiosk mode function
    private fun startKiosk() {

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val cn = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(cn, arrayOf(packageName))

            dpm.setStatusBarDisabled(cn, true)
            dpm.setKeyguardDisabled(cn,true)
        }

        if (dpm.isLockTaskPermitted(packageName)) {
            startLockTask()
        } else {
            //Toast.makeText(this, "Lock task not permitted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun turnDeviceOff() {

        var url:String = RetrofitInstanceBuilder.BASEURL.replace("8012","8020")
        url = url+RetrofitInstanceBuilder.shutDown

        OkHttpClientInstance.getInstance(object:OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: Response?) {

                Log.d("aaaa", "onResult: "+response!!.body!!.string())

                runOnUiThread {

                    isTurnedOff = true // update turnoff flag



                    //isWakeUp = true //for wakeupbutton visibility

                    isReset = true // hiding the editText based on flags(see animateBottomSheetRoot function)

                    animateBottomSheetRoot(isReset)//show/hide edittext

                    unbindService(statusServiceConnection!!) // unbind from Statusservice

                    simulateShutdown(this@GeneralActivity) // turn screen off

                    //connect later after 10 seconds
                    Handler().postDelayed(
                        {

                            var statusIntent = Intent(this@GeneralActivity,StatusService::class.java)
                            bindService(statusIntent,statusServiceConnection!!, Context.BIND_AUTO_CREATE)




                        },10000)

                }

            }

            override fun onError(code: Int, response: Response?) {
                Log.d("aaaa", "onResult: ${code} "+response!!.body!!.string())
            }

            override fun onException(error: String?) {
                Log.d("aaaa", "onResult: "+error)
            }


        })

        OkHttpClientInstance.post(url,null)

    }

    var overlay:FakeShutdownOverlay?=null

    //turn screen off function
    fun simulateShutdown(context: Context) {
        overlay = FakeShutdownOverlay(context)
        overlay!!.show()

        // Optional: lock device
//        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        val admin = ComponentName(context, MyDeviceAdminReceiver::class.java)
//        try {
//            dpm.lockNow()
//        } catch (e: SecurityException) {
//            // Not device admin? Ignore.
//        }
    }

    private fun startStopMic() {

        if (selectedLangId==-1) {

            return
        }

        if (checkAudioPermissionAndStart()) {

            if (!micToggle!!.isSelected) {

                //start spectrum anim
                if (!isSpectrumStarted) {

                    spectrum!!.startAnim(Type.VOICE)
                    isSpectrumStarted = true

                } else {

                    spectrum!!.resumeAnims()

                }

                micToggle!!.isSelected = true
                //isStopped.set(false) //set isStopped to false(used to send speech for transcription.only happens when false)

                startSpeechRecognizer()

                //WhisperInstance.startRecording() // start recording


            } else {

                //isStopped.set(true) //update isStopped when mic off so it
                // doesn't send the recorded speech for transcription

                SpeechRecognizerInstance.destroyInstance()

                //WhisperInstance.stopRecording() // stop recording

                nStrBuilder.clear()

                speechHandler.removeCallbacks(speechRunnable!!) //remove all callbacks

                editTextQuery!!.setText("") //reset editText

                respProgress!!.visibility = View.GONE //reset response progress(GONE)

                spectrum!!.pauseAnims() //pause spectrum anims
                isSpectrumStarted = false //update spectrum anim flag

//                dataReceiver?.isStopped = true
                dataReceiver?.dataListener = null //set datalistener to null(optionally)
                dataReceiver = null

                micToggle!!.isSelected = false //reset micToggle


                //reset stars and turn robo exp to neutral
                starsView1!!.visibility = View.GONE
                starsView2!!.visibility = View.GONE

                starsView1!!.stopAnim()
                starsView2!!.stopAnim()
                expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL)

            }


        }
    }

    //edittext show/hide function
    private fun animateBottomSheetRoot(reset: Boolean) {

        if (!reset) {

            bottomSheetRoot!!.animate().translationX(0f).withEndAction {

                isReset = true
                //micToggle!!.isSelected = false
                //startSpeechRecognizer()


            }.start()

        } else {

            bottomSheetRoot!!.animate().translationX(bottomSheetWidth.toFloat()).withEndAction {

                isReset = false

            }.start()


        }

    }

    //    //grant audiopermission for mic
//    private fun checkAudioPermissionAndStart():Boolean {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//            != PackageManager.PERMISSION_GRANTED) {
//
//
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
//
//                100
//            )
//
//            return false
//
//
//        } else {
//
//           return checkModels()
//
//        }
//    }

    //grant audiopermission for mic
    private fun checkAudioPermissionAndStart():Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),

                100
            )

            return false


        } else {

            return true

        }
    }


    var customDialog:Dialog?=null

    private fun checkOverlayPerms() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 1234)
            } else {

                simulateShutdown(this)


            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1234) {

            buttonWakeUp!!.visibility = View.VISIBLE
            // Give it a small delay to ensure system updates the permission state
            Handler(Looper.getMainLooper()).postDelayed({

                simulateShutdown(this)

            }, 500)
        }


    }


//    private fun initVoskModel() {
//        StorageService.unpack(
//            this, "model-en-us", "model",
//            org.vosk.android.StorageService.Callback<org.vosk.Model> { model: org.vosk.Model ->
//                this.model = model
//
//                val rec = Recognizer(model, 16000.0f)
//                speechService = SpeechService(rec, 16000.0f)
//                speechService!!.startL.setistening(this)
//            },
//            org.vosk.android.StorageService.Callback<IOException> { exception: IOException ->
//
//                Log.d(TAG, "Model load failed")
//            })
//    }



//    private fun showAlertPromptForGoogleApp() {
//
//        AlertDialog.Builder(this)
//            .setIcon(R.drawable.ic_android_black_24dp)
//            .setTitle("App not installed")
//            .setCancelable(false)
//            .setMessage("Google App needs to be installed for voice mode. Do you want to install it now?")
//            .setPositiveButton("Ok",{ dialogInterface, i ->
//
//                dialogInterface.dismiss()
//                installGoogleApp()
//
//            })
//            .setNegativeButton("Cancel",{dialogInterface,i:Int->
//
//                dialogInterface.dismiss()
//
//
//            })
//            .create()
//            .show()
//    }

//    private fun installGoogleApp() {
//
//        var intent:Intent = Intent(Intent.ACTION_VIEW).apply {
//
//            data = Uri.parse("market://details?id=com.google.android.googlequicksearchbox")
//            setPackage("com.android.vending")
//        }
//
//        try {
//
//            startActivity(intent)
//
//        } catch (ex: ActivityNotFoundException) {
//
//            val webIntent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox")
//            }
//            startActivity(webIntent)
//        }
//    }

    var isOnGoing = false
    var nStrBuilder = StringBuilder()

    private fun startSpeechRecognizer() {

        var lang:String = langDialog!!.findViewById<RadioGroup>(R.id.lang_rad_group).let {

            if (it.findViewById<RadioButton>(selectedLangId).text.toString().lowercase().equals("english")) {

                "en-IN"


            } else {

                "hi-IN"
            }
        }



//        if (!isSpeechInitialized) {



            SpeechRecognizerInstance.getInstance(this,object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {


                    //editTextQuery!!.setText("")
                    micToggle!!.isSelected = true



                }

                override fun onBeginningOfSpeech() {

                }

                override fun onRmsChanged(rmsdB: Float) {

//                    rmsList.add(rmsdB)
//
//                    // Keep only last 20 samples
//                    if (rmsList.size > 20) rmsList.removeAt(0)
//
//                    lastRmsAverage = rmsList.average().toFloat()
//                    Log.d("RMS", "Current RMS = $rmsdB | Avg = $lastRmsAverage")

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onEndOfSpeech() {

                    spectrum!!.post {

                        spectrum!!.pauseAnims()

                    }

//                    if (isWakeUp) {
//
//                        Log.d(TAG, "onEndOfSpeech: ")
//
//                        buttonWakeUp!!.visibility = View.VISIBLE
//                    }

                }

                override fun onError(error: Int) {

                    //isProcessing = false
                    //SpeechRecognizerInstance.destroyInstance()
                    //micToggle!!.isSelected = false
                    SpeechRecognizerInstance.destroyInstance()

//                    if (error == 9) {
//
//                        showAlertPrompt("Alert"
//                            ,"Go to settings to enable Google microphone permission"
//                            ,R.drawable.ic_android_black_24dp)
//
//                    }
//
//                    if (isWakeUp) {
//
//                        Log.d(TAG, "onError: ")
//
//                        buttonWakeUp!!.visibility = View.VISIBLE
//                    }


                    Log.d(SelectClassActivity::class.java.name, "onError: ${error}")

                }

                override fun onResults(results: Bundle?) {


                }

                override fun onPartialResults(partialResults: Bundle?) {

                    speechHandler.removeCallbacks(speechRunnable!!)

                    partialResults?.let {

                        val l = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val s:String? = l?.firstOrNull()?.trim()

                        if (!s.isNullOrBlank()) {

                            nStrBuilder.clear()
                            nStrBuilder.append(s)

                            editTextQuery!!.setText(nStrBuilder.toString())

                        }



                        Log.d(SampleActivity::class.java.name, "onPartialResults: "+l?.firstOrNull())

                    }

//                    val confidenceScores:FloatArray? = partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
//                    val speechResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                    val s = speechResultList?.firstOrNull()?.trim()
//
//                    Log.d(TAG, "Data: ${confidenceScores?.get(0)!!}")
//
////// --------------------------
////// 1. Early returns FIRST
////// --------------------------
//                    if (confidenceScores != null && confidenceScores.isNotEmpty() && confidenceScores[0] < 0.6f) {
//                        return
//                    }
//
//                    if (s.isNullOrEmpty()) {
//                        return
//                    }
//
////                    if (lastRmsAverage < 3f) {
////                        Log.d(TAG, "Ignoring soft speech: RMS=$lastRmsAverage")
////                        rmsList.clear()
////                        lastRmsAverage = 0f
////                        return
////                    }
//
//                    speechHandler.removeCallbacks(speechRunnable!!)
////
////// --------------------------
////// 3. Safe string builder logic
////// --------------------------
//                    if (nStrBuilder.isEmpty()) {
//
//                        nStrBuilder.append(s)
//                        editTextQuery?.post { editTextQuery?.append(s) }
//
//                    } else {
//
//                        val current = nStrBuilder.toString()
//
//                        if (s.startsWith(current) && s.length > current.length) {
//
//                            val newPart = s.substring(current.length)
//                            nStrBuilder.append(newPart)
//
//                            editTextQuery?.post { editTextQuery?.append(newPart) }
//
//                        } else if (!s.trim().equals(nStrBuilder.toString().trim(), ignoreCase = true)) {
//
//                            val isValidNewStart = s.trim().split(" ").size==1
//
//                            if (isValidNewStart) {
//                                nStrBuilder.clear()
//                                nStrBuilder.append(s)
//                                editTextQuery?.post { editTextQuery?.append(s) }
//                            }
//                        }
//                    }
//
//// --------------------------
//// 4. GUARANTEED repost
//// --------------------------
                    speechHandler.postDelayed(speechRunnable!!, 3000)


//// --------------------------
//// 2. Cancel previous runnable
//// --------------------------






//
//
////                    speechHandler.removeCallbacks(speechRunnable!!)
////
//                    val confidenceScores = partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
//                    if (confidenceScores != null && confidenceScores[0] < 0.5f) return
//
//                    val speechResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                    val s = speechResultList?.firstOrNull()?.trim()
//
//                    if (s.isNullOrEmpty()) return
//
//                    if (nStrBuilder.isEmpty()) {
//                        nStrBuilder.append(s)
//                        editTextQuery?.post { editTextQuery!!.append(nStrBuilder.toString()) }
//                    } else {
//                        val current = nStrBuilder.toString()
//                        if (s!!.startsWith(current) && s.length > current.length) {
//                            val newPart = s.substring(current.length)
//                            nStrBuilder.append(newPart)
//                            editTextQuery?.post { editTextQuery!!.append(newPart) }
//                        } else if (!s.equals(current, ignoreCase = true)) {
//                            val isValidNewStart = s.length >= 4 && s.count { it.isLetter() } >= 3
//                                    && !editTextQuery!!.text.toString().contains(s) && s.trim().split(" ").size==1
//                            if (isValidNewStart ) {
//                                nStrBuilder.clear()
//                                nStrBuilder.append(s)
//                                editTextQuery?.post { editTextQuery!!.append("$s") }
//                            }
//                        }
//                    }
//
//                    //speechHandler.postDelayed(speechRunnable!!,3000)
//
                }


                override fun onEvent(eventType: Int, params: Bundle?) {

                }


            },20000,lang)

       // }

        SpeechRecognizerInstance.startSpeech()

    }

    private fun stopStreaming() {

        dataReceiver?.dataListener = null //set datalistener to null. Prevents UI update
        dataReceiver = null

        thinkingRoot!!.post {

            thinkingRoot!!.stopAnim()
        }

        respProgress!!.visibility = View.GONE //hide indeterminate circular progress

        if (dataSet.get(index).isAnim) { //if Thinking animation runs stop it

            var errorStr = SpannableString("Stopped by user")
            errorStr.setSpan(ForegroundColorSpan(Color.RED),0,errorStr.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)


            dataSet.get(index).spannableStringBuilder.clear()
            dataSet.get(index).spannableStringBuilder.append(errorStr)
            dataSet.get(index).isError = true

            dataSet.get(index).isAnim = false

            chatAdapter!!.notifyItemChanged(index)


        }

        TextToSpeechInstance.destroyInstance() //destroy TTS
        expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL) //set robo exp to neutral

        //hide stars if present(stars are present when mode is happy)
        starsView1!!.visibility = View.GONE
        starsView2!!.visibility = View.GONE

        //stop the star anims
        starsView1!!.stopAnim()
        starsView2!!.stopAnim()

        isExpSet = false //updated expression flag to false
        btnStop!!.visibility = View.GONE //hide stop button

        isReset = false //reset edittext visibility flag

        micToggle!!.isSelected = false

        animateBottomSheetRoot(isReset)
    }

    //var isStopped:AtomicBoolean = AtomicBoolean(false)

    var thinkRegex = "<think>(.*?)</think>"
    var modeRegex = "<mode>(.*?)</mode>"
    var markRegex = "```(.*?)```"
    var modee:String?=null
    var wordLength = 0

    //setMode function(happy,sad etc. Comes from <mode>happy</mode> tag from response)
    private fun setMode(mode:String) {

        if (mode == "neutral") {

            runOnUiThread {

                neutralMode = RoboExpressionView.Expressions.NEUTRAL
                //expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL)
                talkMode = RoboExpressionView.Expressions.NEUTRAL_TALKING

                starsView1!!.visibility = View.GONE
                starsView2!!.visibility = View.GONE

                starsView1!!.stopAnim()
                starsView2!!.stopAnim()


            }



        } else if (mode == "happy") {

            runOnUiThread {

                neutralMode = RoboExpressionView.Expressions.HAPPY

                //expView!!.setExpression(RoboExpressionView.Expressions.HAPPY)
                talkMode = RoboExpressionView.Expressions.HAPPY_TALKING

                starsView1!!.visibility = View.VISIBLE
                starsView2!!.visibility = View.VISIBLE

                starsView1!!.startAnim()
                starsView2!!.startAnim()


            }



        } else if (mode == "sad") {

            runOnUiThread {

                neutralMode = RoboExpressionView.Expressions.SAD
                //expView!!.setExpression(RoboExpressionView.Expressions.SAD)
                talkMode = RoboExpressionView.Expressions.SAD_TALKING

                starsView1!!.visibility = View.GONE
                starsView2!!.visibility = View.GONE

                starsView1!!.stopAnim()
                starsView2!!.stopAnim()

            }



        } else if (mode == "angry") {

            runOnUiThread {

                neutralMode = RoboExpressionView.Expressions.ANGRY
                //expView!!.setExpression(RoboExpressionView.Expressions.ANGRY)
                talkMode = RoboExpressionView.Expressions.ANGRY_TALKING

                starsView1!!.visibility = View.GONE
                starsView2!!.visibility = View.GONE

                starsView1!!.stopAnim()
                starsView2!!.stopAnim()

            }



        } else if (mode == "sleep") {

            runOnUiThread {

                neutralMode = RoboExpressionView.Expressions.SLEEP
                //expView!!.setExpression(RoboExpressionView.Expressions.SLEEP)
                talkMode = RoboExpressionView.Expressions.SLEEP

                starsView1!!.visibility = View.GONE
                starsView2!!.visibility = View.GONE

                starsView1!!.stopAnim()
                starsView2!!.stopAnim()

            }



        } else if (mode == "love") {

            runOnUiThread {

                neutralMode = RoboExpressionView.Expressions.LOVE
                //expView!!.setExpression(RoboExpressionView.Expressions.LOVE)
                talkMode = RoboExpressionView.Expressions.LOVE_TALKING

                starsView1!!.visibility = View.GONE
                starsView2!!.visibility = View.GONE

                starsView1!!.stopAnim()
                starsView2!!.stopAnim()


            }



        } else if (mode == "confused") {

            runOnUiThread {

                neutralMode = RoboExpressionView.Expressions.CONFUSED
                //expView!!.setExpression(RoboExpressionView.Expressions.CONFUSED)
                talkMode = RoboExpressionView.Expressions.CONFUSED_TALKING

                starsView1!!.visibility = View.GONE
                starsView2!!.visibility = View.GONE

                starsView1!!.stopAnim()
                starsView2!!.stopAnim()

            }



        }


    }

    var isExpSet = false

    //setup TTS
    private fun setupTTS() {

        TextToSpeechInstance.getInstance(applicationContext)
        TextToSpeechInstance.setProgressListener(object :
            UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

                runOnUiThread {

                    respProgress!!.visibility = View.GONE

                    if (!isExpSet) {

                        isExpSet = true

                        expView!!.setExpression(talkMode)
                    }

                    mLineCount++



                }

            }

            override fun onDone(utteranceId: String?) {

                val wLength = utteranceId!!.split(":")[0].toInt()
                wordLength+= wLength

                Log.d(TAG, "onDone: ${lineCount} ${mLineCount}")

                if (lineCount == mLineCount) {

                    Log.d(TAG, "Doneeeee")

                    runOnUiThread {

                        expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL)
                    }

                    isExpSet = false

                    Handler(Looper.getMainLooper()).postDelayed({

                        isReset = false

                        btnStop!!.visibility = View.GONE

                        animateBottomSheetRoot(isReset)

                        //valueAnim!!.resume()

                        //startSpeechRecognizer()
                                                                },1000)
                }

            }

            override fun onError(utteranceId: String?) {

            }


        })
    }

    private fun sendForSpeech(data:String) {

        TextToSpeechInstance.speak(data,data.length,0)

    }

    //hide systembars and show on swipe
    private fun enterFullScreenMode(window: Window) {


        //for new devices edgeToedge+hide systembars
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.insetsController?.apply {

                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {

            //for older devices fullscreen+hide systembars


            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
        }



    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//
//        finish()
//        startActivity(intent)
//    }



//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//
//        finish()
//        startActivity(intent)
//    }


    override fun onPause() {
        super.onPause()

        Log.d(TAG, "onPause: ")

        SpeechRecognizerInstance.destroyInstance()
        //WhisperInstance.setListener(null)

        TextToSpeechInstance.destroyInstance()

        micToggle!!.isSelected = false

        isReset = true
        animateBottomSheetRoot(isReset)

        buttonWakeUp!!.visibility = View.VISIBLE


    }

    override fun onStop() {
        super.onStop()

        Log.d(TAG, "onStop: ")

        speechHandler.removeCallbacks(speechRunnable!!)

        editTextQuery!!.setText("")

//        dataReceiver?.isStopped = true
        dataReceiver?.dataListener = null //prevent UI updates
        dataReceiver = null
        //isStopped.set(true)

        thinkingRoot!!.post {

            thinkingRoot!!.stopAnim()
        }

        //close drawer if open

        if (drawer!!.isDrawerOpen(Gravity.LEFT)) {

            drawer!!.closeDrawer(Gravity.LEFT)

        }


//set wakeup btn to GONE
        //buttonWakeUp!!.visibility = View.GONE

        //isWakeUp = false //reset isWakeUp

        respProgress!!.visibility = View.GONE //hide circular indeterminate progress

        //reset Thinking animation if it exists
        if (dataSet.size>=1) {

            dataSet.get(index)?.let {

                if (dataSet.get(index).isAnim) {

                    dataSet.get(index).isAnim = false
                    dataSet.get(dataSet.size-1).spannableStringBuilder.clear()

                    runOnUiThread {

                        chatAdapter!!.notifyItemChanged(dataSet.size-1)
                    }


                }


            }


        }

        //hide btnStop(the one visible during response)

        btnStop!!.visibility = View.GONE


       //hide the chatedittext
        isReset = true
        animateBottomSheetRoot(isReset)

        //set robo exp to neutral

        runOnUiThread {

            expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL)
        }

        isExpSet = false

        SpeechRecognizerInstance.destroyInstance()
        //WhisperInstance.setListener(null) // setListeners to null
        //WhisperInstance.stopRecording() //stop recording

        TextToSpeechInstance.destroyInstance() //destroy TTS

        //unbind service


            statusBinder?.setStatusCallback(null)
            //isBound = false
            unbindService(statusServiceConnection!!)
            stopService(statusIntent)

    }

    override fun onDestroy() {
        super.onDestroy()

        SpeechRecognizerInstance.destroyInstance()
        //WhisperInstance.setListener(null)
        //WhisperInstance.stopRecording()
        //WhisperInstance.stopSpeech()
        TextToSpeechInstance.destroyInstance()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {

            if (grantResults.size>0) {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    micToggle!!.isSelected = true

                    isReset = false
                    animateBottomSheetRoot(isReset)

                    buttonWakeUp!!.visibility = View.GONE

                    startSpeechRecognizer()



                    //checkModels()

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (!Settings.canDrawOverlays(this)) {
//                            val intent = Intent(
//                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                                Uri.parse("package:$packageName")
//                            )
//                            startActivityForResult(intent, 1234)
//                        }
//                    }

                   // buttonWakeUp!!.visibility = View.GONE

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

    var toDownload:ArrayList<String> = arrayListOf<String>()

    fun replaceSingleDollars(s: String): String {
//        val sb = StringBuilder()
//        var i = 0
//
//        while (i < s.length) {
//            if (i + 1 < s.length && s[i] == '$' && s[i + 1] == '$') {
//                // Copy $$ as-is
//                sb.append("$$")
//                i += 2
//            } else if (s[i] == '$') {
//                // Replace single $ with $$
//                sb.append("$$")
//                i++
//            } else {
//                sb.append(s[i])
//                i++
//            }
//        }
//
//        return sb.toString()

        var sB = StringBuilder()

        var s1 = ""
        var sIndex = 0

        var index = s.indexOf("$",sIndex)

        if (index!=-1) {

            while (index != -1) {

                if (index + 1 < s.length && s[index + 1] == '$') {
                    // Already $$ → skip both dollars

                    s1 = s.substring(sIndex,index+2)
                    sB.append(s1)

                    sIndex = index+2
                    index = s.indexOf("$", sIndex)
                } else {
                    // Replace only this single $
                    s1 = s.substring(sIndex, index) + "$$"

                    sB.append(s1)

                    sIndex = index+1


                    index = s.indexOf("$", sIndex) // skip the newly inserted $$
                }
            }


        } else {

            return s;
        }


        sB.append(s.substring(sIndex))
        return sB.toString()
    }

    private inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.single_item_chat_question, parent, false)
            )

        }

        override fun getItemCount(): Int {

            return dataSet.size

        }

//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//            val tv = holder.textView
//
//            val tag = "md-$position"
//            tv.tag = tag
//            //tv.tag = "md-$position"
//
//// 1) clear previous content immediately
//            tv.text = null              // remove old text & spans
//            tv.visibility = View.VISIBLE
//
//
//
//
//            if (dataSet.get(position).isQuestion) {
//
//                holder.logo.visibility = View.VISIBLE
//
//                holder.logo.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        this@GeneralActivity,
//                        R.drawable.panda
//                    )
//                )
//
//                tv.text = dataSet[position].spannableStringBuilder
//
//
//            } else if (!dataSet.get(position).isQuestion) {
//
//                if (dataSet.get(position).isImage) {
//
//                    holder.logo.visibility = View.VISIBLE
//
//                    holder.logo.setImageDrawable(
//                        ContextCompat.getDrawable(
//                            this@GeneralActivity,
//                            R.drawable.panda_1
//                        )
//                    )
//
//
//                } else {
//
//                    holder.logo.visibility = View.GONE
//
//
//                }
//
//
//                if (!dataSet.get(position).isProcess) {
//
//                    if (dataSet.get(position).isPage) {
//
//                        tv.setText(dataSet[position].spannableStringBuilder)
//
//                        tv.setTextColor(Color.BLUE)
//                        tv.movementMethod = LinkMovementMethod.getInstance()
//
//
//                    } else {
//
////                        val currentPosition = holder.adapterPosition
////                        if (currentPosition == RecyclerView.NO_POSITION) return
////
////                        val markdown =
////                            dataSet[currentPosition].spannableStringBuilder.toString()
////
////                        tv.text = null
////                        tv.visibility = View.VISIBLE
////
////                        markWon?.setMarkdown(tv, markdown)
//
//                        if (dataSet.get(position).renderedMarkDown!=null) {
//
//                            tv.post {
//
//                                if (tv.tag.equals(tag)) {
//
//                                    markWon!!.setParsedMarkdown(tv,dataSet.get(position).renderedMarkDown!!)
//
//                                }
//
//
//                                //dataSet.get(position).renderedMarkDown=null
//
//                            }
//
//                            //markWon!!.setParsedMarkdown(tv,dataSet.get(position).renderedMarkDown!!)
//
//                            Log.d(TAG, "onBindViewHolder: ")
//
//
//
//                            //dataSet.get(position).renderedMarkDown = markWon!!.toMarkdown(dataSet.get(position).spannableStringBuilder.toString())
//
////                            tv.text = dataSet.get(position).renderedMarkDown
//
//                        } else {
//
//                            //tv.text = dataSet.get(position).spannableStringBuilder
//
//
//                        }
//
//
//
//                        tv.setTextColor(
//                            ContextCompat.getColor(this@GeneralActivity, R.color.white)
//                        )
//
//
//                        //val markdown = dataSet[position].spannableStringBuilder.toString()
//
//// 3) post the work to main thread (ensures view is attached)
//
////                        tv.post {
////                            // ⛔ View was recycled → STOP
////                            if (tv.tag != tag) return@post
////
////                            markWon?.setMarkdown(tv, markdown)
////
////                            tv.setTextColor(
////                                ContextCompat.getColor(this@GeneralActivity, R.color.white)
////                            )
////                        }
////                        tv.post {
////                            // If your Markwon instance is markWon:
////                            markWon?.setMarkdown(tv, markdown)
////
////                            // Set color AFTER applying markdown only if you want to set the default color
////                            // (spans inside markdown will still override foreground where present)
////                            tv.setTextColor(ContextCompat.getColor(this@GeneralActivity, R.color.white))
////                        }
//
//
//
////                        holder.textView.post {
////
////                            markWon!!.setMarkdown(holder.textView,dataSet[position].spannableStringBuilder.toString())
////                            holder.textView.setTextColor(ContextCompat.getColor(this@GeneralActivity,R.color.white))
////
////
////                        }
//
//                    }
//
//                }
//
//            }
//
////            holder.textView.setOnClickListener {
////
////                if (holder.textView.text.trim().startsWith("Download Pdf")) {
////
////                    var file = Utils.generatePDF3(this@GeneralActivity
////                        ,pdfMap.get(dataSet.get(position).quesNo).toString())
////
////                    var intent = Intent(this@GeneralActivity,PdfActivity::class.java)
////                    intent.putExtra("pdfFile",file)
////                    startActivity(intent)
////
//////                    var intent:Intent = Intent(this@GeneralActivity,SampleActivity::class.java)
//////                    intent.putExtra("data",renderedMarkdown)
//////                    startActivity(intent)
////
////                }
////
////            }
//
//            if (dataSet.get(position).isAnim) {
//
//                if (position == index) {
//
//                    initAnimator(holder.textView, dataSet.get(position).isAnim)
//                }
//            } else {
//
//                   initAnimator(holder.textView, dataSet.get(position).isAnim)
//                   holder.textView.alpha = 1f
//
//            }
//
//
//        }

//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//            val tv = holder.textView
//            val item = dataSet[position]
//
//            val tag = "md-$position"
//            tv.tag = tag
//
//            // Reset view state (VERY important for RecyclerView)
//            tv.text = null
//            //tv.setTextColor(ContextCompat.getColor(this@GeneralActivity, R.color.white))
//            //tv.movementMethod = null
//            tv.visibility = View.VISIBLE
//
//            // ---------------- LOGO ----------------
//            if (item.isQuestion) {
//                holder.logo.visibility = View.VISIBLE
//                holder.logo.setImageResource(R.drawable.panda)
//            } else if (item.isImage) {
//                holder.logo.visibility = View.VISIBLE
//                holder.logo.setImageResource(R.drawable.panda_1)
//            } else {
//                holder.logo.visibility = View.GONE
//            }
//
//            // ---------------- SIMPLE TEXT CASES ----------------
//            if (item.isQuestion) {
//                tv.text = item.spannableStringBuilder
//                //return
//            }
//
//            if (item.isPage) {
//                tv.text = item.spannableStringBuilder
//                tv.setTextColor(Color.BLUE)
//                tv.movementMethod = LinkMovementMethod.getInstance()
//
//                // ---------------- CLICK ----------------
//                tv.setOnClickListener {
//                    if (tv.text?.trim()?.startsWith("Download Pdf") == true) {
//
//                        val file = Utils.generatePDF3(
//                            this@GeneralActivity,
//                            pdfMap.get(item.quesNo)!!
//                        )
//
//                        val intent = Intent(this@GeneralActivity, PdfActivity::class.java)
//                        intent.putExtra("pdfFile", file)
//                        startActivity(intent)
//                    }
//                }
//
//                //return
//            }
//
////            if (item.isProcess) {
////                tv.text = item.spannableStringBuilder
////               // return
////            }
//
//            // ---------------- MARKDOWN CASE ----------------
//            val renderedMarkdown = item.renderedMarkDown
//
//            if (renderedMarkdown != null) {
//
//                // IMPORTANT: only Markwon writes to TextView
//                tv.post {
//                    if (tv.tag != tag) return@post
//                    markWon!!.setParsedMarkdown(tv, renderedMarkdown)
//                }
//
//            } else {
//                // Fallback: plain text only
//                tv.text = item.spannableStringBuilder
//            }
//
//
//
//            // ---------------- ANIMATION ----------------
//            if (item.isAnim && position == index) {
//                initAnimator(tv, true)
//            } else {
//                initAnimator(tv, false)
//                tv.alpha = 1f
//            }
//        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val tv = holder.textView
            val item = dataSet[position]

            val tag = "md-$position"
            tv.tag = tag

            // Reset
            tv.text = ""
            tv.movementMethod = null
            tv.visibility = View.VISIBLE

            // ---------------- LOGO ----------------
            when {
                item.isQuestion -> {
                    holder.logo.visibility = View.VISIBLE
                    holder.logo.setImageResource(R.drawable.panda)
                }
                item.isImage -> {
                    holder.logo.visibility = View.VISIBLE
                    holder.logo.setImageResource(R.drawable.panda_1)
                }
                else -> holder.logo.visibility = View.GONE
            }

            //------------------ERROR----------------

            if (item.isError) {

                tv.setText(item.spannableStringBuilder,TextView.BufferType.SPANNABLE)
                //tv.setTextColor(Color.RED)
                return
            }

            // ---------------- PAGE ----------------
            if (item.isPage) {

                tv.setText(item.spannableStringBuilder,TextView.BufferType.SPANNABLE)
                //tv.setTextColor(Color.BLUE)

                tv.setOnClickListener {
                    if (tv.text?.trim()?.startsWith("Download Pdf")!!) {

                        try {

                            val file = Utils.generatePdfNew1(
                                this@GeneralActivity,
                                pdfMap[item.quesNo]!!
                            )
                            startActivity(
                                Intent(this@GeneralActivity, PdfActivity::class.java)
                                    .putExtra("pdfFile", file)
                            )

                        } catch (ex:Exception) {

                            ex.printStackTrace()


                        }

                    }
                }
                return
            }

            // ---------------- MARKDOWN (ONLY PATH) ----------------
            val markdown = item.renderedMarkDown

            if (markdown != null) {
                tv.post {
                    if (tv.tag != tag) return@post
                    markWon!!.setParsedMarkdown(tv, markdown)
                }
            } else {

                tv.setText(item.spannableStringBuilder)

            }

            // ---------------- ANIMATION ----------------
            if (item.isAnim && position == index) {

                initAnimator(tv, true)
            } else {
                initAnimator(tv, false)
                tv.alpha = 1f
            }
        }



        //val textAnimator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f)

        private fun initAnimator(textView: TextView,isStart:Boolean) {

            if (isStart) {

                if (valueAnim == null) {

                    valueAnim = MyValueAnimator(1f,0f,1f)

                }

                valueAnim!!.setMyAnimListener(object : MyValueAnimator.AnimListener{
                    override fun onValueReceived(value: Float) {

                        textView.alpha = value

                    }


                })

                valueAnim!!.start()

//                Log.d(TAG, "initAnimator: ")
//
//                textAnimator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f).apply {
//
//                    duration = 700
//                    repeatCount = ObjectAnimator.INFINITE
//
//                    addUpdateListener {
//
//                        textView.alpha = it.animatedValue as Float
//                    }
//
//
//                }
//
//                textAnimator?.addListener(object : Animator.AnimatorListener {
//                    override fun onAnimationStart(animation: Animator) {
//
//                    }
//
//                    override fun onAnimationEnd(animation: Animator) {
//
//                        textView.alpha = 1f
//                        textAnimator = null
//
//                    }
//
//                    override fun onAnimationCancel(animation: Animator) {
//
//                    }
//
//                    override fun onAnimationRepeat(animation: Animator) {
//
//                    }
//
//
//                })
//
//                textAnimator?.start()

            } else {

                valueAnim?.let {

                    valueAnim!!.pause()
                }

//                Log.d(TAG, "initAnimatorCancel: ")
//
//                textAnimator?.cancel()

            }

        }


        override fun getItemId(position: Int): Long {

            return dataSet.get(position).id
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

            var textView: TextView = v.findViewById(R.id.text_query)
            var logo: ImageView = v.findViewById(R.id.text_logo)

        }

    }

    val respBuilder = StringBuilder()

    //dataListener functions(onDataReceived(success),onError(exception))

    override suspend fun onDataReceived(line: String?,isEnded:Boolean) {

        runOnUiThread {

            thinkingRoot!!.post {

                thinkingRoot!!.stopAnim()
            }



            line?.let {

                //reset Thinking anim
                if (dataSet.get(index).isAnim) {


                    dataSet.get(index).spannableStringBuilder.clear()
                    dataSet.get(index).isAnim = false

                    chatAdapter!!.notifyItemChanged(index)

                }

                //show valid lines(!Ended means valid lines)

                if (!isEnded) {

                    respBuilder.append(line)
                    var s = respBuilder.toString()

                        val tMatcher = thinkPattern!!.matcher(s) //detect <think>...</think>
                        val mMatcher = modePattern!!.matcher(s) //detect <mode>...</mode>

                        if (tMatcher.find()) {

                            //replace think and update counter
                            s = s.replace(s.substring(tMatcher.start(),tMatcher.end()),"")
                            mCount++
                        }

                        //find mode tag and extract the text within
                        if (mMatcher.find()) {

                            modee = mMatcher.group(1) //extract the text within
                            Log.d(TAG, "Mode: " + modee)
                            s = s.replace(mMatcher.group(0)!!,"")//replace the whole line with ""

                            if (mCount<1) {

                                mCount++// update count
                                setMode(modee!!) //set robo exp based on mode


                            }

                        }

                        //replace [,], [, ],(,), (, ) with $$

                        if (mCount == 1) {

                            s = Utils.normalizeLatexDelimiters(s)

//                        s = s.replace("\\[","$$")
//                        s = s.replace("\\[ ","$$")
//                        s = s.replace("\\]","$$")
//                        s = s.replace(" \\]","$$")
//                        s = s.replace("\\(","$")
//                        s = s.replace("\\( ","$")
//                        s = s.replace("\\)","$")
//                        s = s.replace(" \\)","$")
                        s = replaceSingleDollars(s) //replace single dollars with double dollars

                            val qRegex = Regex("<sugq>(.*)</sugq>",RegexOption.DOT_MATCHES_ALL)

                            val m:MatchResult? = qRegex.find(s);

                            if (m!=null) {

                                var sugq:String = "**Suggested questions:**\n\n${m.value}"
                                Log.d(TAG, "Suggestions: "+sugq)
                                sugq = sugq.replace("<sugq>","-")
                                    .replace("</sugq>","")

                                s = s.replace(qRegex) {
                                    sugq
                                }

                            }

                            val node = markWon!!.parse(s)


                            Log.d(TAG, "onDataReceived: "+ s)

                            //add the string to dataset

                            dataSet.get(index).renderedMarkDown = markWon!!.render(node)
                            chatAdapter!!.notifyItemChanged(index)

//                            dataRecycler!!.post {
//
//                                chatAdapter!!.notifyItemChanged(index)
//
//
//                            }



                            //speech regex(replaces math latex,<page> tag etc for speech)

                            //val regex = Regex("""(^\s*\|[-\s|]+\|\s*$)|([*#]+\s*)|(<page>\[[0-9]+\]</page>)|([-]+)|(\[.*?])|(\^)|(\bsqrt\b)|([=+\-*/<>\]\[])|([∑∫π√])|\$|\$\$|\\frac|`+|<sugq>|</sugq>""", RegexOption.MULTILINE)

                            //var qList = renderedMarkdown.toString().split(Regex("\\s{2,}",RegexOption.DOT_MATCHES_ALL))

                            pdfMap.get(quesNo)!!.second.clear().append(respBuilder.toString())

//                            sendForSpeech(Utils.markdownToTts(s))//replace the speech regex with ""
                            //lineCount++ // increment lineCount(will be used in utteranceProgressListener)

                        }



                } else {

                    //if Ended the show Download PDF

                    Log.d(TAG, "Ended")

                    var s = "Download Pdf"

                    sendForSpeech(Utils.markdownToTts(pdfMap.get(quesNo)?.second.toString()))//replace the speech regex with ""
                    lineCount++

                    dataSet.add(
                        DataSet(
                            System.currentTimeMillis(),
                            MySpannableStringBuilder(s).apply {

                                setSpan(ForegroundColorSpan(Color.BLUE),0,s.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

                            }

                            ,
                            false,
                            null,
                            false,
                            false,
                            true,
                            quesNo = quesNo
                        )
                    )

                    chatAdapter!!.notifyItemInserted(dataSet.size-1)

//                    dataRecycler!!.post {
//
//                        chatAdapter!!.notifyItemInserted(dataSet.size-1)
//
//                    }



                }

            }


        }




        //--------Old---------//

//            var s = line
//
//            //reset Thinking anim
//            if (dataSet.get(index).isAnim) {
//
//                withContext(Dispatchers.Main) {
//
//                    isAnim = false
//                    dataSet.get(index).spannableStringBuilder.clear()
//                    dataSet.get(index).isAnim = false
//                    chatAdapter!!.notifyItemChanged(index)
//
//
//                }
//
//            }
//
//        //show valid lines(!Ended means valid lines)
//
//            if (!line.equals("Ended")) {
//
//                withContext(Dispatchers.Main) {
//
//                    var tMatcher = thinkPattern!!.matcher(line) //detect <think>...</think>
//                    var mMatcher = modePattern!!.matcher(line) //detect <mode>...</mode>
//
//                    if (tMatcher.find()) {
//
//                        //replace think and update counter
//                        s = line.replace(line.substring(tMatcher.start(),tMatcher.end()),"")
//                        mCount++
//                    }
//
//                    //find mode tag and extract the text within
//                    if (mMatcher.find()) {
//
//                        modee = mMatcher.group(1) //extract the text within
//                        Log.d(TAG, "Mode: " + modee)
//                        s = line.replace(mMatcher.group(0)!!,"")//replace the whole line with ""
//                        mCount++// update count
//                        setMode(modee!!) //set robo exp based on mode
//                    }
//
//                    //replace [,], [, ],(,), (, ) with $$
//
//                    if (mCount == 1) {
//
//                        s = Utils.normalizeLatexDelimiters(s)
//
////                        s = s.replace("\\[ ","$$ ")
////                        s = s.replace("\\[","$$ ")
////                        s = s.replace(" \\]","$$ ")
////                        s = s.replace("\\]","$$ ")
////                        s = s.replace("\\( ","$$ ")
////                        s = s.replace("\\(","$$ ")
////                        s = s.replace(" \\)"," $$")
////                        s = s.replace("\\)"," $$")
//                        s = replaceSingleDollars(s) //replace single dollars with double dollars
//
//                        var qRegex = Regex("<sugq>(.*)</sugq>",RegexOption.DOT_MATCHES_ALL)
//
//                        var m:MatchResult? = qRegex.find(s);
//
//                        if (m!=null) {
//
//                            var sugq:String = "**Suggested questions:**\n\n${m.value}"
//                            Log.d(TAG, "Suggestions: "+sugq)
//                            sugq = sugq.replace("<sugq>","-")
//                                .replace("</sugq>","")
//
//                            s = s.replace(qRegex) {
//                                sugq
//                            }
//
//                        }
//
//                        val node = markWon!!.parse(s)
//
//
//                        Log.d(TAG, "onDataReceived: "+ s)
//
//                        //add the string to dataset
//
//                        dataSet.add(
//                            DataSet(
//                                System.currentTimeMillis(),
//                                MySpannableStringBuilder("").apply {
//
//                                    append(s)
//
//                                },
//                                false,
//                                null,
//                                false,
//                                false,
//                                quesNo = quesNo,
//                                renderedMarkDown = markWon!!.render(node)// will be used in
//                            // adapter onBindViewHolder to render markdown
//
//                                )
//                        )
//
//                        Log.d(TAG, "onDataReceived: "+renderedMarkdown.toString())
//
//
//
//                        chatAdapter!!.notifyItemInserted(dataSet.size-1)
////
//
//                        //Log.d("abcde", "processData1: " + dataSet.get(dataSet))
//
//                        isOnGoing = true
//
//
//
//                        //speech regex(replaces math latex,<page> tag etc for speech)
//
//                        //val regex = Regex("""(^\s*\|[-\s|]+\|\s*$)|([*#]+\s*)|(<page>\[[0-9]+\]</page>)|([-]+)|(\[.*?])|(\^)|(\bsqrt\b)|([=+\-*/<>\]\[])|([∑∫π√])|\$|\$\$|\\frac|`+|<sugq>|</sugq>""", RegexOption.MULTILINE)
//
//                        //var qList = renderedMarkdown.toString().split(Regex("\\s{2,}",RegexOption.DOT_MATCHES_ALL))
//
//
//                        pdfMap!!.get(quesNo)!!.append(s)
//
//                        sendForSpeech(Utils.markdownToTts(s))//replace the speech regex with ""
//                        lineCount++ // increment lineCount(will be used in utteranceProgressListener)
//
//                    }
//
//                }
//
//            } else {
//
//                //if Ended the show Download PDF
//
//                withContext(Dispatchers.Main) {
//
//                    dataSet.add(
//                        DataSet(
//                            System.currentTimeMillis(),
//                            MySpannableStringBuilder("").apply {
//
//                                append("Download Pdf")
//
//                            },
//                            false,
//                            null,
//                            false,
//                            false,
//                            true,
//                            quesNo = quesNo
//                        )
//                    )
//
//                    chatAdapter!!.notifyItemInserted(dataSet.size-1)
//
//
//                }
//
//
//            }

        //--------Old-----------//





    }

    //response error function

    override suspend fun onError(ex: Exception) {

        runOnUiThread {

            thinkingRoot!!.post {

                thinkingRoot!!.stopAnim()
            }

            //reset the Thinking anim
            if (dataSet.get(index).isAnim) {

                dataSet.get(index).spannableStringBuilder.clear()
                dataSet.get(index).isAnim = false
                dataSet.get(index).isError = true
                dataSet.get(index).spannableStringBuilder.append("Error : ${ex.message}")
            dataSet.get(index).spannableStringBuilder.setSpan(
                ForegroundColorSpan(Color.RED)
                ,0,dataSet.get(index).spannableStringBuilder.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)


                chatAdapter!!.notifyItemChanged(index)

            } else {

                //show a toast

                val errorStr = "Error : ${ex.message}"

                dataSet.add(DataSet(System.currentTimeMillis()
                    , MySpannableStringBuilder(errorStr).apply {

                        setSpan(ForegroundColorSpan(Color.RED),0,errorStr.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)



                    }
                    ,false,null,false,false,isAnim = false, isError = true))

                chatAdapter!!.notifyItemInserted(dataSet.size-1)

                //Toast.makeText(this@GeneralActivity,"Connect Error : ${ex.message}",Toast.LENGTH_SHORT).show()



            }


        }



    }

    override suspend fun onRestart(msg: String) {

        runOnUiThread {

            thinkingRoot!!.post {

                thinkingRoot!!.stopAnim()
            }

            //reset the Thinking anim
            if (dataSet.get(index).isAnim) {

                dataSet.get(index).spannableStringBuilder.clear()
                dataSet.get(index).isAnim = false
                dataSet.get(index).isError = true
                dataSet.get(index).spannableStringBuilder.append("$msg")
                dataSet.get(index).spannableStringBuilder.setSpan(
                    ForegroundColorSpan(Color.RED)
                    ,0,dataSet.get(index).spannableStringBuilder.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)


                chatAdapter!!.notifyItemChanged(index)

            } else {

                //show a toast

                val errorStr = "$msg"

                dataSet.add(DataSet(System.currentTimeMillis()
                    , MySpannableStringBuilder(errorStr).apply {

                        setSpan(ForegroundColorSpan(Color.RED),0,errorStr.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)



                    }
                    ,false,null,false,false,isAnim = false, isError = true))

                chatAdapter!!.notifyItemInserted(dataSet.size-1)

                //Toast.makeText(this@GeneralActivity,"Connect Error : ${ex.message}",Toast.LENGTH_SHORT).show()



            }


        }


    }

    //var isWakeUp = false
    var statusIntent:Intent?=null




    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume: ")

        statusIntent = Intent(this, StatusService::class.java)
        startService(statusIntent)

        //isWakeUp = true

        status = ""

        //buttonWakeUp!!.visibility = View.VISIBLE

        //bind to service when activity resumes


            //var statusIntent = Intent(this,StatusService::class.java)
        bindService(statusIntent!!,statusServiceConnection!!, Context.BIND_AUTO_CREATE)



        //WhisperInstance.setListener(this) //set listener
    }



    var isServiceAlert = false
    var statusBinder:StatusService.StatusBinder?=null
    var status:String?=null


    private inner class StatusServiceConnection: ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            Log.d(TAG, "onServiceConnected: ")



            statusBinder = service as StatusService.StatusBinder
            statusBinder!!.setStatusCallback {

                runOnUiThread {

                    if(it.equals("down")) {

                        status = it

                        dataReceiver?.dataListener = null //set dataListener to
                        // null so no updates to UI

                        dataReceiver = null

//

                        //if screen on show a "service not available" dialog
                        if (!isTurnedOff && !isServiceAlert) {

                            isServiceAlert = true

                            showServiceAlert()// show service not available dialog




                                // set exp to neutral
                                expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL)


                            //if Thinking anim running stop and reset the flags

                            if (dataSet.get(index).isAnim) {

                                dataSet.get(index).isAnim = false

                                runOnUiThread {

                                    //clear the Thinking text
                                    dataSet.get(index).spannableStringBuilder.clear()

                                    chatAdapter!!.notifyItemChanged(index)

                                }


                            }


                        }

                        //when status is up

                    } else if (it.equals("up")){

                        status = it //store in status

                        if (isTurnedOff && overlay!=null) {  //turn screen on only when its off

//                        runOnUiThread {
//
//                            Handler().postDelayed({
//
//                                //buttonWakeUp!!.visibility = View.VISIBLE
//
//                                //startSpeechRecognizer()
//
//                            },2000)
//
//                        }

                            isTurnedOff = false

                            overlay!!.hide()
                            overlay = null



                        }

                    }


                }

                //if status down

            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {

            Log.d(TAG, "onServiceDisconnected: "+isTurnedOff)

        }


    }

    //service alert function

    private fun showServiceAlert() {

        runOnUiThread{

            var aBuilder = AlertDialog.Builder(this@GeneralActivity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Alert")
                .setMessage("Service unavailable. Please try again")
                .setPositiveButton("Ok",object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        dialog!!.dismiss()
                    }

                })

            var alertDialog = aBuilder.create()
            alertDialog.show()


        }

    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    //resultreceiver for transcription


    var modelDialog:Dialog?=null
    var progressBar:ProgressBar?=null
    var progressText:TextView?=null
    var progressMsg:TextView?=null
    var titleText:TextView?=null

    override fun onDownloadStarted() {

        modelDialog = CustomDialogBox()
            .buildDialog(this@GeneralActivity,R.layout.layout_download_model)
            .setSize((Utils.getScreenWidth(this@GeneralActivity)*0.60).toInt(),(Utils.getScreenHeight(this@GeneralActivity)*0.40).toInt())
            .setCancelable(false)
            .createDialog()

        modelDialog!!.show()

        var okBtn = modelDialog!!.findViewById<Button>(R.id.btnOk)
        okBtn.visibility = View.INVISIBLE


        progressBar = modelDialog!!.findViewById<ProgressBar>(R.id.progressRing)
        progressMsg = modelDialog!!.findViewById<TextView>(R.id.progress_msg)
        progressText = modelDialog!!.findViewById(R.id.progress_text)

        titleText = modelDialog!!.findViewById<TextView>(R.id.titleText)
        titleText!!.visibility = View.INVISIBLE

        progressBar!!.visibility = View.VISIBLE
        progressMsg!!.visibility = View.VISIBLE
        progressText!!.visibility = View.VISIBLE

        progressBar!!.setProgress(0)
        progressMsg!!.text = "0"
        progressText!!.text = "Downloading..."


    }

    override fun onDownloadComplete(result:Int) {

        runOnUiThread {

            progressText!!.text = "Success"

            if (result == toDownload.size) {

                toDownload.clear()

                Handler().postDelayed({

                    modelDialog?.dismiss()


//                    WhisperInstance.getInstance(this)
//                    WhisperInstance.setListener(this)


                },1000)



                //load Model
            }

        }




    }

    override fun onError() {

        runOnUiThread {

            progressText!!.text = "Download failed"
            toDownload.clear()

            Handler().postDelayed({

                modelDialog?.dismiss()

            },1000)

        }

    }

    override fun onProgress(progress: Float) {

        runOnUiThread {

            progressBar!!.progress = progress.toInt()
            progressMsg!!.text = progress.toInt().toString()

        }

    }


}