package com.ai.roboteacher.activities

import CustomAlertDialogBuilder
import CustomDialogBox
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ai.roboteacher.ChatEditTextView
import com.ai.roboteacher.DataSet
import com.ai.roboteacher.KtorDataReceiver
import com.ai.roboteacher.LinearLayoutManagerWrapper
import com.ai.roboteacher.ModelDownloadTask
import com.ai.roboteacher.MySpannableStringBuilder
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.SpeechRecognizerInstance
import com.ai.roboteacher.StatusService
import com.ai.roboteacher.TextToSpeechInstance
import com.ai.roboteacher.Utils

import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class ChatActivity:AppCompatActivity(),KtorDataReceiver.KtorDataListener
,ModelDownloadTask.ModelDownloadListener{



    private val TAG = ChatActivity::class.java.name

    var starsCount:Int = 0
    var lineCount = 0
    var mLineCount = 0
    private var statusServiceConnection:StatusServiceConnection?=null


    var ansDataList:ArrayList<MySpannableStringBuilder?>? = ArrayList()

    var onGoingIndex = -1

    var headerArr:Array<String> = Array(5,{

        ""
    })

    var subHeaderArr:Array<String> = Array(5,{

        ""
    })


    lateinit var chatEditText: ChatEditTextView


    var dataSet: ArrayList<DataSet> = ArrayList<DataSet>()

    var index: Int = -1
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var textAnimator: ObjectAnimator? = null

//    private var isAnim: Boolean = false
    private var isSpeech: Boolean = true
    private var dropDownArrow:ImageView?=null


    val baseMargin = 5
    var markWon:Markwon?=null
    //private var isStopped:AtomicBoolean = AtomicBoolean(false)
    private var mainLayout:ConstraintLayout?=null

    private var headerLabel:TextView?=null
    private var subHeaderLabel:TextView?=null
    private var matrix:Matrix = Matrix()
    private var constraintSet:ConstraintSet?= ConstraintSet()
    private var isFlow:Boolean = false
    private var isCountDownStarted:Boolean = false
    private var countDownProgress:ProgressBar?=null
    private var countDownLayout:View?=null
    private var progressText:TextView?=null

    private var bundle:Bundle?=null
    private var classId = 0
    private var subject:String?=null
    private var isChecked = false
    private var voiceSwitch:Switch?=null
    private var speechRunnable:Runnable?=null
    private var speechHandler:Handler = Handler()
    var isBound = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // enable edgeToEdge functionality(full screen)
        enterFullScreenMode(window)
        setContentView(R.layout.activity_chat)

        statusServiceConnection = StatusServiceConnection()


        //initialize Whisper
//        WhisperInstance.getInstance(this)
//        WhisperInstance.setListener(this)

        //initModel()



        speechRunnable = object :java.lang.Runnable{
            override fun run() {


                SpeechRecognizerInstance.destroyInstance()

                respStrBuilder.clear()


                    if (!chatEditText!!.text.toString().isEmpty()) {

                        //valueAnim!!.resume()

                        if (status.equals("up")) { //only send request when service is up

                            nStrBuilder.clear()

                            isServiceAlert = false
                            processData2(chatEditText.text.toString()) // send request
                            chatEditText!!.setText("") //reset editText

                        } else {

                            //reset chatEditText buttons

                            chatEditText.isSelected = false
                            chatEditText.setDefaultState()

                            //alert dialog

                            Toast.makeText(this@ChatActivity,"Service not available.Please try again",Toast.LENGTH_SHORT).show()
                        }

                    } else {

                        chatEditText.isSelected = false
                        chatEditText.setDefaultState()


                    }



            }

        }

        //get class and subject from SelectClassActivity intent

        bundle = intent.extras!!
        classId = bundle!!.getString("class")!!.toInt()
        subject = bundle!!.getString("subject")

        Log.d(ChatActivity::class.java.name, "onCreate: ${classId} ${subject}")

        //markPattern = Pattern.compile(markRegex)




        headerArr[0] = "Welcome to your %s class with %s."
        headerArr[1] = "You're all set for %s Literature with %s."
        headerArr[2] = "Your %s session with %s is ready."
        headerArr[3] = "Time to start %s with %s."
        headerArr[4] = "Get ready to explore %s today with %s."

        subHeaderArr[0] = "Let's begin your learning journey."
        subHeaderArr[1] = "Let’s dive in!"
        subHeaderArr[2] = " Let’s make it count!"
        subHeaderArr[3] = "You’ve got this!"


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {

                val window = Rect()
                v.getWindowVisibleDisplayFrame(window)

                var editTextRect = Rect()
                chatEditText.getGlobalVisibleRect(editTextRect)

                val diff = editTextRect.bottom - window.bottom

                if (diff > 0) {
                    v.scrollBy(0, diff.plus(baseMargin*resources.displayMetrics.density.toInt()))
                }
            } else {

                v.scrollTo(0,0)
            }
            insets
        }

        countDownLayout = findViewById(R.id.count_down_layout)
        countDownProgress = countDownLayout!!.findViewById(R.id.progressRing)
        progressText = countDownLayout!!.findViewById(R.id.progress_text)
        voiceSwitch = findViewById(R.id.voice_switch)

        voiceSwitch!!.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

                if(isChecked) {

                    isSpeech = true

                } else {

                    isSpeech = false
                }
            }


        })


        intent?.let {

            isFlow = bundle!!.getBoolean("isFlow")

            if (isFlow) {

                val customAlertDialogBuilder:CustomAlertDialogBuilder = CustomAlertDialogBuilder(object:CustomAlertDialogBuilder.DialogButtonClickListener{
                    override fun onClickPositiveButton(dialog:DialogInterface) {

                        dialog.dismiss()


                    }


                })
                customAlertDialogBuilder.buildAlertDialog(this@ChatActivity
                    ,"Hello!"
                    ,"Class has been started"
                , R.drawable.panda).show()

                lifecycleScope.launch {

                   // while (true) {

                        var sdf:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                        var date:Date? = sdf.parse("${bundle!!.getString("date")}T${bundle!!.getString("end_time")}")
                        val tmpStmp:Long? = date?.time

                        val delayTime = (tmpStmp!!-60000) - System.currentTimeMillis()

                        delay(delayTime)

                        if (!isCountDownStarted) {

                            isCountDownStarted = true

                            //launch Countdown

                            lifecycleScope.launch {

                                initiateCountDown()

                            }
                        }

                        //Log.d(ChatActivity::class.java.name, "FutureTimeStamp ${System.currentTimeMillis()-tmpStmp!!}")
                        //Log.d(ChatActivity::class.java.name, "FutureTimeStamp ${System.currentTimeMillis()-tmpStmp!!}")

//                        if (Math.abs(tmpStmp!!-System.currentTimeMillis())<=60000) {
//
//                            if (!isCountDownStarted) {
//
//                                isCountDownStarted = true
//
//                                //launch Countdown
//
//                                lifecycleScope.launch {
//
//                                    initiateCountDown()
//
//                                }
//                            }
//
//                            break
//
//                        }


                        //delay(5000)


                    //}

                }
            }
        }

