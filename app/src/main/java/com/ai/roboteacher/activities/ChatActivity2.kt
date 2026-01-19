//package com.ai.roboteacher.activities
//
////import org.vosk.Model
////import org.vosk.Recognizer
////import org.vosk.android.SpeechService
////import org.vosk.android.StorageService
//
//import CustomAlertDialogBuilder
//import CustomDialogBox
//import android.Manifest
//import android.animation.Animator
//import android.animation.ObjectAnimator
//import android.app.AlertDialog
//import android.app.Dialog
//import android.content.ActivityNotFoundException
//import android.content.Context
//import android.content.DialogInterface
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.content.res.Configuration
//import android.graphics.Color
//import android.graphics.Matrix
//import android.graphics.Rect
//import android.graphics.drawable.ColorDrawable
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.HandlerThread
//import android.provider.Settings
//import android.speech.RecognitionListener
//import android.speech.SpeechRecognizer
//import android.speech.tts.UtteranceProgressListener
//import android.text.Spannable
//import android.text.SpannableString
//import android.text.method.ScrollingMovementMethod
//import android.text.style.ForegroundColorSpan
//import android.util.Log
//import android.view.GestureDetector
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.Window
//import android.view.WindowInsetsController
//import android.view.WindowManager
//import android.view.inputmethod.InputMethodManager
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.ProgressBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.constraintlayout.widget.ConstraintSet
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.ai.roboteacher.ChatEditTextView
//import com.ai.roboteacher.DataSet
//import com.ai.roboteacher.Direction
//import com.ai.roboteacher.MainActivity
//import com.ai.roboteacher.MySpannableStringBuilder
//import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
//import com.ai.roboteacher.R
//import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
//import com.ai.roboteacher.SpeechRecognizerInstance
//import com.ai.roboteacher.SwipeGestureListener
//import com.ai.roboteacher.TextToSpeechInstance
//import com.ai.roboteacher.Utils
//import com.ai.roboteacher.WhisperInstance
//import io.ktor.client.HttpClient
//import io.ktor.client.engine.cio.CIO
//import io.ktor.client.engine.cio.endpoint
//import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
//import io.ktor.client.request.preparePost
//import io.ktor.client.request.setBody
//import io.ktor.client.statement.bodyAsChannel
//import io.ktor.http.ContentType.Application.Json
//import io.ktor.http.contentType
//import io.ktor.http.isSuccess
//import io.ktor.serialization.kotlinx.json.json
//import io.ktor.utils.io.ByteReadChannel
//import io.noties.markwon.Markwon
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.flow.flowOn
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//import java.net.ConnectException
//import java.nio.charset.StandardCharsets
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import java.util.TimeZone
//import java.util.regex.Pattern
//import kotlin.random.Random
//
//class ChatActivity2:AppCompatActivity(),WhisperInstance.DataListener{
//
//    private val TAG = ChatActivity2::class.java.name
//
//    private var isChecked = false
//
//    var starsCount:Int = 0
//
//    var ansDataList:ArrayList<MySpannableStringBuilder?>? = ArrayList()
//
//    var onGoingIndex = -1
//
//    val MARKDOWN_CODE = "```"
//
//    val markdownStringBuilder:java.lang.StringBuilder = StringBuilder()
//
//    //var model:Model?=null
//
//    var headerArr:Array<String> = Array(5,{
//
//        ""
//    })
//
//    var subHeaderArr:Array<String> = Array(5,{
//
//        ""
//    })
//
//    var id = 0
//
//
//    var isClientClosed:Boolean = false
//
//    lateinit var chatEditText: ChatEditTextView
//
//
//    var dataSet: ArrayList<DataSet> = ArrayList<DataSet>()
//
//    var index: Int = -1
//    private lateinit var chatRecyclerView: RecyclerView
//    private lateinit var chatAdapter: ChatAdapter
//    private var textAnimator: ObjectAnimator? = null
//
//    private var isAnim: Boolean = false
//    private var isSpeech: Boolean = true
//    private var isProgress: Boolean = false
//    private var dropDownArrow:ImageView?=null
//    private var stopIcon:ImageView?=null
//
//
//    val baseMargin = 5
//    private var httpClient: HttpClient?=null
//    var isSpeechStarted = false
//    var handler:Handler?=null
//    var handlerThread:HandlerThread?=null
//    var r:java.lang.Runnable?=null
//    var markWon:Markwon?=null
//    private var isStopped = false
//    private var layoutBack:LinearLayout?=null
//    private var mainLayout:ConstraintLayout?=null
//
//    private var isScrolling = false
//    private var headerLabel:TextView?=null
//    private var subHeaderLabel:TextView?=null
//    private var matrix:Matrix = Matrix()
//    private var constraintSet:ConstraintSet?= ConstraintSet()
//    private var nameHeader:TextView?=null
//    private var startTime:String?=null
//    private var endTime:String?=null
//    private var isFlow:Boolean = false
//    private var isCountDownStarted:Boolean = false
//    private var countDownProgress:ProgressBar?=null
//    private var countDownLayout:View?=null
//    private var progressText:TextView?=null
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        enterFullScreenMode(window)
//        setContentView(R.layout.activity_chat)
//
//        headerArr[0] = "Welcome to your %s class with %s."
//        headerArr[1] = "You're all set for %s Literature with %s."
//        headerArr[2] = "Your %s session with %s is ready."
//        headerArr[3] = "Time to start %s with %s."
//        headerArr[4] = "Get ready to explore %s today with %s."
//
//        subHeaderArr[0] = "Let's begin your learning journey."
//        subHeaderArr[1] = "Let’s dive in!"
//        subHeaderArr[2] = " Let’s make it count!"
//        subHeaderArr[3] = "You’ve got this!"
//
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, 0, systemBars.right, 0)
//
//            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
//
//                val window = Rect()
//                v.getWindowVisibleDisplayFrame(window)
//
//                var editTextRect = Rect()
//                chatEditText.getGlobalVisibleRect(editTextRect)
//
//                val diff = editTextRect.bottom - window.bottom
//
//                if (diff > 0) {
//                    v.scrollBy(0, diff.plus(baseMargin*resources.displayMetrics.density.toInt()))
//                }
//            } else {
//
//                v.scrollTo(0,0)
//            }
//            insets
//        }
//
//        buildCustomListeningDialog()
//
//        countDownLayout = findViewById(R.id.count_down_layout)
//        countDownProgress = countDownLayout!!.findViewById(R.id.progressRing)
//        progressText = countDownLayout!!.findViewById(R.id.progress_text)
//
//        Log.d(TAG, "onCreate: " + intent.getStringExtra("assignmentId"))
//        Log.d(TAG, "onCreate: " + intent.getStringExtra("teacherId"))
//
//        intent?.let {
//
//            isFlow = intent.getBooleanExtra("isFlow",false)
//
//            if (isFlow) {
//
//                val customAlertDialogBuilder:CustomAlertDialogBuilder = CustomAlertDialogBuilder(object:CustomAlertDialogBuilder.DialogButtonClickListener{
//                    override fun onClickPositiveButton(dialog:DialogInterface) {
//
//                        dialog.dismiss()
//
//
//                    }
//
//
//                })
//                customAlertDialogBuilder.buildAlertDialog(this@ChatActivity2
//                    ,"Hello!"
//                    ,"Class has been started"
//                    , R.drawable.panda).show()
//
//                lifecycleScope.launch {
//
//                    while (true) {
//
//                        var sdf:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault())
//                        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
//                        var date:Date? = sdf.parse("${intent.getStringExtra("date")}T${intent.getStringExtra("end_time")}")
//                        val tmpStmp:Long? = date?.time
//
//                        Log.d(ChatActivity::class.java.name, "FutureTimeStamp ${System.currentTimeMillis()-tmpStmp!!}")
//
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
//
////                        if (System.currentTimeMillis()>=tmpStmp!!) {
////
////                            //showEndDialog
////
////                            val customAlertDialogBuilder:CustomAlertDialogBuilder = CustomAlertDialogBuilder()
////                            customAlertDialogBuilder.buildAlertDialog(this@ChatActivity
////                                ,"Hello!"
////                                ,"Class has ended"
////                                , R.drawable.panda).show()
////
////                            break
////
////                        } else if (tmpStmp-System.currentTimeMillis()<=60000) {
////
////                            if (!isCountDownStarted) {
////
////                                isCountDownStarted = true
////
////                                //launch Countdown
////
////                                lifecycleScope.launch {
////
////                                    initiateCountDown()
////
////                                }
////                            }
////
////                        }
//
//                        delay(5000)
//
//
//                    }
//
//                }
//            }
//        }
//
//
//
//        markWon = Markwon.create(this)
//
//        handlerThread = HandlerThread("Speech Thread")
//        handlerThread!!.start()
//        handler = Handler(handlerThread!!.looper)
//
//
//        TextToSpeechInstance.getInstance(applicationContext)
//        TextToSpeechInstance.setProgressListener(object :
//            UtteranceProgressListener() {
//            override fun onStart(utteranceId: String?) {
//
//
//
//
//                val indexes = utteranceId!!.split(":")
//                onGoingIndex = indexes[0].toInt()
//
//                chatEditText.isSelected = true
//
//                runOnUiThread {
//
//                    stopIcon!!.isSelected = true
//
//                    chatRecyclerView.post {
//
//                        dataSet.get(indexes[0].toInt()).spannableStringBuilder
//                            .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                                this@ChatActivity2,R.color.yellow_panda_logo_color))
//                                ,0,dataSet.get(indexes[0].toInt()).spannableStringBuilder.length
//                                ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                        chatAdapter.notifyItemChanged(indexes[0].toInt())
//
//                        chatRecyclerView.scrollToPosition(indexes[0].toInt())
//                    }
//
//
//
//                    Log.d(ChatActivity::class.java.name, "onStart: " + indexes[0])
//
////                                                chatRecyclerView.post {
////
////                                                    val lArr = intArrayOf(0,0)
////
////
////
////                                                    (chatRecyclerView.layoutManager!!.getChildAt(index))
////                                                        ?.findViewById<RecyclerView>(R.id.recycler_answers)
////                                                        ?.getLocationInWindow(lArr)
////
////                                                    Log.d(ChatActivity::class.java.name, "PosY: " + lArr[1])
////
////                                                    if (indexes[0].toInt() == 0) {
////
////                                                        chatRecyclerView.layoutManager?.getChildAt(index)
////                                                            ?.findViewById<RecyclerView>(R.id.recycler_answers)
////                                                            ?.scrollBy(0,360)
////                                                    }
////
//////                                                        val recyclerLocation = IntArray(2)
//////                                                        chatRecyclerView.getLocationInWindow(
//////                                                            recyclerLocation
//////                                                        )
//////                                                        val recyclerY = recyclerLocation[1]
//////                                                        val recyclerCenterY =
//////                                                            (recyclerY + (chatRecyclerView.height)) / 2
//////
//////                                                        if (lArr[1] > recyclerCenterY) {
//////
//////                                                            var diff: Int =
//////                                                                lArr[1] - recyclerCenterY
//////
//////                                                            Log.d(
//////                                                                ChatActivity::class.java.name,
//////                                                                "Diff" + diff
//////                                                            )
//////
//////                                                                chatRecyclerView.layoutManager?.getChildAt(index)
//////                                                                    ?.findViewById<RecyclerView>(R.id.recycler_answers)
//////                                                                    ?.scrollBy(0,diff+200)
//////
//////                                                                //chatRecyclerView.scrollBy(0, diff)
//////
//////                                                        }
//////
//////                                                        Log.d(
//////                                                            ChatActivity::class.java.name,
//////                                                            "Location On Screen" + chatRecyclerView.height
//////                                                        )
////                                                }
//
//
//                }
//
//
//
//
//
//
////                                                if (indexes[0].toInt() > 0) {
////
////
////
////
////
//////                                        dataSet.get(index)!!.setSpan(StyleSpan(Typeface.NORMAL),
//////                                            0
//////                                            ,dataSet.get(index).length
//////                                        ,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//////
//////                                        dataSet.get(index)!!.setSpan(ForegroundColorSpan(Color.WHITE),
//////                                            0
//////                                            ,dataSet.get(index)!!.length
//////                                            ,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        StyleSpan(Typeface.NORMAL),
////                                                        0,
////                                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
////                                                    )
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        ForegroundColorSpan(Color.WHITE),
////                                                        0,
////                                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
////                                                    )
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        ForegroundColorSpan(Color.YELLOW),
////                                                        indexes[0].toInt(),
////                                                        indexes[1].toInt(),
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        StyleSpan(Typeface.BOLD),
////                                                        indexes[0].toInt(),
////                                                        indexes[1].toInt(),
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////
////
//////
////                                                } else {
////
////
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        StyleSpan(Typeface.NORMAL),
////                                                        0,
////                                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
////                                                    )
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        ForegroundColorSpan(Color.WHITE),
////                                                        0,
////                                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
////                                                    )
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        ForegroundColorSpan(Color.YELLOW),
////                                                        indexes[0].toInt(),
////                                                        indexes[1].toInt(),
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        StyleSpan(Typeface.BOLD),
////                                                        indexes[0].toInt(),
////                                                        indexes[1].toInt(),
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////
////                                                }
////
////
////                                                chatRecyclerView.post {
////                                                    chatAdapter.notifyItemChanged(index)
////                                                }
//
//
//
//            }
//
//            override fun onDone(utteranceId: String?) {
//
//
//                val indexes = utteranceId!!.split(":")
//
//                chatRecyclerView.post {
//
//                    dataSet.get(indexes[0].toInt()).spannableStringBuilder
//                        .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                            this@ChatActivity2,R.color.white))
//                            ,0,dataSet.get(indexes[0].toInt()).spannableStringBuilder.length
//                            ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                    chatAdapter.notifyItemChanged(indexes[0].toInt())
//
//                    //chatRecyclerView.scrollToPosition(indexes[0].toInt())
//                }
//
//                if (indexes[0].toInt() == dataSet.size-1 && isClientClosed) {
//
//                    onGoingIndex=-1
//                    isMarkDownCode = false
//
//
//
//                    //disable stop button
//
//                    runOnUiThread {
//
//                        stopIcon!!.isSelected = false
//
//                        chatEditText.isSelected = false
//                        chatEditText.setDefaultState()
//                        //stopIcon!!.visibility = View.GONE
//
//                        Handler().postDelayed({
//
//                            startSpeechRecognizer()
//
//
//                        },1000)
//
//
//
//                    }
//
//
//
//                }
////
////                                                if (indexes[1].toInt() == dataSet.get(index).spannableStringBuilder.length-1) {
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        StyleSpan(Typeface.NORMAL),
////                                                        0,
////                                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
////
////
////                                                    )
////
////                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                                        ForegroundColorSpan(Color.WHITE),
////                                                        0,
////                                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                                    chatRecyclerView.post {
////
////                                                        chatAdapter.notifyItemChanged(index)
////                                                    }
////
////                                                    chatEditText.isSelected = false
////
//////                                                    dataSet.removeAt(dataSet.size-1)
//////                                                    chatAdapter.notifyItemRemoved(dataSet.size-1)
////
////
////
////                                                }
//
//
//
//            }
//
//            override fun onError(utteranceId: String?) {
//
//            }
//
//
//        })
//
//
//        this@ChatActivity2.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//
//        val gestureDetector:GestureDetector = GestureDetector(this,SwipeGestureListener({direction, velocityX, velocityY ->
//
//            when(direction) {
//
//                Direction.RIGHT-> {
//
//                    if (intent.hasExtra("source")) {
//
//                        val intent = Intent(this, MainActivity::class.java)
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        startActivity(intent)
//
//
//                    } else {
//
//                        val intent = Intent(this, SpikeActivity::class.java)
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        startActivity(intent)
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
//        }))
//
//        layoutBack = findViewById(R.id.back_layout)
//        mainLayout = findViewById(R.id.main)
//        dropDownArrow = findViewById(R.id.dropdown_arrow)
//        headerLabel = findViewById(R.id.header_label)
//        subHeaderLabel = findViewById(R.id.sub_header_label)
//        nameHeader = findViewById(R.id.app_name_header)
//        stopIcon = findViewById(R.id.stop_icon)
//        stopIcon!!.visibility = View.VISIBLE
//        val pandaIcon:ImageView = findViewById(R.id.yellow_panda_menu)
//
//        Handler().postDelayed({ isChecked = !isChecked
//            val stateSet = intArrayOf(android.R.attr.state_checked * (if (isChecked) 1 else -1))
//            pandaIcon.setImageState(stateSet, true)},2000)
//
//        pandaIcon.setOnClickListener {
//
//            isChecked = !isChecked
//
//            val stateSet = intArrayOf(android.R.attr.state_checked * (if (isChecked) 1 else -1))
//            pandaIcon.setImageState(stateSet, true)
//
//            Handler().postDelayed({
//
//                var intent = Intent(this,MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            },1000)
//
//
//        }
//
//
//
//
//        //pandaIcon.isSelected = false
//
//        stopIcon!!.setOnClickListener {
//
//            stopIcon!!.isSelected = !stopIcon!!.isSelected
//
//            if (stopIcon!!.isSelected) {
//
//                Handler().postDelayed({startSpeechRecognizer()
//                    isStopped = false},1000)
//
//
//            } else {
//
//                stopIcon!!.isSelected = false
//
//                TextToSpeechInstance.destroyInstance()
//
//                isStopped = true
//
//                if (httpClient!=null) {
//
//                    if (httpClient?.isActive!!) {
//
//                        httpClient!!.cancel()
//                        httpClient = null
//
//
//
//
//                    } else {
//
//                        httpClient = null
//
//                    }
//
//                }
//
//                if (isAnim) {
//
//                    isAnim = false
//
//                    dataSet.get(index).spannableStringBuilder.clear()
//                    dataSet.get(index).spannableStringBuilder.append("Stopped by user")
//                    dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                        ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                    chatRecyclerView.post {
//
//                        chatAdapter.notifyItemChanged(index)
//                    }
//                } else {
//
//                    if (onGoingIndex!=-1) {
//
//                        chatRecyclerView.post {
//
//                            dataSet.get(onGoingIndex).spannableStringBuilder
//                                .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                                    this@ChatActivity2,R.color.white))
//                                    ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
//                                    ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                            chatAdapter.notifyItemChanged(onGoingIndex)
//
//                            chatRecyclerView.scrollToPosition(onGoingIndex)
//                        }
//
//
//
//
//                    }
//
//
//
////                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                        StyleSpan(Typeface.NORMAL),
////                                        0,
////                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                        ForegroundColorSpan(Color.WHITE),
////                                        0,
////                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                    chatRecyclerView.post {
////
////                                        chatAdapter.notifyItemChanged(index)
////                                    }
//                }
//
//
//
////            else {
////
////
////
////                if (isAnim) {
////
////                                        isAnim = false
////
////                                        dataSet.get(index).spannableStringBuilder.clear()
////                                        dataSet.get(index).spannableStringBuilder.append("Stopped by user")
////                                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
////                                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                        chatRecyclerView.post {
////
////                                            chatAdapter.notifyItemChanged(index)
////                                        }
////                } else {
////
////                    if (onGoingIndex!=-1) {
////
////                        chatRecyclerView.post {
////
////                            dataSet.get(onGoingIndex).spannableStringBuilder
////                                .setSpan(ForegroundColorSpan(ContextCompat.getColor(
////                                    this@ChatActivity2,R.color.white))
////                                    ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
////                                    ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                            chatAdapter.notifyItemChanged(onGoingIndex)
////
////                            chatRecyclerView.scrollToPosition(onGoingIndex)
////                        }
////
////
////
////
////                    }
////                }
////            }
//
//                Handler().postDelayed({startSpeechRecognizer()
//                    isStopped = false},1000)
//
//
//
//
//            }
//
//            //stopIcon!!.visibility = View.GONE
//
//
//        }
//
//
//        countDownLayout = findViewById(R.id.count_down_layout)
//        countDownProgress = countDownLayout!!.findViewById(R.id.progressRing)
//        progressText = countDownLayout!!.findViewById(R.id.progress_text)
//
//        if (isFlow) {
//
//            headerLabel!!.setText(String.format(headerArr.get(Random.nextInt(headerArr.size))
//                ,intent.getStringExtra("subject_name")
//                ,intent.getStringExtra("teacher_name")))
//
//            subHeaderLabel!!.setText(subHeaderArr.get(Random.nextInt(subHeaderArr.size)))
//
//
//        }
//
//
//
//        dropDownArrow!!.scaleType = ImageView.ScaleType.MATRIX
//
//        val px = dropDownArrow!!.drawable.intrinsicWidth/2f
//        val py = dropDownArrow!!.drawable.intrinsicHeight/2f
//
//        constraintSet!!.clone(mainLayout)
//
//
//
//        dropDownArrow!!.setOnClickListener {
//
//            if (headerLabel!!.visibility == View.VISIBLE
//                && subHeaderLabel!!.visibility == View.VISIBLE) {
//
//                chatEditText.visibility = View.GONE
//
////                dropDownArrow!!.rotation = 180f
//
//                matrix.postRotate(180f,px,py)
//                dropDownArrow!!.imageMatrix = matrix
//
//
//
//                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.TOP
//                    ,R.id.app_name_header,ConstraintSet.BOTTOM)
//
//                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.START
//                    ,R.id.app_name_header,ConstraintSet.START)
//
//                //!!.connect(R.id.dropdown_arrow,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,20)
//
//                constraintSet!!.applyTo(mainLayout)
//
//
//
//                headerLabel!!.visibility = View.GONE
//                subHeaderLabel!!.visibility = View.GONE
//
//                chatEditText.visibility = View.GONE
//
//
//
//            } else {
//
//
//
//                matrix.postRotate(-180f,px,py)
//                dropDownArrow!!.imageMatrix = matrix
//
//                headerLabel!!.visibility = View.VISIBLE
//                subHeaderLabel!!.visibility = View.VISIBLE
//
//                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.TOP
//                    ,R.id.sub_header_label,ConstraintSet.BOTTOM)
//
//                constraintSet!!.connect(R.id.chat_recycler,ConstraintSet.START
//                    ,R.id.header_label,ConstraintSet.START)
//
//                //constraintSet!!.connect(R.id.dropdown_arrow,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,20)
//
//                constraintSet!!.applyTo(mainLayout)
//
//                chatEditText.visibility = View.GONE
//
//                if (onGoingIndex!=-1) {
//
//                    chatRecyclerView.scrollToPosition(onGoingIndex)
//                }
//
//            }
//
//
//        }
//
//
//
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
//
//
//
//
//
//
//
//        chatEditText = findViewById(R.id.chat_edit_text)
//        chatEditText.visibility = View.GONE
//        chatEditText.setMovementMethod(ScrollingMovementMethod.getInstance())
//
//
//        chatEditText.setOnDrawableClickListener(object : ChatEditTextView.OnDrawableClickListener {
//            override fun onClick(position: ChatEditTextView.DrawablePosition) {
//
//                when(position) {
//
//                    ChatEditTextView.DrawablePosition.RIGHT->{
//
//                        if (chatEditText.isRightSelected) {
//
//                            if (chatEditText.isSelected) {
//
//                                isStopped = false
//
//                                if(!chatEditText.text.toString().isBlank()) {
//
//                                    processData1(chatEditText.text.toString())
//
//                                } else {
//
//                                    Toast.makeText(this@ChatActivity2
//                                        ,"Enter valid question"
//                                        ,Toast.LENGTH_SHORT).show()
//
//                                    chatEditText.isSelected = false
//                                    chatEditText.setDefaultState()
//
//                                }
//
//                            } else {
//
//                                TextToSpeechInstance.destroyInstance()
//
//                                isStopped = true
//
//                                if (httpClient!=null) {
//
//                                    if (httpClient?.isActive!!) {
//
//                                        httpClient!!.cancel()
//                                        httpClient = null
//
//                                        //TextToSpeechInstance.destroyInstance()
//
//
//                                    } else {
//
//                                        httpClient = null
//                                        //TextToSpeechInstance.destroyInstance()
//                                    }
//
//                                    if (isAnim) {
//
//
//                                        isAnim = false
//
//                                        dataSet.get(index).spannableStringBuilder.clear()
//                                        dataSet.get(index).spannableStringBuilder.append("Stopped by user")
//                                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                        chatRecyclerView.post {
//
//                                            chatAdapter.notifyItemChanged(index)
//                                        }
//                                    } else {
//
//                                        if (onGoingIndex!=-1) {
//
//                                            chatRecyclerView.post {
//
//                                                dataSet.get(onGoingIndex).spannableStringBuilder
//                                                    .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                                                        this@ChatActivity2,R.color.white))
//                                                        ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
//                                                        ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                                chatAdapter.notifyItemChanged(onGoingIndex)
//
//                                                chatRecyclerView.scrollToPosition(onGoingIndex)
//                                            }
//
//                                        }
//
//
//
////                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                        StyleSpan(Typeface.NORMAL),
////                                        0,
////                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                        ForegroundColorSpan(Color.WHITE),
////                                        0,
////                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                    chatRecyclerView.post {
////
////                                        chatAdapter.notifyItemChanged(index)
////                                    }
//                                    }
//
//
//                                } else {
//
//
//
//                                    if (isAnim) {
//
//                                        isAnim = false
//
//                                        dataSet.get(index).spannableStringBuilder.clear()
//                                        dataSet.get(index).spannableStringBuilder.append("Stopped by user")
//                                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                        chatRecyclerView.post {
//
//                                            chatAdapter.notifyItemChanged(index)
//                                        }
//                                    } else {
//
//                                        if (onGoingIndex!=-1) {
//
//                                            chatRecyclerView.post {
//
//                                                dataSet.get(onGoingIndex).spannableStringBuilder
//                                                    .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                                                        this@ChatActivity2,R.color.white))
//                                                        ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
//                                                        ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                                chatAdapter.notifyItemChanged(onGoingIndex)
//
//                                                chatRecyclerView.scrollToPosition(onGoingIndex)
//                                            }
//
//
//
//
//                                        }
//                                    }
//                                }
//
//                            }
//                        }
//
//                    }
//
//                    ChatEditTextView.DrawablePosition.LEFT->{
//
//                        if (chatEditText.isLeftSelected) {
//
//                            if (chatEditText.isSelected) {
//
//                                isStopped = false
//
//                                if (SpeechRecognizer.isRecognitionAvailable(this@ChatActivity2)) {
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
//
//                            } else {
//
//                                TextToSpeechInstance.destroyInstance()
//
//                                isStopped = true
//
//                                if (httpClient!=null) {
//
//                                    if (httpClient?.isActive!!) {
//
//                                        httpClient!!.cancel()
//                                        httpClient = null
//
//                                    } else {
//
//                                        httpClient = null
//
//                                    }
//
//                                    if (isAnim) {
//
//                                        isAnim = false
//
//                                        dataSet.get(index).spannableStringBuilder.clear()
//                                        dataSet.get(index).spannableStringBuilder.append("Stopped by user")
//                                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                        chatRecyclerView.post {
//
//                                            chatAdapter.notifyItemChanged(index)
//                                        }
//                                    } else {
//
//                                        if (onGoingIndex!=-1) {
//
//                                            chatRecyclerView.post {
//
//                                                dataSet.get(onGoingIndex).spannableStringBuilder
//                                                    .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                                                        this@ChatActivity2,R.color.white))
//                                                        ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
//                                                        ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                                chatAdapter.notifyItemChanged(onGoingIndex)
//
//                                                chatRecyclerView.scrollToPosition(onGoingIndex)
//                                            }
//
//
//
//
//                                        }
//
//
//
////                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                        StyleSpan(Typeface.NORMAL),
////                                        0,
////                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
////                                        ForegroundColorSpan(Color.WHITE),
////                                        0,
////                                        dataSet.get(index).spannableStringBuilder!!.length,
////                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                    chatRecyclerView.post {
////
////                                        chatAdapter.notifyItemChanged(index)
////                                    }
//                                    }
//
//
//                                } else {
//
//
//
//                                    if (isAnim) {
//
////                                        isAnim = false
////
////                                        dataSet.get(index).spannableStringBuilder.clear()
////                                        dataSet.get(index).spannableStringBuilder.append("Stopped by user")
////                                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
////                                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                        chatRecyclerView.post {
////
////                                            chatAdapter.notifyItemChanged(index)
////                                        }
//                                    } else {
//
//                                        if (onGoingIndex!=-1) {
//
//                                            chatRecyclerView.post {
//
//                                                dataSet.get(onGoingIndex).spannableStringBuilder
//                                                    .setSpan(ForegroundColorSpan(ContextCompat.getColor(
//                                                        this@ChatActivity2,R.color.white))
//                                                        ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
//                                                        ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                                chatAdapter.notifyItemChanged(onGoingIndex)
//
//                                                chatRecyclerView.scrollToPosition(onGoingIndex)
//                                            }
//
//
//
//
//                                        }
//                                    }
//                                }
//
//                            }
//                        }
//
//
//
//
//                    }
//
//                    ChatEditTextView.DrawablePosition.OTHER->{
//
//                        chatEditText.requestFocus()
//
//                        chatEditText.setFocusable(true)
//                        chatEditText.isFocusableInTouchMode = true
//
//                        showKeyboardFocus(true)
//                    }
//                }
//
//
//
//
//            }
//
//
//        })
//
//
//        chatRecyclerView = findViewById(R.id.chat_recycler)
//        chatRecyclerView.layoutManager = LinearLayoutManager(this@ChatActivity2)
//
//        chatAdapter = ChatAdapter()
////        chatAdapter.setHasStableIds(true)
//        chatRecyclerView.adapter = chatAdapter
//
//
//        chatRecyclerView.addOnScrollListener(object:RecyclerView.OnScrollListener(){
//
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//
//                    isScrolling = false
//                }
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//
//                if (dy>0) {
//
//                    isScrolling = true
//                }
//
//
//            }
//        })
//
//        Handler().postDelayed({
//
//            chatEditText.isLeftSelected = true
//
//            initModel()
//
//
//        }
//            ,2000)
//
//
//
//
//
//
//    }
//
//    private fun initModel() {
//
//        if (SpeechRecognizer.isRecognitionAvailable(this@ChatActivity2)) {
//
//            checkAudioPermissionAndStart()
//
//
//        } else {
//
//            chatEditText.isSelected = false
//
//            showAlertPromptForGoogleApp()
//
//                                if (!checkGoogleAppAvailability()) {
//
//                                    installGoogleApp()
//                                }
//        }
//
////        WhisperInstance.getInstance(this)
////        WhisperInstance.setDataListener(this)
////        WhisperInstance.startSpeech(this)
////
////        Handler().postDelayed({
////
////            WhisperInstance.stopSpeech()
////
////        },6000)
//    }
//
////    private fun initModel() {
////        StorageService.unpack(
////            this, "model-en-us", "model",
////            { model: Model ->
////                this.model = model
////                val rec = Recognizer(model, 16000.0f)
////                speechService = SpeechService(rec, 16000.0f)
////                speechService!!.startListening(this)
////
////
////                //setUiState(org.vosk.demo.VoskActivity.STATE_READY)
////            },
////            {
////                    exception: IOException -> Log.d(MainActivity::class.java.name,"Failed to unpack the model" + exception.message)
////            }
////        )
////    }
//
//    private suspend fun initiateCountDown() {
//
//        Log.d(ChatActivity::class.java.name, "launched")
//
//        runOnUiThread {
//
//            countDownLayout?.visibility = View.VISIBLE
//            countDownProgress?.max = 60
//
//
//        }
//
//        for (i in 0..60) {
//
//            runOnUiThread {
//
//                countDownProgress?.progress = 60-i
//                progressText?.text = countDownProgress?.progress.toString()
//
//                if (countDownProgress?.progress == 0) {
//
//                    val customAlertDialogBuilder:CustomAlertDialogBuilder = CustomAlertDialogBuilder(object : CustomAlertDialogBuilder.DialogButtonClickListener{
//                        override fun onClickPositiveButton(dialog: DialogInterface) {
//
//                            dialog.dismiss()
//                            initiateFeedBackDialog()
//                        }
//
//
//                    })
//                    customAlertDialogBuilder.buildAlertDialog(this@ChatActivity2
//                        ,"Hello!"
//                        ,"Class has ended"
//                        , R.drawable.panda).show()
//
//
//                }
//
//            }
//
//            delay(1000)
//
//
//
//
//        }
//
//
//    }
//
//    private fun initiateFeedBackDialog() {
//
//        var widthPixels = (resources.displayMetrics.widthPixels*0.70f).toInt()
//        var heightPixels = (resources.displayMetrics.heightPixels*0.80f).toInt()
//
//        var dialogBuilder:CustomDialogBox = CustomDialogBox().buildDialog(this,R.layout.dialog_your_feedback)
//            .setSize(widthPixels,heightPixels)
//
//        var dialog = dialogBuilder.createDialog()
//        dialog.window?.apply {
//
//            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//            enterFullScreenMode(this)
//
//        }
//
//
//        var linearLayoutStars:LinearLayout = dialog.findViewById(R.id.linear_layout_stars)
//        var reviewEdtText:EditText = dialog.findViewById(R.id.review_edit_text)
//        reviewEdtText.movementMethod = ScrollingMovementMethod.getInstance()
//
//        var submitBtn:Button = dialog.findViewById(R.id.review_submit_button)
//
//        submitBtn.setOnClickListener {
//
//            sendFeedBack(reviewEdtText)
//
//            //post To server
//        }
//
//
//
//        for (i in 0 until  linearLayoutStars.childCount) {
//
//            if (i == 0) {
//
//
//                //set One
//
//                (linearLayoutStars.getChildAt(i) as ImageView).setOnClickListener {
//
//                    starsCount = i+1
//
//                    deselectAllSelectedStars(linearLayoutStars)
//
//                    (linearLayoutStars.getChildAt(i) as ImageView).isSelected = true
//                }
//
//
//
//
//            } else {
//
//
//                (linearLayoutStars.getChildAt(i) as ImageView).setOnClickListener {
//
//                    starsCount = i+1
//
//                    deselectAllSelectedStars(linearLayoutStars)
//
//                    for (j in 0 ..i) {
//
//                        //setSelectedAll
//                        linearLayoutStars.getChildAt(j).isSelected = true
//
//                    }
//
//
//                }
//
//
//
//
//            }
//
//
//
//        }
//
//
//
//        dialog.show()
//
//    }
//
//    private fun sendFeedBack(edtText: EditText) {
//
//        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
//            override fun onResult(code: Int, response: okhttp3.Response?) {
//
//                val intent = Intent(this@ChatActivity2,SpikeActivity::class.java)
//                startActivity(intent)
//
//                finish()
//
//            }
//
//            override fun onError(code: Int, response: okhttp3.Response?) {
//
//            }
//
//            override fun onException(error: String?) {
//
//            }
//
//        })
//
//        val params:MutableMap<String,String> = mutableMapOf()
//        params.put("assignment",intent.getStringExtra("assignmentId")!!)
//        params.put("teacher",intent.getStringExtra("teacherId")!!)
//        params.put("rate",starsCount.toString())
//
//        if (edtText.text.toString().isNotBlank()) {
//
//            params.put("message",edtText.text.toString())
//
//        }
//
//
//
//
//        OkHttpClientInstance.post(OkHttpClientInstance.BASEURL+OkHttpClientInstance.feedBackUrl
//            ,params)
//    }
//
//    private fun deselectAllSelectedStars(linearLayout: LinearLayout) {
//
//        for (j in 0 until linearLayout.childCount) {
//
//            //setSelectedAll
//            linearLayout.getChildAt(j).isSelected = false
//
//        }
//
//
//
//
//    }
//
//
//
////    private fun showProgressDialog() {
////
////        var width = resources.displayMetrics.widthPixels
////        var height = resources.displayMetrics.heightPixels * 0.20
////
////        var view = layoutInflater.inflate(R.layout.layout_countdown_dialog,null,false)
////
////
////        dialog = Dialog(this)
////        dialog?.setContentView(view)
////        dialog?.create()
////        dialog?.window?.setLayout(width,height.toInt())
////        dialog?.show()
////
////        val progressBar: ProgressBar = view.findViewById(R.id.progressRing)
////        val progressText = view.findViewById<TextView>(R.id.progress_text)
////        val progressMsg = view.findViewById<TextView>(R.id.progress_msg)
////
////        lifecycleScope.launch {
////
////            for (i in 1..10) {
////
////
////
////                delay(1000)
////
////                if (!isConnected) {
////
////                    progressMsg.animate()
////                        .alpha(0f)
////                        .setDuration(300)
////                        .withEndAction {
////
////                            progressMsg.animate()
////                                .alpha(1f)
////                                .setDuration(300)
////                                .withEndAction {
////
////                                    progressMsg.text = "Disconnected"
////
////                                    Handler().postDelayed({
////
////                                        dialog?.dismiss()
////                                        view = null
////                                        dialog = null
////
////
////                                    },500)
////                                }
////                        }
////
////
////                } else {
////
////                    runOnUiThread {
////
////                        progressBar.progress = i*10
////                        progressText.setText("${progressBar.progress.toString()}%")
////
////                    }
////
////                    if (i == 10) {
////
////                        dialog?.dismiss()
////                        view = null
////                        dialog = null
////
////
////                    } else if (i==5) {
////
////                        progressMsg.animate()
////                            .alpha(0f)
////                            .setDuration(300)
////                            .withEndAction {
////
////                                progressMsg.text = ""
////
////                                progressMsg.animate()
////                                    .alpha(1f)
////                                    .setDuration(300)
////                                    .withEndAction {
////
////                                        progressMsg.text = "Almost there..."
////                                    }
////                            }
////                    }
////
////
////                }
////
////
////
////
////
////            }
////
////
////
////
////        }
////
////    }
//
//    private fun showKeyboardFocus(visible:Boolean) {
//
//        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//
//        if (visible) {
//
//            imm.showSoftInput(chatEditText,InputMethodManager.SHOW_IMPLICIT)
//            //imm = null
//
//        }
//
//
//    }
//
//    private fun enterFullScreenMode(window: Window) {
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//
//            window.insetsController?.apply {
//
//                hide(WindowInsetsCompat.Type.statusBars())
//                hide(WindowInsetsCompat.Type.navigationBars())
//                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            }
//
//        } else {
//
//
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                    View.SYSTEM_UI_FLAG_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_LOW_PROFILE
//        }
//
//
//
//    }
//
//
//
//
//
////    private suspend fun parseBytes(byteArray: ByteArray) {
////
////
////        var i = 0
////        var codePoint = 0
////        var numBytes = 0
////        //var sb:StringBuilder = StringBuilder()
////
////        while (i < byteArray.size) {
////
////            var unsignedFirst = byteArray[i].toInt() and (0xFF)
////
////            if (unsignedFirst shr (7) == 0b0) {
////
////                codePoint = unsignedFirst
////                numBytes = 1
////                i += numBytes
////
////            } else if (unsignedFirst shr (5) == 0b110) {
////
////                var second = byteArray[i + 1].toInt() and (0x3F)
////
////                codePoint = ((unsignedFirst and (0x1F)) shl (6)) or (second)
////                numBytes = 2
////                i += numBytes
////
////            } else if (unsignedFirst shr (4) == 0b1110) {
////
////                var second = byteArray[i + 1].toInt() and (0x3F)
////                var third = byteArray[i + 2].toInt() and (0x3F)
////
////                codePoint = ((unsignedFirst and (0x0F)) shl (12)) or
////                        (second shl (6)) or
////                        third
////
////
////                numBytes = 3
////                i += numBytes
////
////            } else if (unsignedFirst shr (3) == 0b11110) {
////
////                var second = byteArray[i + 1].toInt() and (0x3F)
////                var third = byteArray[i + 2].toInt() and (0x3F)
////                var fourth = byteArray[i + 3].toInt() and (0x3F)
////
////                codePoint = ((unsignedFirst and (0x07)) shl (18)) or
////                        (second shl (12)) or
////                        (third shl (6)) or
////                        fourth
////
////
////                numBytes = 4
////                i += numBytes
////
////
////            } else {
////
////                throw IOException("InValid Encoding")
////            }
////
////            val s: String = String(Character.toChars(codePoint))
////            //s.intern()
////
//////            strBuilder!!.append(s)
//////            strBuilder!!.setSpan(ForegroundColorSpan(Color.WHITE),0
//////                ,strBuilder!!.length
//////                ,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
//////            (s.equals("\n")||s.equals(".")) &&
////
////            if (word.length > 0) {
////
////
////                //word+=String(Character.toChars(codePoint))
////                //strBuilder!!.append(word)
////                word.appendCodePoint(codePoint)
////                strBuilder!!.append(s)
////                startIndex = strBuilder!!.indexOf(word.toString(), startIndex)
////                Log.d(ChatActivity::class.java.name, "index: " + startIndex)
////                //startIndex = strBuilder!!.lastIndexOf(word.toString(), ignoreCase = true)
////
////
////                //TextToSpeechInstance.speak(strBuilder!!,startIndex, strBuilder!!.length)
////
////                Log.d(ChatActivity::class.java.name, "parseBytes: " + word)
////
////
////            } else  {
////
////                word.appendCodePoint(codePoint)
////                strBuilder!!.append(word)
////
//////                var p = Pattern.compile("[a-zA-Z0-9]+")
//////                var m = p.matcher(s)
//////
//////                if (m.matches()) {
//////
//////                    word.appendCodePoint(codePoint)
//////                    strBuilder!!.append(s)
//////
//////
//////                }
////
////
////            }
////
////            if (!s.isBlank()) {
////
////                if (s.equals("\n") || s.equals(".")) {
////
////                    //strBuilder!!.append(s)
////
////                    TextToSpeechInstance.speak(word.toString(), startIndex, startIndex + word.length)
////
////                    //delay(1000)
////                    //TextToSpeechInstance.speak(strBuilder!!,startIndex,startIndex+word.length)
////
////                    word.clear()
////
////
////                }
////
////
////            }
////
////
////
////
////
////
////            //var char:String = String(Character.toChars(codePoint))
////
////            //strBuilder!!.appendCodePoint(codePoint)
////
////
//////            if (!p.matcher(char).matches()) {
//////
//////                strBuilder.lastIndexOf()
//////
//////                startIndex++
//////                //strBuilder!!.appendCodePoint(codePoint)
//////
//////
//////            } else {
//////
//////                //strBuilder!!.appendCodePoint(codePoint)
//////
//////                //tts.speak(strBuilder)
//////
//////                //TextToSpeechInstance.speak(strBuilder!!,startIndex,strBuilder!!.length)
//////
//////
//////
//////
//////            }
////
////
////            dataSet.set(index, strBuilder!!)
////            chatRecyclerView.post {
////                chatAdapter.notifyItemChanged(index)
////            }
////            //chatAdapter.notifyItemChanged(index)
////
//////            chatRecyclerView.post {
//////
//////                //chatRecyclerView.scrollToPosition(index+1)
//////            }
////
////            //chatRecyclerView.scrollToPosition(index)
////
////
//////            runOnUiThread {
//////
//////                textView?.setText(sb)
//////
//////            }
////
//////            delay(50)
////
////        }
////
////        //Log.d(MainActivity::class.java.name, "parseBytes: " + sb.toString())
////
////
////    }
//
//
//
//
//
//
//    private inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
//
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//
//            return ViewHolder(
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.single_item_chat_question, parent, false)
//            )
//
//        }
//
//        override fun getItemCount(): Int {
//
//            return dataSet.size
//
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
////            holder.textView.text = dataSet[position].spannableStringBuilder
//
//            //holder.textView.append(dataSet.get(position).spannableStringBuilder.subSequence(startIndex, dataSet.get(position).spannableStringBuilder.length))
//
//
//
//
////            if ((position) % 2 == 0) {
////
////                //questions
////
////                holder.textView.text = dataSet[position].spannableStringBuilder
////
////                if (!dataSet.get(position).isProcess) {
////
////                    holder.logo.setImageDrawable(
////                        ContextCompat.getDrawable(
////                            this@ChatActivity,
////                            R.drawable.panda
////                        )
////                    )
////
////
////                } else {
////
////                    holder.logo.visibility = View.INVISIBLE
////                }
////
////            } else {
////
////                //answers
////
////                holder.textView.visibility = View.GONE
////                holder.ansRecycler.visibility = View.VISIBLE
////
////                holder.logo.setImageDrawable(
////                    ContextCompat.getDrawable(
////                        this@ChatActivity,
////                        R.drawable.panda_1
////                    )
////                )
////
////                (holder.ansRecycler.adapter as AnswersAdapter).notifyDataSetChanged()
////
////
////
////
////            }
//
//            if (dataSet.get(position).isQuestion) {
//
//                holder.logo.visibility = View.VISIBLE
//
//                holder.logo.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        this@ChatActivity2,
//                        R.drawable.panda
//                    )
//                )
//
//                holder.textView.text = dataSet[position].spannableStringBuilder
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
//                            this@ChatActivity2,
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
//                holder.textView.text = dataSet[position].spannableStringBuilder
//            }
//
//            if (isAnim) {
//
//                if (position == index) {
//
//                    initAnimator(holder.textView, isAnim)
//                }
//            } else {
//
//                textAnimator?.let {
//
//                    initAnimator(holder.textView, isAnim)
//
//
//                }
//
//
//            }
//
//
//        }
//
//
//        override fun getItemId(position: Int): Long {
//
////            return super.getItemId(position)
//
//            return dataSet.get(position).id
//        }
//
//        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
//
//            var textView: TextView = v.findViewById(R.id.text_query)
//            var logo: ImageView = v.findViewById(R.id.text_logo)
//
//        }
//
//    }
//
//    private inner class AnswersAdapter : RecyclerView.Adapter<AnswersAdapter.ViewHolder>() {
//
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//
//            return ViewHolder(
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.single_item_answers, parent, false)
//            )
//
//
//        }
//
//        override fun getItemCount(): Int {
//
//            return dataSet.get(index).ansList?.size!!
//
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//
//            holder.textView.text = dataSet[index].ansList?.get(position)
//
//        }
//
////        override fun getItemViewType(position: Int): Int {
////
////            if ((position+1)%2 == 0) {
////
////                return 2
////
////
////            } else {
////
////                return 1
////            }
////
////        }
//
//        override fun getItemId(position: Int): Long {
//
//            return position.toLong()
//        }
//
//        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
//
//            var textView: TextView = v.findViewById(R.id.answer_text)
//
//
//        }
//
//    }
//
//    private fun getDataKtor(
//        query: String? = "Please recite the poem 'What a bird thought'.",
//        cls: Int = 6,
//        subject: String? = "english"
//    ): Flow<ByteArray> = flow {
//
//
//        val json = JSONObject().apply {
//            put("query_text", query)
//            put("class_number", cls)
//            put("subject", subject)
//        }
//
//        httpClient = HttpClient(CIO) {
//            install(ContentNegotiation) {
//                json()
//            }
//
//            engine {
//                requestTimeout = 0 // ✅ For streaming: disable total timeout
//                endpoint {
//                    connectTimeout = 15_000
//                    socketTimeout = 60_000
//                }
//            }
//        }
//
//        httpClient!!.preparePost(RetrofitInstanceBuilder.BASEURL + RetrofitInstanceBuilder.GENERIC_TASK) {
//
//            contentType(Json)
//
//            setBody(json.toString())
//        }.execute {
//
//
//            if (it.status.isSuccess()) {
//
//
//                val channel: ByteReadChannel = it.bodyAsChannel()
//
//
//                val buffer = ByteArray(1024)
//
//                while (!channel.isClosedForRead) {
//
//
//
//                    try {
//
//                        val bytesRead: Int = channel.readAvailable(buffer, 0, buffer.size)
//
//                        emit(buffer.copyOf(bytesRead))
//
//                    } catch (ex: Exception) {
//
//                        if (isAnim) {
//
//                            dataSet.get(index).spannableStringBuilder.clear()
//                            dataSet.get(index).spannableStringBuilder.append("Server Error : ${ex.message}")
//                            dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                                ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//                            isAnim = false
//                            chatEditText.isSelected = false
//
////                            runOnUiThread {
////
////                                stopIcon!!.isSelected = false
////                            }
//
//
//                            chatRecyclerView.post {
//
//                                chatAdapter.notifyItemChanged(index)
//                            }
//
//
//                        } else {
//
//                            chatEditText.isSelected = false
//
//
//                        }
//
////                        if (dataSet.get(index).length == 0) {
////
////                            dataSet.get(index).clear()
////                            dataSet.get(index).append("Server Error : ${ex.message}")
////                            dataSet.get(index).setSpan(ForegroundColorSpan(Color.RED)
////                                ,0,dataSet.get(index).length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
////                            isAnim = false
////
////
////                            chatRecyclerView.post {
////
////                                chatAdapter.notifyItemChanged(index)
////                            }
////
////
////                        }
//
//
////                        runOnUiThread {
////
////                            Toast.makeText(
////                                this@ChatActivity, "Server Error : ${it.status.value} ${ex.message}", Toast.LENGTH_SHORT
////                            ).show()
////
////                        }
//
//
//
//                    }
//
//                }
//
//                chatEditText.isSelected = false
//
//
//            }
//
//
//        }
//
//        httpClient!!.close()
//
//    }.flowOn(Dispatchers.IO)
//
//    private suspend fun getDataKtorr(
//        query: String? = "Please recite the poem 'What a bird thought'.",
//        cls: Int = 6,
//        subject: String? = "english"
//    ): Flow<String?> = flow {
//
//        Log.d(TAG, "getDataKtorr: ${query} ${cls} ${subject}")
//
//
//        val json = JSONObject().apply {
//            put("query_text", query)
//            put("task_type", "creative")
////            put("subject", subject)
//        }
//
//
//
//        httpClient?.preparePost(RetrofitInstanceBuilder.BASEURL + RetrofitInstanceBuilder.GENERIC_TASK) {
//
//            contentType(Json)
//
//            setBody(json.toString())
//        }?.execute {
//
////            val channel: ByteReadChannel = it.bodyAsChannel()
////            val buffer = ByteArray(4096)
//
//
//
////            while (!channel.isClosedForRead) {
////                val buffer:ByteBuffer = ByteBuffer(DEFAULT_BUFFER_SIZE)
////                channel.readAvailable(buffer)
////                val trimmed = buffer.dropLastWhile { it == 0.toByte() }.toByteArray()
////                emit(String(trimmed))
////            }
//
//
//            val channel = it.bodyAsChannel()
//
//            val p = Pattern.compile("\\s+")
//
//            var byteBuff:ByteArray = ByteArray(1024)
//            val buffer = StringBuilder()
//            val tempBuffer = StringBuilder()
//
//            while (!channel.isClosedForRead) {
//
//                stopIcon!!.isSelected = true
//
//                try {
//
//                    val bytesRead = channel.readAvailable(byteBuff,0,byteBuff.size)
//
//                    val chunk = String(byteBuff,0,bytesRead,StandardCharsets.UTF_8)
//
//                    buffer.append(chunk)
//
//                    var newLineIndex:Int
//
//                    while (true) {
//
//                        newLineIndex = buffer.indexOf("\n")
//
//                        if (newLineIndex==-1) {
//
//                            break
//                        }
//
//                        val line = buffer.substring(0,newLineIndex+1)
//                        buffer.delete(0,newLineIndex+1)
//                        //val trimmed = line.trimEnd('\r','\n')
//
//                        if (line.isNotBlank()) {
//
//                            if (line.startsWith("```")) {
//
//                                isMarkDownCode = !isMarkDownCode
//
//                                tempBuffer.append(line.trim()+"\n")
//
//
//
//                                if (!isMarkDownCode) {
//
////                            val node = markWon!!.parse(
////                                line!!.trim().replace("[$]{2,}|\\$\\s|\n+".toRegex(), "")
////                            )
////                            renderedMarkdown = markWon!!.render(node)
//
//                                    emit(tempBuffer.toString().trim()+"\n")
//                                    tempBuffer.clear()
//
////                                    tempBuffer.append(line + "\n")
////                                    isMarkDownCode = true
//
//                                }
//                            } else {
//
//                                if (isMarkDownCode) {
//
//                                    tempBuffer.append(line.trim() + "\n")
//
//                                } else {
//
//                                    emit(line.trim()+"\n")
//
//
//                                }
//                            }
//                        }
//
//                        if (buffer.isNotEmpty()) {
//
//                            if (isMarkDownCode) {
//
//                                tempBuffer.append(buffer.toString().trim()+"\n")
//
//                            } else {
//
//                                emit(buffer.toString().trim()+"\n")
//
//
//                            }
//                        }
//
//                    }
//
//                    isClientClosed = false
//
//                } catch (ex:Exception) {
//
//                    Log.d(ChatActivity::class.java.name, "getDataKtorr: "+ ex.message)
//
//                    if (isAnim) {
//
//                        isAnim = false
//
//
//
////                        dataSet.get(index).spannableStringBuilder.clear()
////                        dataSet.get(index).spannableStringBuilder.append("Server Error : ${ex.message}")
////                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
////                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
////                        isAnim = false
////                        chatEditText.isSelected = false
////
////
////                        chatRecyclerView.post {
////
////                            chatAdapter.notifyItemChanged(index)
////                        }
//
//
//                    } else {
//
//                        chatEditText.isSelected = false
//
//
//                    }
//
//
//                }
//
//
//
//
////
//
////                try {
////
////                    val line = channel.readUTF8Line()
////                    if (line != null) {
////
////                        Log.d(TAG, "Line: " + line)
////
////                        //emit(line)
////
////                        if (line.trim().isNotBlank()) {
////
////                            emit(line)
////                        }
////
//////                        val matcher = p.matcher(line)
//////
//////                        if (!matcher.matches()) {
//////
//////                            Log.d(ChatActivity::class.java.name, "getDataKtorr: " + line)
//////
//////                            emit(line) // ✅ emit as Flow<String>
//////
//////
//////                        }
////
////                    }
////
////                } catch (ex:Exception) {
////
////                    Log.d(ChatActivity::class.java.name, "getDataKtorr: "+ ex.message)
////
////                    if (isAnim) {
////
////                        isAnim = false
////
////
////
//////                        dataSet.get(index).spannableStringBuilder.clear()
//////                        dataSet.get(index).spannableStringBuilder.append("Server Error : ${ex.message}")
//////                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//////                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//////                        isAnim = false
//////                        chatEditText.isSelected = false
//////
//////
//////                        chatRecyclerView.post {
//////
//////                            chatAdapter.notifyItemChanged(index)
//////                        }
////
////
////                    } else {
////
////                        chatEditText.isSelected = false
////
////
////                    }
////
////
////                }
//
//            }
//
//            isAnim = false
//            isClientClosed = true
//
//            httpClient?.cancel()
//            httpClient?.close()
//            httpClient = null
//
//            emit("Ended")
//
//            Log.d(ChatActivity::class.java.name, "End Reached")
//
////            dataSet.removeAt(sampleScrolling)
////            chatRecyclerView.post {
////
////                chatAdapter.notifyItemRemoved(sampleScrolling)
////            }
////
////            sampleScrolling = -1
//
//
//
//
//
//        }
//
//
//
//    }.flowOn(Dispatchers.IO)
//
//
//
//    private fun startSpeechRecognizer() {
//
//        isMarkDownCode = false
//
//        chatEditText.setText("")
//
//        isSpeechStarted = true
//
////        SpeechRecognizerInstance.getInstance(this)
////        SpeechRecognizerInstance.setSpeechListener(object : RecognitionListener {
////            override fun onReadyForSpeech(params: Bundle?) {
////
////                listenDialog!!.show()
////                stopIcon!!.isSelected = true
////
////
////
////
////
//////                Toast.makeText(this@ChatActivity2
//////                    ,"Speak Now..."
//////                    , Toast.LENGTH_SHORT).show()
////
////            }
////
////            override fun onBeginningOfSpeech() {
////
////            }
////
////            override fun onRmsChanged(rmsdB: Float) {
////
////            }
////
////            override fun onBufferReceived(buffer: ByteArray?) {
////
////            }
////
////            override fun onEndOfSpeech() {
////
////            }
////
////            override fun onError(error: Int) {
////
////                isProcessing = false
////
////                if (error == 9) {
////
////
////                    chatEditText.isSelected = false
////
////                    showAlertPrompt("Alert"
////                        ,"Go to settings to enable Google microphone permission"
////                        ,R.drawable.ic_android_black_24dp)
////
////                }
////
////                Log.d(SelectClassActivity::class.java.name, "onError: ${error}")
////
////
////
////            }
////
////            override fun onResults(results: Bundle?) {
////
////                isSpeechStarted = false
////
////
////                var runnable:java.lang.Runnable = java.lang.Runnable{
////
////                    var i = 0
////
////                    while (i<5) {
////
////                        if (isSpeechStarted) {
////
////
////                            handler!!.removeCallbacks(r!!)
////
////                        }
////
////                        i++
////                    }
////
////                    var speechResultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
////                    Log.d(SelectClassActivity::class.java.name, "Results: ${speechResultList?.get(0)}")
////
////                    speechResultList?.let {
////
////                        runOnUiThread {
////
////                            chatEditText.setText(speechResultList.get(0))
////                            listenDialog!!.findViewById<TextView>(R.id.speech_text)
////                                .setText(speechResultList.get(0))
////
////                            Handler().postDelayed({
////                                listenDialog!!.dismiss()
////                                processData1(speechResultList.get(0))},2000)
////
////
////
////                        }
////
////                    }
////
////                }
////
////                handler!!.post(runnable)
////
////            }
////
////            override fun onPartialResults(partialResults: Bundle?) {
////
////            }
////
////            override fun onEvent(eventType: Int, params: Bundle?) {
////
////            }
////
////
////        })
////        SpeechRecognizerInstance.startSpeech()
//
//    }
//
//    var isMarkDownCode = false
//
//    private fun processData1(data:String) {
//
//        chatEditText.isLeftSelected = true
//        chatEditText.isRightSelected = false
//
//
//        var isFirst = true
//        dataSet.add(DataSet(System.currentTimeMillis(), MySpannableStringBuilder(data),false,null,true,true))
//        chatAdapter.notifyItemInserted(dataSet.size - 1)
//
////        if (dataSet.isEmpty()) {
////
////            dataSet.add(DataSet(System.currentTimeMillis(), MySpannableStringBuilder(data),false,null,true,true))
////            chatAdapter.notifyItemInserted(dataSet.size - 1)
////
////        } else {
////
////            dataSet.set(dataSet.size-1,DataSet(System.currentTimeMillis(), MySpannableStringBuilder(data),false,null,true))
////            chatAdapter.notifyItemChanged(dataSet.size - 1)
////        }
//
//
//        chatEditText.setText("")
//
//
//        dataSet.add(DataSet(System.currentTimeMillis(),MySpannableStringBuilder("Thinking"),false,ansDataList,false,true))
//
//        isAnim = true
//
//        index = dataSet.size - 1
//
//        chatAdapter.notifyItemInserted(index)
//
//
//        chatRecyclerView.scrollToPosition(index)
//
//        stopIcon!!.visibility = View.VISIBLE
//
//
//        lifecycleScope.launch {
//
//            if (!isStopped) {
//
//                if (httpClient == null) {
//
//                    httpClient = HttpClient(CIO) {
//                        install(ContentNegotiation) {
//                            json()
//                        }
//
//                        engine {
//                            requestTimeout = 0 // ✅ For streaming: disable total timeout
//                            endpoint {
//                                connectTimeout = 15_000
//                                socketTimeout = 60_000
//                            }
//                        }
//                    }
//
//
//                }
//            }
//
//            try {
//
//                getDataKtorr(
//                    dataSet.get(index - 1).toString(),
//                    6,
//                    "english"
//                )
//                    .collect({
//
//                        if (isAnim) {
//
//                            isAnim = false
//
//                            dataSet.get(dataSet.size-1).spannableStringBuilder.clear()
//
//                            //dataSet.get(dataSet.size-1).spannableStringBuilder.append("")
//
////                            chatRecyclerView.post {
////
////                                chatAdapter.notifyItemChanged(dataSet.size-1)
////
////                            }
//
//                        }
//
//                        it?.let {
//
//                            if (it.isNotBlank()) {
//
//                                if (!it.equals("Ended")) {
//
//                                    val node = markWon!!.parse(
//                                        it!!.trim().replace("[$]{2,}|\\$\\s".toRegex(), "")
//                                    )
//                                    var renderedMarkdown = markWon!!.render(node)
//
//                                    dataSet.add(
//                                        DataSet(
//                                            System.currentTimeMillis(),
//                                            MySpannableStringBuilder("").apply {
//
//                                                append(renderedMarkdown)
//
//                                            },
//                                            false,
//                                            null,
//                                            false,
//                                            false
//                                        )
//                                    )
//
//                                    chatRecyclerView.post {
//
//                                        try {
//
//                                            chatAdapter.notifyItemChanged(index)
//
//                                        } catch (ex:Exception) {
//
//
//                                        }
//
//                                        chatAdapter.notifyItemInserted(dataSet.size-1)
//                                    }
//
//                                    if (isSpeech) {
//
//
//                                        TextToSpeechInstance.speak(renderedMarkdown.toString()!!
//                                            , dataSet.size - 1
//                                            , 0
//                                        )
//
//
//                                    }
//
//                                } else {
//
//                                    //stopIcon!!.isSelected = false
//
//
//                                }
//
//
//                            }
//                        }
//
//
//
//
//
////                        if (isFirst) {
////
////                            isFirst = false
////
////                            dataSet.get(index).spannableStringBuilder.clear()
////                            dataSet.get(index).spannableStringBuilder.append("")
////
////                            //chatAdapter.notifyItemChanged(index)
////
////                            it?.let {
////
////                                var sample:String?=null
////                                var renderedMarkdown:Spanned?=null
////
////
////                                if (it.isNotBlank()) {
////
////                                    evaluateIsMarkdown(it)
////
////                                    if (isMarkdownStarted) {
////
////                                        markdownStringBuilder.append(it+"\n")
////
////                                    } else {
////
////                                        if (markdownStringBuilder.length>0) {
////
////                                            markdownStringBuilder.append(it+"\n")
////                                            sample = markdownStringBuilder.toString()
////                                            //markdownStringBuilder.clear()
////
////
////                                        } else {
////
////                                            sample = it
////                                        }
////
////
////                                    }
////
////
////                                    if (!isMarkdownStarted) {
////
////                                        if (markdownStringBuilder.length>0) {
////
////                                            //MarkDown Code
////
////                                            markdownStringBuilder.clear()
////
////                                            val node = markWon!!.parse(
////                                                sample!!.trim().replace("[$]{2,}|\\$\\s|\n+".toRegex(), "")
////                                            )
////                                            renderedMarkdown = markWon!!.render(node)
////
////                                            dataSet.add(
////                                                DataSet(
////                                                    System.currentTimeMillis(),
////                                                    MySpannableStringBuilder("").apply {
////
////                                                        append(renderedMarkdown)
////
////                                                    },
////                                                    false,
////                                                    null,
////                                                    false,
////                                                    false
////                                                )
////                                            )
////
////
////                                            chatRecyclerView.post {
////
////                                                chatAdapter.notifyItemInserted(dataSet.size - 1)
////
////
////                                            }
////
////                                            if (isSpeech) {
////
////
////                                                TextToSpeechInstance.speak(
////                                                    renderedMarkdown.toString()!!, dataSet.size - 1, 0
////                                                )
////
////
////                                            }
////
////
////
////                                        } else {
////
////                                            //Normal
////
////                                            val node = markWon!!.parse(
////                                                sample!!.trim().replace("[$]{2,}|\\$\\s|\n+".toRegex(), "")
////                                            )
////                                            renderedMarkdown = markWon!!.render(node)
////
////                                            if (renderedMarkdown!!.toString().equals("Ended")) {
////
////                                                dataSet.get(index).spannableStringBuilder.clear()
////                                                dataSet.get(index).spannableStringBuilder.append("Ended")
////
////
////                                                dataSet.get(index).spannableStringBuilder.setSpan(
////                                                    ForegroundColorSpan(Color.RED),
////                                                    0,
////                                                    dataSet.get(dataSet.size - 1).spannableStringBuilder.length,
////                                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                                                )
////
////                                                chatAdapter.notifyItemChanged(index)
////
////                                                chatEditText.isSelected = false
////                                                chatEditText.setDefaultState()
////
////                                                isProcessing = false
////
////
////                                            } else {
////
////                                                dataSet.add(
////                                                    DataSet(
////                                                        System.currentTimeMillis(),
////                                                        MySpannableStringBuilder("").apply {
////
////                                                            append(renderedMarkdown)
////
////                                                        },
////                                                        false,
////                                                        null,
////                                                        false,
////                                                        false
////                                                    )
////                                                )
////
////                                                chatRecyclerView.post {
////
////                                                    chatAdapter.notifyItemInserted(dataSet.size - 1)
////
////
////                                                }
////
////                                                if (isSpeech) {
////
////
////                                                    TextToSpeechInstance.speak(
////                                                        renderedMarkdown.toString()!!, dataSet.size - 1, 0
////                                                    )
////
////
////                                                }
////                                            }
////
////
////                                        }
////
////
////                                    }
////
////                                }
////                            }
////
////
////                            //parseBytes(it)
////
////
////                        } else {
////
////                            //parseBytes(it)
////
////                            it?.let {
////
////                                if (!it.equals("Ended")) {
////
////                                    var sample: String? = null
////                                    var renderedMarkdown:Spanned?=null
////
////                                    if (it.isNotBlank()) {
////
////                                        var sample:String?=null
////
////                                        evaluateIsMarkdown(it)
////
////                                        if (isMarkdownStarted) {
////
////                                            markdownStringBuilder.append(it+"\n")
////
////                                        } else {
////
////                                            if (markdownStringBuilder.length>0) {
////
////                                                markdownStringBuilder.append(it+"\n")
////                                                sample = markdownStringBuilder.toString()
////                                                //markdownStringBuilder.clear()
////
////
////                                            } else {
////
////                                                sample = it
////                                            }
////
////
////                                        }
////
////                                        if (!isMarkdownStarted) {
////
////                                            if (markdownStringBuilder.length>0) {
////
////                                                //MarkDown Code
////
////                                                markdownStringBuilder.clear()
////
////                                                val node = markWon!!.parse(
////                                                    sample!!.trim().replace("[$]{2,}|\\$\\s|\n+".toRegex(), "")
////                                                )
////                                                renderedMarkdown = markWon!!.render(node)
////
////                                                dataSet.add(
////                                                    DataSet(
////                                                        System.currentTimeMillis(),
////                                                        MySpannableStringBuilder("").apply {
////
////                                                            append(renderedMarkdown)
////
////                                                        },
////                                                        false,
////                                                        null,
////                                                        false,
////                                                        false
////                                                    )
////                                                )
////
////
////                                                chatRecyclerView.post {
////
////                                                    chatAdapter.notifyItemInserted(dataSet.size - 1)
////
////
////                                                }
////
////                                                if (isSpeech) {
////
////
////                                                    TextToSpeechInstance.speak(
////                                                        renderedMarkdown.toString()!!, dataSet.size - 1, 0
////                                                    )
////
////
////                                                }
////
////
////
////                                            } else {
////
////                                                //Normal
////
////                                                val node = markWon!!.parse(
////                                                    sample!!.trim().replace("[$]{2,}|\\$\\s|\n+".toRegex(), "")
////                                                )
////                                                renderedMarkdown = markWon!!.render(node)
////
////                                                dataSet.add(
////                                                    DataSet(
////                                                        System.currentTimeMillis(),
////                                                        MySpannableStringBuilder("").apply {
////
////                                                            append(renderedMarkdown)
////
////                                                        },
////                                                        false,
////                                                        null,
////                                                        false,
////                                                        false
////                                                    )
////                                                )
////
////                                                chatRecyclerView.post {
////
////                                                    chatAdapter.notifyItemInserted(dataSet.size - 1)
////
////
////                                                }
////
////                                                if (isSpeech) {
////
////                                                    TextToSpeechInstance.speak(
////                                                        renderedMarkdown.toString()!!, dataSet.size - 1, 0
////                                                    )
////                                                }
////
////                                                }
////                                            }
////                                        }
////
////
////                                    } else {
////
////                                        //end here
////
////
//////                                    if (httpClient!=null) {
//////
//////                                        if (httpClient?.isActive!!) {
//////
//////                                            httpClient!!.cancel()
//////                                            httpClient = null
//////
//////                                        } else {
//////
//////                                            httpClient = null
//////
//////                                        }
//////
//////                                    }
////
////
////
////                                }
////                            }
////
////                    }
//                    })
//
//            } catch (ex:ConnectException) {
//
//                Log.d(ChatActivity::class.java.name, "ConnectExp: " + ex.message)
//
//                if (isAnim) {
//
//                    dataSet.get(index).spannableStringBuilder.clear()
//                    dataSet.get(index).spannableStringBuilder.append("Server Error : ${ex.message}")
//                    dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                        ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    isAnim = false
//                    chatEditText.isSelected = false
//
//                    stopIcon!!.isSelected = false
//
//
//                    chatRecyclerView.post {
//
//                        chatAdapter.notifyItemChanged(index)
//                    }
//
//                    chatEditText.isSelected = false
//                    chatEditText.setDefaultState()
//
//
//                } else {
//
//                    chatEditText.isSelected = false
//                    chatEditText.setDefaultState()
//
//
//                }
//
//
//            }catch (ex:Exception) {
//
//                ex.printStackTrace()
//
//                if (dataSet.get(index).spannableStringBuilder.length == 0) {
//
//                    dataSet.get(index).spannableStringBuilder.clear()
//                    dataSet.get(index).spannableStringBuilder.append("Server Error : ${ex.message}ttt")
//                    dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                        ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    isAnim = false
//
//                    stopIcon!!.isSelected = false
//
//
//                    chatRecyclerView.post {
//
//                        chatAdapter.notifyItemChanged(index)
//                    }
//
//                    chatEditText.isSelected = false
//                    chatEditText.setDefaultState()
//
//
//                }
//
//            }
//
//        }
//
//
//    }
//
//    var isMarkdownStarted = false
//
//    private fun evaluateIsMarkdown(it: String) {
//
//        if (it.length>=3) {
//
//            if (it.substring(0,3).equals(MARKDOWN_CODE)) {
//
//                var containIndex = it.indexOf(MARKDOWN_CODE,3)
//
//                if (containIndex!=-1) {
//
//                    if (containIndex<=it.length-1) {
//
//                        isMarkdownStarted = false
//                        return
//                    }
//                }
//
//                isMarkdownStarted = true
//
//            } else if (it.substring(it.length-MARKDOWN_CODE.length).equals(MARKDOWN_CODE)) {
//
//                isMarkdownStarted = false
//            }
//
//
//        } else {
//
//            isMarkdownStarted = false
//        }
//
//
//
//    }
//
////    var markMap:HashMap<String,Boolean> = HashMap()
////
////    private fun checkForMarkdown(it:String) {
////
////        if (it.substring(0,3).equals(MARKDOWN_CODE)) {
////
////            isMarkDownCode = true
////
////        } else if (it.substring(it.length-MARKDOWN_CODE.length,it.length).equals(MARKDOWN_CODE)) {
////
////            isMarkDownCode = false
////        }
////
////        if (isMarkDownCode) {
////
////            sample = MARKDOWN_CODE+it;
////
////        } else {
////
////            sample = it+MARKDOWN_CODE
////        }
////
////    }
//
//
//    private fun initAnimator(textView: TextView,isStart:Boolean) {
//
//        if (isAnim) {
//
//            textAnimator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f).apply {
//
//                duration = 700
//                repeatCount = ObjectAnimator.INFINITE
//
//                addUpdateListener {
//
//                    textView.alpha = it.animatedValue as Float
//                }
//
//
//            }
//
//            textAnimator?.addListener(object : Animator.AnimatorListener {
//                override fun onAnimationStart(animation: Animator) {
//
//                }
//
//                override fun onAnimationEnd(animation: Animator) {
//
//                    textView.alpha = 1f
//                    textAnimator = null
//
//                }
//
//                override fun onAnimationCancel(animation: Animator) {
//
//                }
//
//                override fun onAnimationRepeat(animation: Animator) {
//
//                }
//
//
//            })
//
//            textAnimator?.start()
//
//        } else {
//
//            textAnimator?.cancel()
//
//        }
//
//    }
//
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
//
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 100) {
//            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                if (SpeechRecognizer.isRecognitionAvailable(this)) {
//
//                    startSpeechRecognizer()
//
//                } else {
//
//                    if (!checkGoogleAppAvailability()) {
//
//                        installGoogleApp()
//                    }
//
//
//                }
//
//            } else {
//                Toast.makeText(
//                    this,
//                    "Microphone permission is required for speech recognition",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//
//    private fun checkGoogleAppAvailability():Boolean {
//
//        return try {
//
//            this.packageManager.getPackageInfo("com.google.android.googlequicksearchbox",0)
//            true
//
//        } catch (ex:PackageManager.NameNotFoundException) {
//
//            false
//        }
//    }
//
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
//        } catch (ex:ActivityNotFoundException) {
//
//            val webIntent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox")
//            }
//            startActivity(webIntent)
//        }
//    }
//
//    private fun showAlertPrompt(title:String,message:String,iconId:Int) {
//
//        AlertDialog.Builder(this)
//            .setIcon(iconId)
//            .setTitle(title)
//            .setCancelable(false)
//            .setMessage(message)
//            .setPositiveButton("Ok",{ dialogInterface, i ->
//
//                dialogInterface.dismiss()
//
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                    data = Uri.parse("package:com.google.android.googlequicksearchbox")
//                }
//                startActivity(intent)
//
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
//
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
//
//    var listenDialog:Dialog?=null
//
//    private fun buildCustomListeningDialog() {
//
//        val dialogBox = CustomDialogBox()
//
//        dialogBox.buildDialog(this,R.layout.say_something_dialog)
//        dialogBox.setSize((Utils.getScreenWidth(this)*0.40).toInt(),(Utils.getScreenWidth(this)*0.50).toInt())
//
//        listenDialog = dialogBox.createDialog()
//        listenDialog!!.setCancelable(false)
//
//        listenDialog!!.window!!.apply {
//
//            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            enterFullScreenMode(this)
//
//        }
//
//        var stopIcon:ImageView = listenDialog!!.findViewById(R.id.mic_stop)
//
//        stopIcon.setOnClickListener {
//
//            listenDialog!!.dismiss()
//            this.stopIcon!!.isSelected = false
//            SpeechRecognizerInstance.destroyInstance()
//
//
//        }
//    }
//
//    private fun hideKeyBoard() {
//
//        val imm:InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//
//        imm.hideSoftInputFromWindow(mainLayout?.windowToken,0)
//
//    }
//
//    var isProcessing:Boolean = false
//
//    override fun onDestroy() {
//        super.onDestroy()
//        TextToSpeechInstance.destroyInstance()
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//    }
//
//    override fun onDataReceived(data: String) {
//
//        Log.d(TAG, "onDataReceived: ${data}")
//
//        runOnUiThread {
//
//            chatEditText.setText(data)
//            chatEditText.requestFocus()
//            chatEditText.focusable
//            chatEditText.isFocusableInTouchMode = true
//            chatEditText.setSelection(data.length)
//
////            processData1(data)
//
//        }
//
//
//    }
//
//    override fun onRMSReceived(value: Double) {
//        TODO("Not yet implemented")
//    }
//
////    override fun onPartialResult(hypothesis: String?) {
////
////        if (!isProcessing) {
////
////            hypothesis?.let {
////
////                if (hypothesis.lowercase().contains("hello robot")||hypothesis.contains("hello robo")
////                    || hypothesis.lowercase().contains("hello robert")) {
////
////                    isProcessing = true
////
////                    if (SpeechRecognizer.isRecognitionAvailable(this@ChatActivity2)) {
////
////                        checkAudioPermissionAndStart()
////
////
////                    } else {
////
////                        chatEditText.isSelected = false
////
////                        showAlertPromptForGoogleApp()
////                    }
////
////                }
////
////            }
////
////
////        }
////
////        Log.d(TAG, "onPartialResult: " + hypothesis)
////
////
////
////    }
//
////    override fun onResult(hypothesis: String?)
////
////    }
////
////    override fun onFinalResult(hypothesis: String?) {
////
////    }
////
////    override fun onError(exception: java.lang.Exception?) {
////
////
////    }
////
////    override fun onTimeout() {
////
////    }
//
//
//}