//        markWon = Markwon.builder(this)
////            .usePlugin(ImagesPlugin.create(ImagesPlugin.))
//            .usePlugin(TablePlugin.create(this)) // <-- important
//            .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
//            .usePlugin((JLatexMathPlugin.create(16f)))
//            .build()

        //for markdown
        markWon = Markwon.builder(this)
            .usePlugin(MarkwonInlineParserPlugin.create())
//            .usePlugin(ImagesPlugin.create(ImagesPlugin.))
            .usePlugin(TablePlugin.create(this)) // <-- important
            .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
            .usePlugin((JLatexMathPlugin.create(16f,object:JLatexMathPlugin.BuilderConfigure{
                override fun configureBuilder(builder: JLatexMathPlugin.Builder) {

                    builder.inlinesEnabled(true)

                    builder.blocksLegacy(true);

                    builder.blocksEnabled(true);
                }


            })))
            .build()



        //initialize TTS
        TextToSpeechInstance.getInstance(applicationContext)
        TextToSpeechInstance.setProgressListener(object :
            UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

                //increment mLineCount when speech starts(will be used when speech done)

                mLineCount++

                val indexes = utteranceId!!.split(":") //get
                // index from utteranceID which was sent to speechqueue earlier

                //keep track of onGoingIndex
                onGoingIndex = indexes[0].toInt()

                //chatEditText.isSelected = true

                runOnUiThread {

                    chatRecyclerView.post {

                        //change is process flag of dataset to
                        // change the text color while in speech

                        dataSet.get(indexes[0].toInt()).isProcess = true

                        chatAdapter.notifyItemChanged(indexes[0].toInt())

                        chatRecyclerView.scrollToPosition(indexes[0].toInt())
                    }

                }
            }

            override fun onDone(utteranceId: String?) {


                val indexes = utteranceId!!.split(":")

                //reset isProcess flag to change the textcolor back to white

                runOnUiThread {

                    chatRecyclerView.post {

                        dataSet.get(indexes[0].toInt()).isProcess = false
                        chatAdapter.notifyItemChanged(indexes[0].toInt())


                    }
                }

                //when lineCount equals mLine count it implies
                // all speeches are complete,hence reset chatedittext


                if (lineCount == mLineCount) {

                    onGoingIndex=-1

                    //disable stop button

                    runOnUiThread {

                        chatEditText.isSelected = false
                        chatEditText.setDefaultState()

                    }

                }

            }

            override fun onError(utteranceId: String?) {

            }

        })


        this@ChatActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

//        val gestureDetector:GestureDetector = GestureDetector(this,SwipeGestureListener(
//
//            {direction, velocityX, velocityY ->
//
//            when(direction) {
//
//                Direction.RIGHT-> {
//
//                    if (intent.hasExtra("source")) {
//
////                        val intent = Intent(this, MainActivity::class.java)
////                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
////                        startActivity(intent)
//
//
//                    } else {
//
//                        finish()
//
////                        val intent = Intent(this, SpikeActivity::class.java)
////                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
////                        startActivity(intent)
//                    }
//
//
//
//                }
//
//                Direction.TAP_UP->{
//
//                    hideKeyBoard()
//
//                }
//
//                else-> {
//
//
//            }
//
//
//            }
//        },applicationContext))


        mainLayout = findViewById(R.id.main)
        dropDownArrow = findViewById(R.id.dropdown_arrow)
        headerLabel = findViewById(R.id.header_label)
        subHeaderLabel = findViewById(R.id.sub_header_label)


        countDownLayout = findViewById(R.id.count_down_layout)
        countDownProgress = countDownLayout!!.findViewById(R.id.progressRing)
        progressText = countDownLayout!!.findViewById(R.id.progress_text)

        val pandaIcon:ImageView = findViewById(R.id.yellow_panda_menu)

        //back button logic
        pandaIcon.setOnClickListener {

            isChecked = !isChecked

            val stateSet = intArrayOf(android.R.attr.state_checked * (if (isChecked) 1 else -1))
            pandaIcon.setImageState(stateSet, true)

            Handler().postDelayed({

//                var intent = Intent(this,ChoiceActivity::class.java)
//                startActivity(intent)
                finish()
            },1000)


        }

        mainLayout!!.setOnClickListener {

            hideKeyBoard()


        }


        Handler().postDelayed({ isChecked = !isChecked
            val stateSet = intArrayOf(android.R.attr.state_checked * (if (isChecked) 1 else -1))
            pandaIcon.setImageState(stateSet, true)},2000)




        if (isFlow) {

            headerLabel!!.setText(String.format(headerArr.get(Random.nextInt(headerArr.size))
                ,bundle!!.getString("subject_name")
                ,bundle!!.getString("teacher_name")))

            subHeaderLabel!!.setText(subHeaderArr.get(Random.nextInt(subHeaderArr.size)))


        }



        dropDownArrow!!.scaleType = ImageView.ScaleType.MATRIX

        val px = dropDownArrow!!.drawable.intrinsicWidth/2f
        val py = dropDownArrow!!.drawable.intrinsicHeight/2f

        constraintSet!!.clone(mainLayout)


        //show/hide header
        dropDownArrow!!.setOnClickListener {

            if (headerLabel!!.visibility == View.VISIBLE
                && subHeaderLabel!!.visibility == View.VISIBLE) {

//                dropDownArrow!!.rotation = 180f

                matrix.postRotate(180f,px,py)
                dropDownArrow!!.imageMatrix = matrix



                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.TOP
                    ,R.id.app_name_header,ConstraintSet.BOTTOM)

                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.START
                    ,R.id.app_name_header,ConstraintSet.START)

                //!!.connect(R.id.dropdown_arrow,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,20)

                constraintSet!!.applyTo(mainLayout)



                headerLabel!!.visibility = View.GONE
                subHeaderLabel!!.visibility = View.GONE



            } else {

                matrix.postRotate(-180f,px,py)
                dropDownArrow!!.imageMatrix = matrix

                headerLabel!!.visibility = View.VISIBLE
                subHeaderLabel!!.visibility = View.VISIBLE

                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.TOP
                    ,R.id.sub_header_label,ConstraintSet.BOTTOM)

                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.START
                    ,R.id.header_label,ConstraintSet.START)

                //constraintSet!!.connect(R.id.dropdown_arrow,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,20)

                constraintSet!!.applyTo(mainLayout)

                if (onGoingIndex!=-1) {

                    chatRecyclerView.scrollToPosition(onGoingIndex)
                }

            }


        }



//        layoutBack!!.setOnTouchListener({_,event->
//
//            gestureDetector.onTouchEvent(event)
//            true
//        })
//
//        mainLayout!!.setOnTouchListener({_,event->
//
//            gestureDetector.onTouchEvent(event)
//            true
//        })


        chatEditText = findViewById(R.id.chat_edit_text)
        chatEditText.setMovementMethod(ScrollingMovementMethod.getInstance())

        //chatedittext button logics
        chatEditText.setOnDrawableClickListener(object : ChatEditTextView.OnDrawableClickListener {
            override fun onClick(position: ChatEditTextView.DrawablePosition) {

                when(position) {

                    ChatEditTextView.DrawablePosition.RIGHT->{

                        hideKeyBoard()

                            if (chatEditText.isSelected) {

                                //isStopped.set(false) //set isStopped to false(used to send speech for transcription.only happens when false)

                                //isStopped = false

                                if(!chatEditText.text.toString().isBlank()) {

                                    if (status.equals("up")) { //send req only if status is up

                                        isServiceAlert = false

                                        processData2(chatEditText.text.toString())

                                    } else {

                                        //status is down reset chateditText

                                        chatEditText.isSelected = false
                                        chatEditText.setDefaultState()

                                        Toast.makeText(this@ChatActivity,"Service not available",Toast.LENGTH_SHORT).show()
                                    }

                                } else {

                                    //for blank question

                                    Toast.makeText(this@ChatActivity
                                        ,"Enter valid question"
                                        ,Toast.LENGTH_SHORT).show()

                                    chatEditText.isSelected = false
                                    chatEditText.setDefaultState()

                                }

                            } else {

                               // isStopped.set(true)  //update isStopped when mic off so it
                                // doesn't send the recorded speech for transcription



                                TextToSpeechInstance.destroyInstance() //disable TTS

                                dataThread?.let {

//                                    dataThread!!.isStopped = true

                                    dataThread?.dataListener = null //set datalistener to null
                                    dataThread = null
                                    // to prevent UI update
                                }

                                //if Thinking anim is present reset it


                                if (dataSet.get(index).isAnim) {



                                    dataSet.get(index).isAnim = false

                                    dataSet.get(index).spannableStringBuilder.clear()
                                    dataSet.get(index).spannableStringBuilder.append("Stopped by user")
//                                    dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                                        ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

                                    chatRecyclerView.post {

                                        chatAdapter.notifyItemChanged(index)
                                    }
                                } else {

                                    // change the ongoing speech text to white

                                    if (onGoingIndex!=-1) {

                                        chatRecyclerView.post {

                                            dataSet.get(onGoingIndex).isProcess = false

//                                            dataSet.get(onGoingIndex).spannableStringBuilder
//                                                .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                                                    this@ChatActivity,R.color.white))
//                                                    ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
//                                                    ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                                            chatAdapter.notifyItemChanged(onGoingIndex)

                                            chatRecyclerView.scrollToPosition(onGoingIndex)
                                        }

                                    }

                                }

                            }



                    }

                    ChatEditTextView.DrawablePosition.LEFT->{

                       // if (checkAudioPermissionAndStart()) {

                            if (chatEditText.isSelected) {

                                //isStopped.set(false) //set isStopped to false(used to send speech for transcription
                                // only happens when false)

//                                isStopped = false

                                startSpeechRecognizer()

                                //startRecording() //start recorder

                                //WhisperInstance.startRecording()

//                                if (!mWhisper!!.isInProgress()) {
//                                    //HapticFeedback.vibrate(this)
//                                    startRecording()
//
//                                } else {
//
//                                }

//                                if (SpeechRecognizer.isRecognitionAvailable(this@ChatActivity)) {
//
//                                    checkAudioPermissionAndStart()
//
//
//                                } else {
//
//                                    chatEditText.isSelected = false
//
//                                    showAlertPromptForGoogleApp()
//
////                                if (!checkGoogleAppAvailability()) {
////
////                                    installGoogleApp()
////                                }
//                                }

                            } else {

//                                mRecorder!!.stop()

                                //SpeechRecognizerInstance.destroyInstance()

                                //isStopped.set(true) //update isStopped when mic off so it
                                // doesn't send the recorded speech for transcription

                                //stopRecording() //stop mic



                                speechHandler.removeCallbacks(speechRunnable!!) //clear all callbacks

                                nStrBuilder.clear()

                                SpeechRecognizerInstance.destroyInstance()

                                TextToSpeechInstance.destroyInstance() //destroy TTS

                                chatEditText.setText("")



//                                isStopped = true




                                dataThread?.let {

//                                    dataThread?.isStopped = true
                                    dataThread?.dataListener = null //set datalistener to null
                                    // to prevent UI update

                                    dataThread = null
                                }


                                //reset Thinking Anim

                                if (index!=-1) {

                                    if (dataSet.get(index).isAnim) {

                                        var errorStr = SpannableString("Stopped by user")
                                        errorStr.setSpan(ForegroundColorSpan(Color.RED),0,errorStr.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)


                                        dataSet.get(index).spannableStringBuilder.clear()
                                        dataSet.get(index).spannableStringBuilder.append(errorStr)
                                        dataSet.get(index).isError = true

                                        dataSet.get(index).isAnim = false

                                        chatAdapter!!.notifyItemChanged(index)

                                    } else {

                                        // change the ongoing speech text to white

                                        if (onGoingIndex != -1) {

                                            chatRecyclerView.post {

                                                dataSet.get(onGoingIndex).isProcess = false

                                                chatAdapter.notifyItemChanged(onGoingIndex)

                                                chatRecyclerView.scrollToPosition(onGoingIndex)
                                            }


                                        }
                                    }


                                }


                            }


                        //}



                    }

                    ChatEditTextView.DrawablePosition.OTHER->{

                        //show editText and requestFocus

                        chatEditText.requestFocus()

                        chatEditText.setFocusable(true)
                        chatEditText.isFocusableInTouchMode = true

                        showKeyboardFocus(true)
                    }
                }

            }


        })


        chatRecyclerView = findViewById(R.id.chat_recycler)
        chatRecyclerView.layoutManager = LinearLayoutManagerWrapper(this@ChatActivity)

        chatAdapter = ChatAdapter()
        chatAdapter.setHasStableIds(true)
        chatRecyclerView.adapter = chatAdapter

    }

    var customDialog:Dialog?=null


    private fun stopRecording() {
        //checkPermissions()
        //mRecorder!!.stop()

        //WhisperInstance.stopRecording()
    }

    private suspend fun initiateCountDown() {

        Log.d(ChatActivity::class.java.name, "launched")

        runOnUiThread {

            countDownLayout?.visibility = View.VISIBLE
            countDownProgress?.max = 60


        }

        for (i in 0..60) {

            runOnUiThread {

                countDownProgress?.progress = 60-i
                progressText?.text = countDownProgress?.progress.toString()

                if (countDownProgress?.progress == 0) {

                    val customAlertDialogBuilder:CustomAlertDialogBuilder = CustomAlertDialogBuilder(object : CustomAlertDialogBuilder.DialogButtonClickListener{
                        override fun onClickPositiveButton(dialog: DialogInterface) {

                            dialog.dismiss()
                            initiateFeedBackDialog()
                        }


                    })
                    customAlertDialogBuilder.buildAlertDialog(this@ChatActivity
                        ,"Hello!"
                        ,"Class has ended"
                        , R.drawable.panda).show()


                }

            }

            delay(1000)




        }


    }

    private fun initiateFeedBackDialog() {

        var widthPixels = (resources.displayMetrics.widthPixels*0.70f).toInt()
        var heightPixels = (resources.displayMetrics.heightPixels*0.80f).toInt()

        var dialogBuilder:CustomDialogBox = CustomDialogBox().buildDialog(this,R.layout.dialog_your_feedback)
            .setSize(widthPixels,heightPixels)

        var dialog = dialogBuilder.createDialog()
        dialog.setCancelable(false)
        dialog.window?.apply {

            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            enterFullScreenMode(this)

        }


        var linearLayoutStars:LinearLayout = dialog.findViewById(R.id.linear_layout_stars)
        var reviewEdtText:EditText = dialog.findViewById(R.id.review_edit_text)
        reviewEdtText.movementMethod = ScrollingMovementMethod.getInstance()

        var submitBtn:Button = dialog.findViewById(R.id.review_submit_button)

        submitBtn.setOnClickListener {

            sendFeedBack(reviewEdtText)

            //post To server
        }



        for (i in 0 until  linearLayoutStars.childCount) {

            if (i == 0) {


                //set One

                (linearLayoutStars.getChildAt(i) as ImageView).setOnClickListener {

                    starsCount = i+1

                    deselectAllSelectedStars(linearLayoutStars)

                    (linearLayoutStars.getChildAt(i) as ImageView).isSelected = true
                }




            } else {


                (linearLayoutStars.getChildAt(i) as ImageView).setOnClickListener {

                    starsCount = i+1

                    deselectAllSelectedStars(linearLayoutStars)

                    for (j in 0 ..i) {

                        //setSelectedAll
                        linearLayoutStars.getChildAt(j).isSelected = true

                    }


                }

            }

        }

        dialog.show()

    }

    private fun sendFeedBack(edtText: EditText) {

        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: okhttp3.Response?) {

                val intent = Intent(this@ChatActivity,SpikeActivity::class.java)
                startActivity(intent)

                finish()

            }

            override fun onError(code: Int, response: okhttp3.Response?) {

            }

            override fun onException(error: String?) {

            }

        })

        val params:MutableMap<String,String> = mutableMapOf()
        params.put("assignment",bundle!!.getString("assignmentId")!!)
        params.put("teacher",bundle!!.getString("teacherId")!!)
        params.put("rate",starsCount.toString())

        if (edtText.text.toString().isNotBlank()) {

            params.put("message",edtText.text.toString())

        }




        OkHttpClientInstance.post(OkHttpClientInstance.BASEURL+OkHttpClientInstance.feedBackUrl
            ,params)
    }

    private fun deselectAllSelectedStars(linearLayout: LinearLayout) {

        for (j in 0 until linearLayout.childCount) {

            //setSelectedAll
            linearLayout.getChildAt(j).isSelected = false

        }




    }



    private fun showKeyboardFocus(visible:Boolean) {

        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (visible) {

            imm.showSoftInput(chatEditText,InputMethodManager.SHOW_IMPLICIT)
            //imm = null

        }


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



        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val tv = holder.textView
            val item = dataSet[position]

            val tag = "md-$position"
            tv.tag = tag

            // Reset view state (VERY important for RecyclerView)
            tv.text = null
            tv.setTextColor(ContextCompat.getColor(this@ChatActivity, R.color.white))
            tv.movementMethod = null
            tv.visibility = View.VISIBLE

            // ---------------- LOGO ----------------
            if (item.isQuestion) {
                holder.logo.visibility = View.VISIBLE
                holder.logo.setImageResource(R.drawable.panda)
            } else if (item.isImage) {
                holder.logo.visibility = View.VISIBLE
                holder.logo.setImageResource(R.drawable.panda_1)
            } else {
                holder.logo.visibility = View.GONE
            }

            if (item.isError) {

                tv.setText(item.spannableStringBuilder,TextView.BufferType.SPANNABLE)

            }

            // ---------------- SIMPLE TEXT CASES ----------------
            if (item.isQuestion) {
                tv.text = item.spannableStringBuilder
                return
            }

            if (item.isPage) {
                tv.setText(item.spannableStringBuilder,TextView.BufferType.SPANNABLE)
                tv.setTextColor(Color.BLUE)
                tv.movementMethod = LinkMovementMethod.getInstance()

                // ---------------- CLICK ----------------
//                tv.setOnClickListener {
//                    if (tv.text?.trim()?.startsWith("Download Pdf") == true) {
//
//                        val file = Utils.generatePDF3(
//                            this@ChatActivity,
//                            pdfMap[item.quesNo].toString()
//                        )
//
//                        val intent = Intent(this@ChatActivity, PdfActivity::class.java)
//                        intent.putExtra("pdfFile", file)
//                        startActivity(intent)
//                    }
//                }

                return
            }

//            if (item.isProcess) {
//                tv.text = item.spannableStringBuilder
//                return
//            }

            // ---------------- MARKDOWN CASE ----------------
            val renderedMarkdown = item.renderedMarkDown

            if (renderedMarkdown != null) {

                // IMPORTANT: only Markwon writes to TextView
                tv.post {
                    if (tv.tag != tag) return@post
                    markWon!!.setParsedMarkdown(tv, renderedMarkdown)

                    if (item.isProcess) {
                        tv.setTextColor(
                            ContextCompat.getColor(
                                this@ChatActivity,
                                R.color.yellow_panda_logo_color
                            )
                        )
                    }
                }

            } else {
                // Fallback: plain text only
                tv.text = item.spannableStringBuilder
            }

//            if (item.isProcess) {
//
//                tv.setTextColor(resources.getColor(R.color.yellow_panda_logo_color))
//            }



            // ---------------- ANIMATION ----------------
            if (item.isAnim && position == index) {
                initAnimator(tv, true)
            } else {
                initAnimator(tv, false)
                tv.alpha = 1f
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




   private var statusIntent:Intent?=null
    override fun onResume() {
        super.onResume()
        //SpeechRecognizerInstance.destroyInstance()

        status = ""

        statusIntent = Intent(this, StatusService::class.java)
        startService(statusIntent)

        //bind status service



        //var statusIntent = Intent(this,StatusService::class.java)
        bindService(statusIntent!!,statusServiceConnection!!,Context.BIND_AUTO_CREATE)
        //isBound = true


    }

    override fun onStop() {
        super.onStop()

        //prevent UI update on activity stop

        dataThread?.let {

//            dataThread!!.isStopped = true
            dataThread?.dataListener = null
            dataThread = null

        }

        SpeechRecognizerInstance.destroyInstance()
        TextToSpeechInstance.destroyInstance()
        //WhisperInstance.setListener(null)
        stopRecording() //if recording is ongoing stop it.

        nStrBuilder.clear()
        chatEditText.setDefaultState()

        //unbind statusService

        statusBinder?.setStatusCallback(null)
        unbindService(statusServiceConnection!!)


        stopService(statusIntent)

        //isBound = false

    }

    override fun onPause() {
        super.onPause()
        //SpeechRecognizerInstance.destroyInstance()
    }

    var nStrBuilder:StringBuilder = StringBuilder()





    private fun startSpeechRecognizer() {



        chatEditText.setText("")

            SpeechRecognizerInstance.getInstance(this,object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {


                }

                override fun onBeginningOfSpeech() {

                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onEndOfSpeech() {


                    Log.d(TAG, "onEndOfSpeech: ")

                }

                override fun onError(error: Int) {

                    SpeechRecognizerInstance.destroyInstance()
                    chatEditText.setDefaultState()

//                    if (error == 9) {
//
//                        chatEditText.isSelected = false
//
//                        showAlertPrompt("Alert"
//                            ,"Go to settings to enable Google microphone permission"
//                            ,R.drawable.ic_android_black_24dp)
//
//                    }

                    Log.d(SelectClassActivity::class.java.name, "onError: ${error}")



                }

                override fun onResults(results: Bundle?) {

//                    isSpeechStarted = false
//
//                    var runnable:java.lang.Runnable = java.lang.Runnable{
//
//                        var i = 0
//
//                        while (i<5) {
//
//                            if (isSpeechStarted) {
//
//
//                                handler!!.removeCallbacks(r!!)
//
//                            }
//
//                            i++
//                        }
//
//                        var speechResultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                        Log.d(SelectClassActivity::class.java.name, "Results: ${speechResultList?.get(0)}")
//
//                        speechResultList?.let {
//
//                            runOnUiThread {
//
//                                chatEditText.setText(speechResultList.get(0))
//
//                                processData2(speechResultList.get(0))
//
//                            }
//
//                        }
//
//                    }
//
//                    handler!!.post(runnable)

                }

//                override fun onPartialResults(partialResults: Bundle?) {
//
//                    var speechResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//
//
//                    for (s in speechResultList!!) {
//
//                        Log.d(TAG, "onResults: " + s)
//                        speechHandler.removeCallbacks(speechRunnable!!)
//                        chatEditText!!.append(s)
//
//                    }
//
//                    if (!chatEditText.text!!.isEmpty()) {
//
//                        speechHandler.postDelayed(speechRunnable!!,5000)
//
//                    } else {
//
//                        Handler().postDelayed({
//                            isAnim = false
//                            dataThread?.isStopped = true
//                            //chatEditText.isSelected = false
//                            //chatEditText.setDefaultState()
//                                              SpeechRecognizerInstance.destroyInstance()},3000)
//                    }
//
////                if (speechResultList.isEmpty()) {
//
//
//
//                }

                override fun onPartialResults(partialResults: Bundle?) {

                    speechHandler.removeCallbacks(speechRunnable!!)

                    partialResults?.let {

                        val l = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val s:String? = l?.firstOrNull()?.trim()

                        nStrBuilder.clear()
                        nStrBuilder.append(s)

                        chatEditText.setText(nStrBuilder.toString())

                        Log.d(SampleActivity::class.java.name, "onPartialResults: "+l?.firstOrNull())

                    }

                    speechHandler.postDelayed(speechRunnable!!, 3000)

//                    val confidenceScores = partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
//                    val speechResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                    val s = speechResultList?.firstOrNull()?.trim()
//
//                    Log.d(TAG, "Data: "+s)
//
////// --------------------------
////// 1. Early returns FIRST
////// --------------------------
////                    if (confidenceScores != null && confidenceScores.isNotEmpty() && confidenceScores[0] < 0.5f) {
////                        return
////                    }
//
//                    if (s.isNullOrEmpty()) {
//                        return
//                    }
////
////// --------------------------
////// 2. Cancel previous runnable
////// --------------------------
//                    speechHandler.removeCallbacks(speechRunnable!!)
////
////// --------------------------
////// 3. Safe string builder logic
////// --------------------------
//                    if (nStrBuilder.isEmpty()) {
//
//                        nStrBuilder.append(s)
//                        chatEditText?.post { chatEditText?.append(s) }
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
//                            chatEditText?.post { chatEditText?.append(newPart) }
//
//                        } else if (!s.trim().equals(nStrBuilder.toString().trim(), ignoreCase = true)) {
//
//                            val isValidNewStart = s.trim().split(" ").size==1
//
//                            if (isValidNewStart) {
//                                nStrBuilder.clear()
//                                nStrBuilder.append(s)
//                                chatEditText?.post { chatEditText?.append(s) }
//                            }
//                        }
//                    }
////
////// --------------------------
////// 4. GUARANTEED repost
////// --------------------------
//                    speechHandler.postDelayed(speechRunnable!!, 3000)


                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }


            },20000)

        SpeechRecognizerInstance.startSpeech()

    }

    private var dataThread:KtorDataReceiver?=null

    //send request

    private fun processData2(data:String) {


        dataSet.add(DataSet(System.currentTimeMillis(), MySpannableStringBuilder(data),false,null,true,true))
        chatAdapter.notifyItemInserted(dataSet.size - 1)

        chatEditText.setText("")

        dataSet.add(DataSet(System.currentTimeMillis(),MySpannableStringBuilder("Thinking"),false,ansDataList,false,true,isAnim = true))



        index = dataSet.size - 1

        chatAdapter.notifyItemInserted(index)

        chatRecyclerView.scrollToPosition(index)

        dataThread = KtorDataReceiver(System.currentTimeMillis().toString(),data,classId,subject!!,this,RetrofitInstanceBuilder.TEACHING_ASSISTANT)
        dataThread!!.run()

    }

    //initialize Thinking animation

    private fun initAnimator(textView: TextView,isStart:Boolean) {

        if (isStart) {

            textAnimator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f).apply {

                duration = 700
                repeatCount = ObjectAnimator.INFINITE

                addUpdateListener {

                    textView.alpha = it.animatedValue as Float
                }


            }

            textAnimator?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {

                    textView.alpha = 1f
                    textAnimator = null

                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }


            })

            textAnimator?.start()

        } else {

            textAnimator?.cancel()

        }

    }

//    private fun checkAudioPermissionAndStart() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
//
//                100
//            )
//        } else {
//            startSpeechRecognizer()
//        }
//    }

    var toDownload:ArrayList<String> = arrayListOf<String>()


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (SpeechRecognizer.isRecognitionAvailable(this)) {

                    startSpeechRecognizer()

                } else {

                    if (!checkGoogleAppAvailability()) {

                        installGoogleApp()
                    }


                }

            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required for speech recognition",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkGoogleAppAvailability():Boolean {

       return try {

            this.packageManager.getPackageInfo("com.google.android.googlequicksearchbox",0)
            true

        } catch (ex:PackageManager.NameNotFoundException) {

            false
        }
    }

    private fun installGoogleApp() {

        var intent:Intent = Intent(Intent.ACTION_VIEW).apply {

            data = Uri.parse("market://details?id=com.google.android.googlequicksearchbox")
            setPackage("com.android.vending")
        }

        try {

            startActivity(intent)

        } catch (ex:ActivityNotFoundException) {

            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox")
            }
            startActivity(webIntent)
        }
    }

    //hide keyboard

    private fun hideKeyBoard() {

        val imm:InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(mainLayout?.windowToken,0)

    }

    override fun onDestroy() {
        super.onDestroy()

        isSpeech = false
        TextToSpeechInstance.destroyInstance()
        //SpeechRecognizerInstance.destroyInstance()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    //highlight page logic

    fun highlightPageNumber(text: String): SpannableString {
        val regex = Regex("\\[(\\d+(,\\s*\\d+)*)]")   // detect [25] at end of line
        val spannable = SpannableString(text)
        var startIndex = 0

        val match = regex.find(text)

        if (match!=null) {

            val pagesStr = match.groupValues.get(1)
            val pages = pagesStr.split(Regex(", "))

            var pagesSpanBuilder = MySpannableStringBuilder("Download pdf ")

            for (p in pages.indices) {

                val page = pages[p];


                pagesSpanBuilder.append(page)
                if (p<pages.size-1) {

                    pagesSpanBuilder.append(",")

                }

                startIndex = pagesSpanBuilder.indexOf(page,startIndex)

                pagesSpanBuilder.setSpan(object : ClickableSpan(){
                    override fun onClick(widget: View) {

                        val subjectName = subject!!.trim().split(" ")
                        if (subjectName.size>1) {

                            subject = "${subjectName[0]}_${subjectName[1]}"
                        }

                        val urlStr = String.format(OkHttpClientInstance.pfdUrl,classId,subject!!.lowercase(),subject!!.lowercase())

                        Log.d(TAG, "ClassIdPdf: "+urlStr)

                        var customDialog = CustomDialogBox()
                            .buildDialog(this@ChatActivity,R.layout.layout_download_pdf)
                            .setSize((Utils.getScreenWidth(this@ChatActivity)*0.60).toInt(),(Utils.getScreenHeight(this@ChatActivity)*0.40).toInt())
                            .setCancelable(false)
                            .createDialog()

                        customDialog.show()

                        customDialog.findViewById<TextView>(R.id.progress_text).apply {

                            setText("Opening PDF")
                            setTextColor(ContextCompat.getColor(this@ChatActivity,R.color.black))
                        }

                        CoroutineScope(Dispatchers.IO).launch {

                            var input: InputStream? = null
                            var output: FileOutputStream? = null
                            var connection: HttpURLConnection? = null

                            try {

                                val url = URL(urlStr)
                                connection = url.openConnection() as HttpURLConnection
                                connection.connect()

                                // total size
                                val totalSize = connection.contentLength

                                input = connection.inputStream
                                val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "sample.pdf")
                                output = FileOutputStream(file)

                                val buffer = ByteArray(1024 * 1024)   // 8KB buffer (standard)
                                var downloaded = 0
                                var read: Int

                                // Cache views once
                                val progressBar = customDialog.findViewById<ProgressBar>(R.id.progressRing)
                                val progressText = customDialog.findViewById<TextView>(R.id.progress_msg)

                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                    downloaded += read

                                    if (totalSize > 0) {
                                        val percent = (downloaded * 100f / totalSize)
                                        withContext(Dispatchers.Main) {
                                            progressBar.progress = percent.toInt()
                                            progressText.text = percent.toInt().toString()
                                        }
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@ChatActivity, "Pdf downloaded", Toast.LENGTH_SHORT).show()
                                    customDialog.dismiss()
                                    openPdfAtPage(pages[p].toInt(), file.absolutePath)
                                }

                            } catch (ex: Exception) {

                                ex.printStackTrace()

                                withContext(Dispatchers.Main) {
                                    customDialog.dismiss()
                                    Toast.makeText(this@ChatActivity, "Error: ${ex.message}", Toast.LENGTH_SHORT).show()
                                }

                            } finally {
                                input?.close()
                                output?.close()
                                connection?.disconnect()
                            }
                        }


                        //Toast.makeText(this@ChatActivity,"Clicked",Toast.LENGTH_SHORT).show()
                    }


                },startIndex,pagesSpanBuilder.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } //clickable span for all the pages.

            dataSet.add(
                DataSet(
                    System.currentTimeMillis(),
                    pagesSpanBuilder,
                    false,
                    null,
                    false,
                    false,
                    true
                )
            )

            chatAdapter.notifyItemInserted(dataSet.size-1)



        }
        return spannable
    }

    //open pdf when page no clicked
    fun openPdfAtPage(page:Int,pdfFile: String) {

        val intent:Intent = Intent(this,PdfActivity::class.java).apply {

            putExtra("page",page)
            putExtra("pdfFile",pdfFile)
        }

        startActivity(intent)




    }

    val respStrBuilder:StringBuilder = StringBuilder()


    //dataListener functions(onDataReceived(success),onError(exception))
    override suspend fun onDataReceived(line: String?,isEnded:Boolean) {

        runOnUiThread {

            Log.d("Data", "Data: "+line)

            line?.let {



                if (dataSet.get(index).isAnim) {

                    dataSet.get(index).isAnim = false

                    dataSet.get(index).spannableStringBuilder.clear()
                    dataSet.get(index).isAnim = false

                    chatAdapter!!.notifyItemChanged(index)

                }



                if (!isEnded) {

                    respStrBuilder.append(line)
                    var s = respStrBuilder.toString()



                        //highlight page nos

                        highlightPageNumber(s)

                    s = Utils.normalizeLatexDelimiters(s)

                    val regex = Regex("\\[(\\d+(,\\s*\\d+)*)]")

                    s = s.replace(regex,"")

                    val node = markWon!!.parse(s)

                    dataSet.get(index).renderedMarkDown = markWon!!.render(node)

                    chatRecyclerView.post {

                        chatAdapter.notifyItemChanged(index)

                    }



                } else {

                    //reset chatedittext

                    if (isSpeech) {

                        lineCount++

                        //speech regex(replaces math latex,<page> tag etc for speech)
                        //val regex = Regex("""(^\s*\|[-\s|]+\|\s*$)|([*#]+\s*)|(<page>\[[0-9]+\]</page>)|([-]+)|(\[.*?])|(\^)|(\bsqrt\b)|([=+\-*/<>\]\[])|([∑∫π√])|\\frac|`+""", RegexOption.MULTILINE)

                        //replace the speech regex with "" before sending for speech
                        TextToSpeechInstance.speak(
                            Utils.markdownToTts(respStrBuilder.toString()), dataSet.size - 1, 0
                        )


                    } else if (!isSpeech) {

                        //scroll as text increases

                        chatEditText.isSelected = false
                        chatEditText.setDefaultState()
                        chatRecyclerView.scrollToPosition(dataSet.size-1)

                    }




                }


            }


        }

    }

    override suspend fun onError(ex: Exception) {

        //reset is Anim in case of error

        runOnUiThread {


            if (dataSet.get(index).isAnim) {

                dataSet.get(index).spannableStringBuilder.clear()
                dataSet.get(index).spannableStringBuilder.append("Error : ${ex.message}")
                dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
                ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

                dataSet.get(index).isError = true
                dataSet.get(index).isAnim = false

                chatAdapter.notifyItemChanged(index)
                chatEditText.isSelected = false
                chatEditText.setDefaultState()



            } else {

                val errorStr = "Error : ${ex.message}"

                dataSet.add(DataSet(System.currentTimeMillis(), MySpannableStringBuilder("Connect Error : ${ex.message}").apply {

                    setSpan(ForegroundColorSpan(Color.RED),0,errorStr.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                },false,null,true,true,false, isError = true))

                chatAdapter.notifyItemInserted(dataSet.size-1)

                //reset chatedittext

                //Toast.makeText(this@ChatActivity,"Connect Error : ${ex.message}",Toast.LENGTH_SHORT).show()

                chatEditText.isSelected = false
                chatEditText.setDefaultState()

            }


        }


//


    }

    override suspend fun onRestart(msg: String) {

        runOnUiThread {


            if (dataSet.get(index).isAnim) {

                dataSet.get(index).spannableStringBuilder.clear()
                dataSet.get(index).spannableStringBuilder.append(msg)
                dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
                    ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

                dataSet.get(index).isError = true
                dataSet.get(index).isAnim = false

                chatAdapter.notifyItemChanged(index)
                chatEditText.isSelected = false
                chatEditText.setDefaultState()



            } else {

                val errorStr = msg

                dataSet.add(DataSet(System.currentTimeMillis(), MySpannableStringBuilder(errorStr).apply {

                    setSpan(ForegroundColorSpan(Color.RED),0,errorStr.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                },false,null,true,true,false, isError = true))

                chatAdapter.notifyItemInserted(dataSet.size-1)

                //reset chatedittext

                //Toast.makeText(this@ChatActivity,"Connect Error : ${ex.message}",Toast.LENGTH_SHORT).show()

                chatEditText.isSelected = false
                chatEditText.setDefaultState()

            }


        }


    }


    var statusBinder:StatusService.StatusBinder?=null
    var status:String?=null

    var isServiceAlert = false

    private inner class StatusServiceConnection:ServiceConnection{

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            isBound = true

            statusBinder = service as StatusService.StatusBinder
            statusBinder!!.setStatusCallback {

                runOnUiThread {

                    if(it.equals("down")) {

                        status = it

                        dataThread?.dataListener = null //set dataListener to
                        // null so no updates to UI

                        dataThread = null

                        //if Thinking anim running stop and reset the flags

                        runOnUiThread {

                            if (dataSet.get(index).isAnim) {

                                dataSet.get(index).isAnim = false
                                dataSet.get(index).spannableStringBuilder.clear()
//                            dataSet.removeAt(dataSet.size-1)
                                chatAdapter.notifyItemChanged(index)

                            }

                            chatEditText.isSelected = false
                            chatEditText.setDefaultState()


                        }

                        Log.d(TAG, "Status is down")

                        if (!isServiceAlert) {

                            isServiceAlert = true
                            showServiceAlert() //show service alert

                        }


                    } else {

                        status = it

//
                    }


                }

                //if status down




            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {

            isBound = false

        }


    }

    private fun showServiceAlert() {

        runOnUiThread{

            var aBuilder = AlertDialog.Builder(this@ChatActivity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Alert")
                .setMessage("Service unavailable. Please try again")
                .setPositiveButton("Ok",object:DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        dialog!!.dismiss()
                    }

                })

            var alertDialog = aBuilder.create()
            alertDialog.show()


        }

    }

    //resultreceiver for transcription



    var modelDialog:Dialog?=null
    var progressBar:ProgressBar?=null
    var progressTxt:TextView?=null
    var progressMsg:TextView?=null
    var titleText:TextView?=null

    override fun onDownloadStarted() {

        modelDialog = CustomDialogBox()
            .buildDialog(this@ChatActivity,R.layout.layout_download_model)
            .setSize((Utils.getScreenWidth(this@ChatActivity)*0.60).toInt(),(Utils.getScreenHeight(this@ChatActivity)*0.40).toInt())
            .setCancelable(false)
            .createDialog()

        modelDialog!!.show()

        var okBtn = modelDialog!!.findViewById<Button>(R.id.btnOk)
        okBtn.visibility = View.INVISIBLE


        progressBar = modelDialog!!.findViewById<ProgressBar>(R.id.progressRing)
        progressMsg = modelDialog!!.findViewById<TextView>(R.id.progress_msg)
        progressTxt = modelDialog!!.findViewById(R.id.progress_text)

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

            Handler().postDelayed({modelDialog?.dismiss()},1000)


        }

    }

    override fun onProgress(progress: Float) {

        runOnUiThread {

            progressBar!!.progress = progress.toInt()
            progressMsg!!.text = progress.toInt().toString()

        }

    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }






}