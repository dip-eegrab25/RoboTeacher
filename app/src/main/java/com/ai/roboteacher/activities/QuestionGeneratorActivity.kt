package com.ai.roboteacher.activities

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
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai.roboteacher.ChatEditTextView
import com.ai.roboteacher.DataSet
import com.ai.roboteacher.ModelDownloadTask

import com.ai.roboteacher.Models.CurriculumResponse2
import com.ai.roboteacher.MySpannableStringBuilder
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.QuestionDataReceiver
import com.ai.roboteacher.R
import com.ai.roboteacher.SpeechRecognizerInstance
import com.ai.roboteacher.StatusService
import com.ai.roboteacher.TextToSpeechInstance
import com.ai.roboteacher.Utils
import com.ai.roboteacher.services.QuestionGeneratorService

import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

import io.ktor.client.HttpClient
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

class QuestionGeneratorActivity:AppCompatActivity()
, ModelDownloadTask.ModelDownloadListener{

    private val TAG = QuestionGeneratorActivity::class.java.name


    var quesNo = 0
    var pdfMap = mutableMapOf<Int,java.util.ArrayList<String?>?>()




    var ansDataList:ArrayList<MySpannableStringBuilder?>? = ArrayList()

    var onGoingIndex = -1

    var headerArr:Array<String> = Array(5,{

        ""
    })

    var subHeaderArr:Array<String> = Array(5,{

        ""
    })


    var isClientClosed:Boolean = false

    lateinit var chatEditText: ChatEditTextView


    var dataSet: ArrayList<DataSet> = ArrayList<DataSet>()

    var index: Int = -1
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var textAnimator: ObjectAnimator? = null


    private var isSpeech: Boolean = false
    private var dropDownArrow:ImageView?=null


    val baseMargin = 5
    private var httpClient: HttpClient?=null
    var markWon:Markwon?=null
    //private var isStopped:AtomicBoolean = AtomicBoolean(false)
    private var mainLayout:ConstraintLayout?=null

    private var constraintSet:ConstraintSet?= ConstraintSet()
    private var countDownProgress:ProgressBar?=null
    private var countDownLayout:View?=null


    private var bundle:Bundle?=null
    private var classId = 0
    private var subject:String?=null

    private var drawerLayout:DrawerLayout?=null
    private var drawerNav:NavigationView?=null
    private var navMenuToggle:ImageView?=null

    private var unitChapterView:TextView?=null
    private var unitList:java.util.HashSet<Int> = HashSet()
    private var chapterList:java.util.HashSet<Int> = HashSet()
    private var listView:ListView?=null
    private var radioGroup:RadioGroup?=null
    private var subId = 0

    private var selectedList:java.util.ArrayList<Int> = java.util.ArrayList()

    private var speechRunnable:Runnable?=null
    private var speechHandler = Handler()
    private var txtSubject:TextView?=null
    private var txtClass:TextView?=null
    private var statusServiceConnection:StatusServiceConnection?=null
    private var qDataReceiver:QuestionDataReceiver?=null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode(window)
        setContentView(R.layout.activity_question_generator)



      //  DBConfig.getInstance(this)

        statusServiceConnection = StatusServiceConnection()

//        WhisperInstance.getInstance(this)
//        WhisperInstance.setListener(this)

        speechRunnable = object :java.lang.Runnable{
            override fun run() {

                SpeechRecognizerInstance.destroyInstance()



                    if (!chatEditText!!.text.toString().isEmpty()) {

                        //valueAnim!!.resume()

                        if (status.equals("up")) {

                            if (!selectedList.isEmpty()) {

                                isServiceAlert = false

                                nStrBuilder.clear()

                                startQuestionService(chatEditText.text.toString())

                                chatEditText!!.setText("")


                            } else {

                                chatEditText.isSelected = false
                                chatEditText.setDefaultState()
                                drawerLayout!!.openDrawer(Gravity.RIGHT)
                                Toast.makeText(this@QuestionGeneratorActivity,"Please select chapter/unit",Toast.LENGTH_SHORT).show()
                            }



                        } else {

                            chatEditText.isSelected = false
                            chatEditText.setDefaultState()

                            Toast.makeText(this@QuestionGeneratorActivity,"Service not available.Please try again.",Toast.LENGTH_SHORT).show()
                        }


                    } else {

                        chatEditText.isSelected = false
                        chatEditText.setDefaultState()
                    }

                    Log.d(TAG, "Speech Sent")





            }

        }

        bundle = intent.extras!!
        classId = bundle!!.getString("class")!!.toInt()
        subject = bundle!!.getString("subject")
        subId = bundle!!.getString("subId")!!.toInt()

        Log.d(TAG, "ClassId: "+classId)
        Log.d(TAG, "SubId: "+subId)




        headerArr[0] = "Welcome to your %s class with %s."
        headerArr[1] = "You're all set for %s Literature with %s."
        headerArr[2] = "Your %s session with %s is ready."
        headerArr[3] = "Time to start %s with %s."
        headerArr[4] = "Get ready to explore %s today with %s."

        subHeaderArr[0] = "Let's begin your learning journey."
        subHeaderArr[1] = "Let’s dive in!"
        subHeaderArr[2] = " Let’s make it count!"
        subHeaderArr[3] = "You’ve got this!"

        drawerLayout = findViewById(R.id.drawer)
        drawerNav = findViewById(R.id.drawer_nav)
        navMenuToggle = findViewById(R.id.yellow_panda_menu)
        unitChapterView = drawerNav!!.findViewById(R.id.unit_chapter_view)
        txtSubject = drawerNav!!.findViewById(R.id.txt_subject)
        txtClass = drawerNav!!.findViewById(R.id.txt_class)
        listView = drawerNav!!.findViewById(R.id.picker_list)
        radioGroup = drawerNav!!.findViewById(R.id.radio_group)

        txtClass!!.text = classId.toString()
        txtSubject!!.text = subject

        radioGroup!!.setOnCheckedChangeListener(object:RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

                if ((group!!.getChildAt(0) as RadioButton).isChecked) {

                    var adapter = ListViewAdapter(this@QuestionGeneratorActivity,java.util.ArrayList(unitList))

                    listView!!.adapter = adapter


                } else {

                    var adapter = ListViewAdapter(this@QuestionGeneratorActivity,java.util.ArrayList(chapterList))

                    listView!!.adapter = adapter

                }
            }


        })

        navMenuToggle!!.setOnClickListener {

            finish()

//            if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
//
//                drawerLayout!!.closeDrawer(GravityCompat.START)
//
//            } else {
//
//                drawerLayout!!.openDrawer(GravityCompat.START)
//            }
        }





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

//        val gestureDetector:GestureDetector = GestureDetector(this,SwipeGestureListener({direction, velocityX, velocityY ->
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
//                }
//
//
//            }
//        }))


        getData(OkHttpClientInstance.getTopicUrl+"?class=${classId}&subject=${subject}")

//        countDownLayout = findViewById(R.id.count_down_layout)
//        countDownProgress = countDownLayout!!.findViewById(R.id.progressRing)
//        progressText = countDownLayout!!.findViewById(R.id.progress_text)



//        intent?.let {
//
//            isFlow = bundle!!.getBoolean("isFlow")
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
//                customAlertDialogBuilder.buildAlertDialog(this@ChatActivity
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
//                        var date:Date? = sdf.parse("${bundle!!.getString("date")}T${bundle!!.getString("end_time")}")
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





        markWon = Markwon.builder(this)
            .usePlugin(MarkwonInlineParserPlugin.create())
//            .usePlugin(ImagesPlugin.create(ImagesPlugin.))
            .usePlugin(TablePlugin.create(this)) // <-- important
            .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
            .usePlugin((JLatexMathPlugin.create(16f,object: JLatexMathPlugin.BuilderConfigure{
                override fun configureBuilder(builder: JLatexMathPlugin.Builder) {

                    builder.inlinesEnabled(true)

                    builder.blocksLegacy(true);

                    builder.blocksEnabled(true);
                }


            })))
            .build()



        TextToSpeechInstance.getInstance(applicationContext)
        TextToSpeechInstance.setProgressListener(object :
            UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {


                val indexes = utteranceId!!.split(":")
                onGoingIndex = indexes[0].toInt()

                chatEditText.isSelected = true

                runOnUiThread {

                    chatRecyclerView.post {

                        dataSet.get(indexes[0].toInt()).spannableStringBuilder
                            .setSpan(ForegroundColorSpan(ContextCompat.getColor(
                                this@QuestionGeneratorActivity,R.color.yellow_panda_logo_color))
                                ,0,dataSet.get(indexes[0].toInt()).spannableStringBuilder.length
                                ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        chatAdapter.notifyItemChanged(indexes[0].toInt())

                        chatRecyclerView.scrollToPosition(indexes[0].toInt())
                    }





//                                                chatRecyclerView.post {
//
//                                                    val lArr = intArrayOf(0,0)
//
//
//
//                                                    (chatRecyclerView.layoutManager!!.getChildAt(index))
//                                                        ?.findViewById<RecyclerView>(R.id.recycler_answers)
//                                                        ?.getLocationInWindow(lArr)
//
//                                                    Log.d(ChatActivity::class.java.name, "PosY: " + lArr[1])
//
//                                                    if (indexes[0].toInt() == 0) {
//
//                                                        chatRecyclerView.layoutManager?.getChildAt(index)
//                                                            ?.findViewById<RecyclerView>(R.id.recycler_answers)
//                                                            ?.scrollBy(0,360)
//                                                    }
//
////                                                        val recyclerLocation = IntArray(2)
////                                                        chatRecyclerView.getLocationInWindow(
////                                                            recyclerLocation
////                                                        )
////                                                        val recyclerY = recyclerLocation[1]
////                                                        val recyclerCenterY =
////                                                            (recyclerY + (chatRecyclerView.height)) / 2
////
////                                                        if (lArr[1] > recyclerCenterY) {
////
////                                                            var diff: Int =
////                                                                lArr[1] - recyclerCenterY
////
////                                                            Log.d(
////                                                                ChatActivity::class.java.name,
////                                                                "Diff" + diff
////                                                            )
////
////                                                                chatRecyclerView.layoutManager?.getChildAt(index)
////                                                                    ?.findViewById<RecyclerView>(R.id.recycler_answers)
////                                                                    ?.scrollBy(0,diff+200)
////
////                                                                //chatRecyclerView.scrollBy(0, diff)
////
////                                                        }
////
////                                                        Log.d(
////                                                            ChatActivity::class.java.name,
////                                                            "Location On Screen" + chatRecyclerView.height
////                                                        )
//                                                }


                }






//                                                if (indexes[0].toInt() > 0) {
//
//
//
//
//
////                                        dataSet.get(index)!!.setSpan(StyleSpan(Typeface.NORMAL),
////                                            0
////                                            ,dataSet.get(index).length
////                                        ,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
////
////                                        dataSet.get(index)!!.setSpan(ForegroundColorSpan(Color.WHITE),
////                                            0
////                                            ,dataSet.get(index)!!.length
////                                            ,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        StyleSpan(Typeface.NORMAL),
//                                                        0,
//                                                        dataSet.get(index).spannableStringBuilder!!.length,
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
//                                                    )
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        ForegroundColorSpan(Color.WHITE),
//                                                        0,
//                                                        dataSet.get(index).spannableStringBuilder!!.length,
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
//                                                    )
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        ForegroundColorSpan(Color.YELLOW),
//                                                        indexes[0].toInt(),
//                                                        indexes[1].toInt(),
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        StyleSpan(Typeface.BOLD),
//                                                        indexes[0].toInt(),
//                                                        indexes[1].toInt(),
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//
//
////
//                                                } else {
//
//
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        StyleSpan(Typeface.NORMAL),
//                                                        0,
//                                                        dataSet.get(index).spannableStringBuilder!!.length,
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
//                                                    )
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        ForegroundColorSpan(Color.WHITE),
//                                                        0,
//                                                        dataSet.get(index).spannableStringBuilder!!.length,
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
//                                                    )
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        ForegroundColorSpan(Color.YELLOW),
//                                                        indexes[0].toInt(),
//                                                        indexes[1].toInt(),
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                                                    dataSet.get(index).spannableStringBuilder!!.setSpan(
//                                                        StyleSpan(Typeface.BOLD),
//                                                        indexes[0].toInt(),
//                                                        indexes[1].toInt(),
//                                                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//
//                                                }
//
//
//                                                chatRecyclerView.post {
//                                                    chatAdapter.notifyItemChanged(index)
//                                                }



            }

            override fun onDone(utteranceId: String?) {


                val indexes = utteranceId!!.split(":")

                chatRecyclerView.post {

                    dataSet.get(indexes[0].toInt()).spannableStringBuilder
                        .setSpan(ForegroundColorSpan(ContextCompat.getColor(
                            this@QuestionGeneratorActivity,R.color.white))
                            ,0,dataSet.get(indexes[0].toInt()).spannableStringBuilder.length
                            ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    chatAdapter.notifyItemChanged(indexes[0].toInt())

                    //chatRecyclerView.scrollToPosition(indexes[0].toInt())
                }

                if (indexes[0].toInt() == dataSet.size-1 && isClientClosed) {

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


        this@QuestionGeneratorActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)




        mainLayout = findViewById(R.id.main)
        dropDownArrow = findViewById(R.id.dropdown_arrow)


        dropDownArrow = findViewById(R.id.dropdown_arrow)

        mainLayout?.setOnClickListener {

            hideKeyBoard()

        }


        dropDownArrow!!.setOnClickListener {

            if (drawerLayout!!.isDrawerOpen(GravityCompat.END)) {

                drawerLayout!!.closeDrawer(GravityCompat.END)

            } else {

                drawerLayout!!.openDrawer(GravityCompat.END)
            }
        }

//        countDownLayout = findViewById(R.id.count_down_layout)
//        countDownProgress = countDownLayout!!.findViewById(R.id.progressRing)
//        progressText = countDownLayout!!.findViewById(R.id.progress_text)
//
//        if (isFlow) {
//
//            headerLabel!!.setText(String.format(headerArr.get(Random.nextInt(headerArr.size))
//                ,bundle!!.getString("subject_name")
//                ,bundle!!.getString("teacher_name")))
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

        constraintSet!!.clone(mainLayout)



//        dropDownArrow!!.setOnClickListener {
//
//            if (headerLabel!!.visibility == View.VISIBLE
//                && subHeaderLabel!!.visibility == View.VISIBLE) {
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
//
//
//            } else {
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
//                if (onGoingIndex!=-1) {
//
//                    chatRecyclerView.scrollToPosition(onGoingIndex)
//                }
//
//            }
//
//
//        }



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


        chatEditText.setOnDrawableClickListener(object : ChatEditTextView.OnDrawableClickListener {
            override fun onClick(position: ChatEditTextView.DrawablePosition) {

                when(position) {

                    ChatEditTextView.DrawablePosition.RIGHT->{

                        hideKeyBoard()


                        if (chatEditText.isSelected) {

                            //isStopped.set(false)

//                            isStopped = false


                                if(!chatEditText.text.toString().isBlank()) {

                                    if (status.equals("up")) {

                                        startQuestionService(chatEditText.text.toString())


                                    } else {

                                        chatEditText.isSelected = false
                                        chatEditText.setDefaultState()

                                        Toast.makeText(this@QuestionGeneratorActivity,"Service not available",Toast.LENGTH_SHORT).show()
                                    }


                                } else {

                                    Toast.makeText(this@QuestionGeneratorActivity
                                        ,"Enter valid question"
                                        ,Toast.LENGTH_SHORT).show()

                                    chatEditText.isSelected = false
                                    chatEditText.setDefaultState()

                                }






                        } else {

                            qDataReceiver?.isStopped?.set(true)

                            QuestionDataReceiver.isRunning.set(false)

                            qDataReceiver = null

//                            qDataReceiver?.isStopped = true


                            nStrBuilder.clear()
                            chatEditText.setText("")

                            //isStopped.set(true)

//                            isStopped = true

                            //Toast.makeText(this@QuestionGeneratorActivity,"Clicked Right",Toast.LENGTH_SHORT).show()

                            TextToSpeechInstance.destroyInstance()

                            if (dataSet.get(index).isAnim) {


                                dataSet.get(index).isAnim = false

                                dataSet.get(index).spannableStringBuilder.clear()
                                dataSet.get(index).spannableStringBuilder.append("Stopped by user")
                                dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
                                    ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

                                dataSet.get(index).isError = true

                                chatRecyclerView.post {

                                    chatAdapter.notifyItemChanged(index)
                                }
                            } else {

                                if (onGoingIndex!=-1) {

                                    chatRecyclerView.post {

                                        dataSet.get(onGoingIndex).spannableStringBuilder
                                            .setSpan(ForegroundColorSpan(ContextCompat.getColor(
                                                this@QuestionGeneratorActivity,R.color.white))
                                                ,0,dataSet.get(onGoingIndex).spannableStringBuilder.length
                                                ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                                        chatAdapter.notifyItemChanged(onGoingIndex)

                                        chatRecyclerView.scrollToPosition(onGoingIndex)
                                    }

                                }

                            }

//                            if (bound) {
//                                unbindService(serviceConnection)
//                                stopService(serviceIntent)
//                                bound = false
//                            }

                        }
                    }

                    ChatEditTextView.DrawablePosition.LEFT->{

//                        if (checkAudioPermissionAndStart()) {

                            if (chatEditText.isSelected) {

                               // isStopped.set(false)

                                //startRecording()

                                startSpeechRecognizer()

//                                isStopped = false

                                //checkAudioPermissionAndStart()



                            } else {

                                //isStopped.set(true)

//                            isStopped = true

                                //stopRecording()

                                speechHandler.removeCallbacks(speechRunnable!!)

                                SpeechRecognizerInstance.destroyInstance()

                                TextToSpeechInstance.destroyInstance()

                                qDataReceiver?.isStopped?.set(true)

                                QuestionDataReceiver.isRunning.set(false)

                                qDataReceiver = null


//                            dataThread?.let {
//
//                                dataThread!!.isStopped = true
//                            }


                                Log.d(TAG, "HttpEnded")




                                if (dataSet.get(index).isAnim) {

                                    dataSet.get(index).isAnim = false

                                    dataSet.get(index).spannableStringBuilder.clear()
                                    dataSet.get(index).spannableStringBuilder.append("Stopped by user")
                                    dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
                                        ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

                                    chatRecyclerView.post {

                                        chatAdapter.notifyItemChanged(index)
                                    }
                                } else {

                                    if (onGoingIndex != -1) {

                                        chatRecyclerView.post {

                                            dataSet.get(onGoingIndex).spannableStringBuilder
                                                .setSpan(
                                                    ForegroundColorSpan(
                                                        ContextCompat.getColor(
                                                            this@QuestionGeneratorActivity, R.color.white
                                                        )
                                                    ),
                                                    0,
                                                    dataSet.get(onGoingIndex).spannableStringBuilder.length,
                                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )

                                            chatAdapter.notifyItemChanged(onGoingIndex)

                                            chatRecyclerView.scrollToPosition(onGoingIndex)
                                        }


                                    }
                                }

//                            if (bound) {
//                                unbindService(serviceConnection)
//                                stopService(serviceIntent)
//                                bound = false
//                            }

                            }


                      //  }



                    }

                    ChatEditTextView.DrawablePosition.OTHER->{

                        chatEditText.requestFocus()

                        chatEditText.setFocusable(true)
                        chatEditText.isFocusableInTouchMode = true

                        showKeyboardFocus(true)
                    }
                }




            }


        })


        chatRecyclerView = findViewById(R.id.chat_recycler)
        chatRecyclerView.layoutManager = LinearLayoutManager(this@QuestionGeneratorActivity)

        chatAdapter = ChatAdapter()

        chatRecyclerView.adapter = chatAdapter

    }

    var statusIntent:Intent?=null

    override fun onResume() {
        super.onResume()

        status = ""

        statusIntent = Intent(this, StatusService::class.java)
        startService(statusIntent)

        bindService(statusIntent!!,statusServiceConnection!!,Context.BIND_AUTO_CREATE)



    }


    private fun showKeyboardFocus(visible:Boolean) {

        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (visible) {

            imm.showSoftInput(chatEditText,InputMethodManager.SHOW_IMPLICIT)
            //imm = null

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





//    private suspend fun parseBytes(byteArray: ByteArray) {
//
//
//        var i = 0
//        var codePoint = 0
//        var numBytes = 0
//        //var sb:StringBuilder = StringBuilder()
//
//        while (i < byteArray.size) {
//
//            var unsignedFirst = byteArray[i].toInt() and (0xFF)
//
//            if (unsignedFirst shr (7) == 0b0) {
//
//                codePoint = unsignedFirst
//                numBytes = 1
//                i += numBytes
//
//            } else if (unsignedFirst shr (5) == 0b110) {
//
//                var second = byteArray[i + 1].toInt() and (0x3F)
//
//                codePoint = ((unsignedFirst and (0x1F)) shl (6)) or (second)
//                numBytes = 2
//                i += numBytes
//
//            } else if (unsignedFirst shr (4) == 0b1110) {
//
//                var second = byteArray[i + 1].toInt() and (0x3F)
//                var third = byteArray[i + 2].toInt() and (0x3F)
//
//                codePoint = ((unsignedFirst and (0x0F)) shl (12)) or
//                        (second shl (6)) or
//                        third
//
//
//                numBytes = 3
//                i += numBytes
//
//            } else if (unsignedFirst shr (3) == 0b11110) {
//
//                var second = byteArray[i + 1].toInt() and (0x3F)
//                var third = byteArray[i + 2].toInt() and (0x3F)
//                var fourth = byteArray[i + 3].toInt() and (0x3F)
//
//                codePoint = ((unsignedFirst and (0x07)) shl (18)) or
//                        (second shl (12)) or
//                        (third shl (6)) or
//                        fourth
//
//
//                numBytes = 4
//                i += numBytes
//
//
//            } else {
//
//                throw IOException("InValid Encoding")
//            }
//
//            val s: String = String(Character.toChars(codePoint))
//            //s.intern()
//
////            strBuilder!!.append(s)
////            strBuilder!!.setSpan(ForegroundColorSpan(Color.WHITE),0
////                ,strBuilder!!.length
////                ,SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//
////            (s.equals("\n")||s.equals(".")) &&
//
//            if (word.length > 0) {
//
//
//                //word+=String(Character.toChars(codePoint))
//                //strBuilder!!.append(word)
//                word.appendCodePoint(codePoint)
//                strBuilder!!.append(s)
//                startIndex = strBuilder!!.indexOf(word.toString(), startIndex)
//                Log.d(ChatActivity::class.java.name, "index: " + startIndex)
//                //startIndex = strBuilder!!.lastIndexOf(word.toString(), ignoreCase = true)
//
//
//                //TextToSpeechInstance.speak(strBuilder!!,startIndex, strBuilder!!.length)
//
//                Log.d(ChatActivity::class.java.name, "parseBytes: " + word)
//
//
//            } else  {
//
//                word.appendCodePoint(codePoint)
//                strBuilder!!.append(word)
//
////                var p = Pattern.compile("[a-zA-Z0-9]+")
////                var m = p.matcher(s)
////
////                if (m.matches()) {
////
////                    word.appendCodePoint(codePoint)
////                    strBuilder!!.append(s)
////
////
////                }
//
//
//            }
//
//            if (!s.isBlank()) {
//
//                if (s.equals("\n") || s.equals(".")) {
//
//                    //strBuilder!!.append(s)
//
//                    TextToSpeechInstance.speak(word.toString(), startIndex, startIndex + word.length)
//
//                    //delay(1000)
//                    //TextToSpeechInstance.speak(strBuilder!!,startIndex,startIndex+word.length)
//
//                    word.clear()
//
//
//                }
//
//
//            }
//
//
//
//
//
//
//            //var char:String = String(Character.toChars(codePoint))
//
//            //strBuilder!!.appendCodePoint(codePoint)
//
//
////            if (!p.matcher(char).matches()) {
////
////                strBuilder.lastIndexOf()
////
////                startIndex++
////                //strBuilder!!.appendCodePoint(codePoint)
////
////
////            } else {
////
////                //strBuilder!!.appendCodePoint(codePoint)
////
////                //tts.speak(strBuilder)
////
////                //TextToSpeechInstance.speak(strBuilder!!,startIndex,strBuilder!!.length)
////
////
////
////
////            }
//
//
//            dataSet.set(index, strBuilder!!)
//            chatRecyclerView.post {
//                chatAdapter.notifyItemChanged(index)
//            }
//            //chatAdapter.notifyItemChanged(index)
//
////            chatRecyclerView.post {
////
////                //chatRecyclerView.scrollToPosition(index+1)
////            }
//
//            //chatRecyclerView.scrollToPosition(index)
//
//
////            runOnUiThread {
////
////                textView?.setText(sb)
////
////            }
//
////            delay(50)
//
//        }
//
//        //Log.d(MainActivity::class.java.name, "parseBytes: " + sb.toString())
//
//
//    }






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


            if (dataSet.get(position).isQuestion) {

                holder.logo.visibility = View.VISIBLE

                holder.logo.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@QuestionGeneratorActivity,
                        R.drawable.panda
                    )
                )

                holder.textView.text = dataSet[position].spannableStringBuilder


            } else if (!dataSet.get(position).isQuestion) {

                if (dataSet.get(position).isImage) {

                    holder.logo.visibility = View.VISIBLE

                    holder.logo.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@QuestionGeneratorActivity,
                            R.drawable.panda_1
                        )
                    )


                } else {

                    holder.logo.visibility = View.GONE

                }

                if (dataSet.get(position).isError) {

                    holder.textView.setText(dataSet.get(position).spannableStringBuilder,TextView.BufferType.SPANNABLE)
                }





                if (dataSet.get(position).spannableStringBuilder.toString().trim().startsWith("Download Pdf")) {

                    holder.textView.setText(dataSet[position].spannableStringBuilder.toString().trim())

                    holder.textView.setTextColor(Color.BLUE)

                } else {

                    markWon!!.setMarkdown(holder.textView,dataSet[position].spannableStringBuilder.toString())

                }

            }

            holder.textView.setOnClickListener {

                if (holder.textView.text.trim().startsWith("Download Pdf")) {

                    var file = Utils.generatePDF1(this@QuestionGeneratorActivity
                        ,pdfMap.get(dataSet.get(position).quesNo))

                    var intent = Intent(this@QuestionGeneratorActivity,PdfActivity::class.java)
                    intent.putExtra("pdfFile",file)
                    startActivity(intent)

                }

            }

            if (dataSet.get(index).isAnim) {

                if (position == index) {

                    initAnimator(holder.textView, dataSet.get(index).isAnim)
                }
            } else {

                textAnimator?.let {

                    initAnimator(holder.textView, dataSet.get(index).isAnim)


                }


            }




        }


        override fun getItemId(position: Int): Long {

//            return super.getItemId(position)

            return dataSet.get(position).id
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

            var textView: TextView = v.findViewById(R.id.text_query)
            var logo: ImageView = v.findViewById(R.id.text_logo)

        }

    }

    private inner class AnswersAdapter : RecyclerView.Adapter<AnswersAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.single_item_answers, parent, false)
            )


        }

        override fun getItemCount(): Int {

            return dataSet.get(index).ansList?.size!!

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {


            holder.textView.text = dataSet[index].ansList?.get(position)

        }

//        override fun getItemViewType(position: Int): Int {
//
//            if ((position+1)%2 == 0) {
//
//                return 2
//
//
//            } else {
//
//                return 1
//            }
//
//        }

        override fun getItemId(position: Int): Long {

            return position.toLong()
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

            var textView: TextView = v.findViewById(R.id.answer_text)


        }

    }

    //    private suspend fun getDataKtorr(
//        query: String? = "Please recite the poem 'What a bird thought'.",
//        cls: Int = 6,
//        subject: String? = "english"
//    ): Flow<String?> = flow {
//
//        Log.d(TAG, "getDataKtorr: ${query} ${cls} ${subject}")
//
//        Log.d(TAG, "Selected: "+selectedList)
//
//
//        val json = JSONObject().apply {
//            put("query", query)
//            put("class_number", cls)
//            put("subject_name", subject)
//
//            if ((radioGroup!!.getChildAt(0) as RadioButton).isChecked) {
//
//                put("segmentation_type",(radioGroup!!.getChildAt(0) as RadioButton).text.toString().toLowerCase())
//                put("unit_num",JSONArray(selectedList))
//                put("chapter_num",JSONArray(ArrayList<Int>()))
//
//            } else {
//
//                put("segmentation_type",(radioGroup!!.getChildAt(1) as RadioButton).text.toString().toLowerCase())
//                put("chapter_num",JSONArray(selectedList))
//                put("unit_num",JSONArray(ArrayList<Int>()))
//
//            }
//
//            put("total_book",false)
//        }
//
//        var intent:Intent = Intent(this@QuestionGeneratorActivity,QuestionGeneratorService::class.java).apply {
//
//            putExtra("json",json.toString())
//
//        }
//
//
//
//
//
//        Log.d(TAG, "getDataKtorr: "+ json.getString("segmentation_type"))
//
//    }.flowOn(Dispatchers.IO)

    var nStrBuilder = StringBuilder()



    private fun startSpeechRecognizer() {

        chatEditText.setText("")

        SpeechRecognizerInstance.getInstance(this,object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {


//                Toast.makeText(this@QuestionGeneratorActivity
//                    ,"Speak Now..."
//                    , Toast.LENGTH_SHORT).show()

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

                SpeechRecognizerInstance.destroyInstance()
                chatEditText.setDefaultState()

//                if (error == 9) {
//
//                    chatEditText.isSelected = false
//
//                    showAlertPrompt("Alert"
//                        ,"Go to settings to enable Google microphone permission"
//                        ,R.drawable.ic_android_black_24dp)
//
//                }

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

//                val confidenceScores = partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
//                val speechResultList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                val s = speechResultList?.firstOrNull()?.trim()
//
//                Log.d(TAG, "Data: "+s)
//
////// --------------------------
////// 1. Early returns FIRST
////// --------------------------
////                if (confidenceScores != null && confidenceScores.isNotEmpty() && confidenceScores[0] < 0.5f) {
////                    return
////                }
//
//                if (s.isNullOrEmpty()) {
//                    return
//                }
////
////// --------------------------
////// 2. Cancel previous runnable
////// --------------------------
//                speechHandler.removeCallbacks(speechRunnable!!)
////
////// --------------------------
////// 3. Safe string builder logic
////// --------------------------
//                if (nStrBuilder.isEmpty()) {
//
//                    nStrBuilder.append(s)
//                    chatEditText?.post { chatEditText?.append(s) }
//
//                } else {
//
//                    val current = nStrBuilder.toString()
//
//                    if (s.startsWith(current) && s.length > current.length) {
//
//                        val newPart = s.substring(current.length)
//                        nStrBuilder.append(newPart)
//
//                        chatEditText?.post { chatEditText?.append(newPart) }
//
//                    } else if (!s.trim().equals(nStrBuilder.toString().trim(), ignoreCase = true)) {
//
//                        val isValidNewStart = s.trim().split(" ").size==1
//
//                        if (isValidNewStart) {
//                            nStrBuilder.clear()
//                            nStrBuilder.append(s)
//                            chatEditText?.post { chatEditText?.append(s) }
//                        }
//                    }
//                }
////
////// --------------------------
////// 4. GUARANTEED repost
////// --------------------------
//                speechHandler.postDelayed(speechRunnable!!, 3000)


            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }


        },20000)

        SpeechRecognizerInstance.startSpeech()

    }

    var markBuilder:StringBuilder = StringBuilder()
//    private var serviceConnection:MyServiceConnection = MyServiceConnection()

    private fun startQuestionService(query:String) {

        quesNo++

        pdfMap.put(quesNo,java.util.ArrayList<String?>())

            dataSet.add(
                DataSet(
                    System.currentTimeMillis(),
                    MySpannableStringBuilder(query),
                    false,
                    null,
                    true,
                    true, quesNo = quesNo
                )
            )
            chatAdapter.notifyItemInserted(dataSet.size - 1)

            chatEditText.setText("")


            dataSet.add(
                DataSet(
                    System.currentTimeMillis(),
                    MySpannableStringBuilder("Thinking"), false, ansDataList, false, true,isAnim = true
                    , quesNo = quesNo
                )
            )


            index = dataSet.size - 1

            chatAdapter.notifyItemInserted(index)

            markBuilder.clear()



            chatRecyclerView.scrollToPosition(index)

            val json = JSONObject().apply {
                put("query", query)
                put("class_number", classId)
                put("subject_name", subject)

                if ((radioGroup!!.getChildAt(0) as RadioButton).isChecked) {

                    put(
                        "segmentation_type",
                        (radioGroup!!.getChildAt(0) as RadioButton).text.toString().toLowerCase()
                    )
                    put("unit_num", JSONArray(selectedList))
                    put("chapter_num", JSONArray(ArrayList<Int>()))

                } else {

                    put(
                        "segmentation_type",
                        (radioGroup!!.getChildAt(1) as RadioButton).text.toString().toLowerCase()
                    )
                    put("chapter_num", JSONArray(selectedList))
                    put("unit_num", JSONArray(ArrayList<Int>()))

                }

                put("total_book", false)
            }

        Log.d(TAG, "startQuestionService: "+ json.toString())

        qDataReceiver = QuestionDataReceiver(applicationContext,query, jsonString = json.toString(), {

            runOnUiThread {

                if (it.startsWith("abdfg")) {

                    if (dataSet.get(index).isAnim) {

                        dataSet.get(index).spannableStringBuilder.clear()
                        dataSet.get(index).spannableStringBuilder.append("Error : ${it.substring(0,7)}")
                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
                        ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                        dataSet.get(index).isAnim = false

                        dataSet.get(index).isError = true



                            chatAdapter.notifyItemChanged(index)
                            chatEditText.isSelected = false
                            chatEditText.setDefaultState()




                    } else {

                            dataSet.add(
                                DataSet(
                                    System.currentTimeMillis(),
                                    MySpannableStringBuilder("Error : ${it.substring(0,7)}").apply {

                                        setSpan(ForegroundColorSpan(Color.RED),0,this.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    },
                                    false,
                                    null,
                                    false,
                                    false, quesNo = quesNo, isError = true
                                )
                            )

                        chatAdapter.notifyItemInserted(dataSet.size-1)

                            //Toast.makeText(this@QuestionGeneratorActivity,"Connect Error: ${it.substring(0,7)}",Toast.LENGTH_SHORT).show()

                            chatEditText.isSelected = false
                            chatEditText.setDefaultState()




                    }

                    //SpeechRecognizerInstance.destroyInstance()
//                unbindService(serviceConnection)
//                stopService(serviceIntent)
                    //bound = false

                    //return@QuestionDataReceiver

                } else if (!it.equals("Ended")) {


                        if (dataSet.get(index).isAnim) {

                            dataSet.get(index).isAnim = false
                            dataSet.get(dataSet.size-1).spannableStringBuilder.clear()
//                            dataSet.removeAt(dataSet.size-1)
                            chatAdapter.notifyItemChanged(dataSet.size-1)
                        }

                        dataSet.add(
                            DataSet(
                                System.currentTimeMillis(),
                                MySpannableStringBuilder("").apply {

                                    append(it)

                                },
                                false,
                                null,
                                false,
                                false, quesNo = quesNo
                            )
                        )

                        var qList = it.split(Regex("\\s{2,}",RegexOption.DOT_MATCHES_ALL))


                        pdfMap!!.get(quesNo)!!.addAll(qList)

                        chatAdapter.notifyItemInserted(dataSet.size-1)



                        if (isSpeech) {

                            //lineCount++

                            val regex = Regex("""(^\s*\|[-\s|]+\|\s*$)|([*#]+\s*)|(<page>\[[0-9]+\]</page>)|([-]+)""", RegexOption.MULTILINE)

                            //return markdown.replace(regex, "")
//
//
                            TextToSpeechInstance.speak(
                                it.replace(regex,"")!!, dataSet.size - 1, 0
                            )


                        } else {

                            chatRecyclerView.scrollToPosition(dataSet.size-1)

                        }






                } else {



                        dataSet.add(
                            DataSet(
                                System.currentTimeMillis(),
                                MySpannableStringBuilder("").apply {

                                    append("Download Pdf")

                                },
                                false,
                                null,
                                false,
                                false, quesNo = quesNo
                            )
                        )

                        chatAdapter.notifyItemInserted(dataSet.size-1)

                        SpeechRecognizerInstance.destroyInstance()
                        chatEditText.setDefaultState()

                }
            }

        })

    }

    override fun onStop() {
        super.onStop()


        Log.d(TAG, "onStop: " + QuestionGeneratorService.isRunning)

        nStrBuilder.clear()
        chatEditText.setDefaultState()
        SpeechRecognizerInstance.destroyInstance()



        statusBinder?.setStatusCallback(null)

        unbindService(statusServiceConnection!!)
        stopService(statusIntent)


    }



    private fun getData(url:String) {


        OkHttpClientInstance.getInstance(object : OkHttpClientInstance.ResultReceiver{
            override fun onResult(code: Int, response: okhttp3.Response?) {


                response?.let {resp->

                    resp.body?.let {

                        val gson: Gson = Gson()
                        var clsResp:CurriculumResponse2 = gson.fromJson(it.string(), CurriculumResponse2::class.java)

                        if (clsResp.data!!.totalChapters!!>0) {

                            for (c in clsResp!!.data!!.list) {

                                chapterList.add(c.chapter!!)
                            }

                        } else {

                            for (u in clsResp!!.data!!.list) {

                                unitList.add(u.unit!!)
                            }


                        }

                        Log.d(TAG, "UnitList: "+unitList.size)

                        if (unitList.size>0) {

                            runOnUiThread {

                                (radioGroup!!.getChildAt(0) as RadioButton).isChecked = true
                                (radioGroup!!.getChildAt(1) as RadioButton).isChecked = false
                                (radioGroup!!.getChildAt(1) as RadioButton).visibility = View.INVISIBLE

                                var adapter = ListViewAdapter(this@QuestionGeneratorActivity,java.util.ArrayList(unitList))

                                listView!!.adapter = adapter


                            }

                        } else {

                            runOnUiThread {

                                (radioGroup!!.getChildAt(0) as RadioButton).visibility = View.INVISIBLE
                                (radioGroup!!.getChildAt(0) as RadioButton).isChecked = false
                                (radioGroup!!.getChildAt(1) as RadioButton).isChecked = true

                                var adapter = ListViewAdapter(this@QuestionGeneratorActivity,java.util.ArrayList(chapterList))

                                listView!!.adapter = adapter


                            }
                        }
                    }
                }
            }

            override fun onError(code: Int, response: okhttp3.Response?) {

                Log.d(TAG, "onError: " + response?.body!!.string())

            }

            override fun onException(error: String?) {

                Log.d(TAG, "onException: "+error)

                //throw RuntimeException(error)

            }


        })

        OkHttpClientInstance.get(url)





    }



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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startRecording()

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

            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required for speech recognition",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startRecording() {
        //checkPermissions()
        //mRecorder!!.start()

        //WhisperInstance.startRecording()
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

    private fun hideKeyBoard() {

        val imm:InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(mainLayout?.windowToken,0)

    }


    override fun onDestroy() {
        super.onDestroy()


//            if (bound) {
//                //unbindService(serviceConnection)
//                bound = false
//            }



    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private inner class ListViewAdapter(c:Context,var dataList:java.util.ArrayList<Int>) : BaseAdapter() {




        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = convertView ?: layoutInflater.inflate(R.layout.single_item_list, parent, false)

            val checkBox: CheckBox = view.findViewById(R.id.unit_chapter_checkbox)
            checkBox.text = dataList[position].toString()

            // Set checked state based on selectedList
            checkBox.setOnCheckedChangeListener(null) // avoid recycling triggering old listeners
            checkBox.isChecked = selectedList.contains(dataList[position])

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                val value = dataList[position]

                if (isChecked) {
                    if (!selectedList.contains(value)) {
                        selectedList.add(value)
                    }
                } else {
                    selectedList.remove(value)
                }
            }

            return view
        }


        override fun getCount(): Int {

            return dataList.size

        }

        override fun getItem(position: Int): Any {

           return dataList.get(position)
        }

        override fun getItemId(position: Int): Long {

            return position.toLong()
        }


    }

    private var streamBinder: QuestionGeneratorService.StreamBinder? = null
    private var bound = false
    private var statusBinder:StatusService.StatusBinder?=null
    private var status:String?=null
    

//    private inner class MyServiceConnection : ServiceConnection {
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//
//            streamBinder = service as QuestionGeneratorService.StreamBinder
//            bound = true
//
//
//
//            streamBinder!!.setCallback {
//
//                if (it.startsWith("abdfg")) {
//
//                    if (dataSet.get(index).isAnim) {
//
//                        dataSet.get(index).spannableStringBuilder.clear()
//                        dataSet.get(index).spannableStringBuilder.append("Connect Error : ${it.substring(0,7)}")
//                        dataSet.get(index).spannableStringBuilder.setSpan(ForegroundColorSpan(Color.RED)
//                            ,0,dataSet.get(index).spannableStringBuilder.length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        dataSet.get(index).isAnim = false
//
//                        runOnUiThread {
//
//                            chatAdapter.notifyItemChanged(index)
//                            chatEditText.isSelected = false
//                            chatEditText.setDefaultState()
//
//
//                        }
//
//                    } else {
//
//                        runOnUiThread{
//
//                            Toast.makeText(this@QuestionGeneratorActivity,"Connect Error: ${it.substring(0,7)}",Toast.LENGTH_SHORT).show()
//
//                            chatEditText.isSelected = false
//                            chatEditText.setDefaultState()
//
//
//                        }
//
//                    }
//
//                    SpeechRecognizerInstance.destroyInstance()
//                    unbindService(serviceConnection)
//                    stopService(serviceIntent)
//                    bound = false
//
//                    return@setCallback
//                }
//
////
//
//                if (!it.equals("Ended")) {
//
//
//
//                    isRunning = true
//
//                    runOnUiThread {
//
//                        if (dataSet.get(index).isAnim) {
//
//                            dataSet.get(index).isAnim = false
//                            dataSet.get(dataSet.size-1).spannableStringBuilder.clear()
////                            dataSet.removeAt(dataSet.size-1)
//                            chatAdapter.notifyItemChanged(dataSet.size-1)
//                        }
//
//                        dataSet.add(
//                            DataSet(
//                                System.currentTimeMillis(),
//                                MySpannableStringBuilder("").apply {
//
//                                    append(it)
//
//                                },
//                                false,
//                                null,
//                                false,
//                                false, quesNo = quesNo
//                            )
//                        )
//
//                        var qList = it.split(Regex("\\s{2,}",RegexOption.DOT_MATCHES_ALL))
//
//
//                        pdfMap!!.get(quesNo)!!.addAll(qList)
//
//                        chatAdapter.notifyItemInserted(dataSet.size-1)
//
//
//
//                        if (isSpeech) {
//
//                            //lineCount++
//
//                            val regex = Regex("""(^\s*\|[-\s|]+\|\s*$)|([*#]+\s*)|(<page>\[[0-9]+\]</page>)|([-]+)""", RegexOption.MULTILINE)
//
//                            //return markdown.replace(regex, "")
////
////
//                            TextToSpeechInstance.speak(
//                                it.replace(regex,"")!!, dataSet.size - 1, 0
//                            )
//
//
//                        } else {
//
//                            chatRecyclerView.scrollToPosition(dataSet.size-1)
//
//                        }
//
//                        //highlightPageNumber(dataSet.get(dataSet.size-1).spannableStringBuilder.toString())
//
//
//                    }
//
//                } else {
//
//                    isRunning = false
//
//                    runOnUiThread {
//
//                        dataSet.add(
//                            DataSet(
//                                System.currentTimeMillis(),
//                                MySpannableStringBuilder("").apply {
//
//                                    append("Download Pdf")
//
//                                },
//                                false,
//                                null,
//                                false,
//                                false, quesNo = quesNo
//                            )
//                        )
//
//                        chatAdapter.notifyItemInserted(dataSet.size-1)
//
//                        SpeechRecognizerInstance.destroyInstance()
//                        chatEditText.setDefaultState()
//
//                        SpeechRecognizerInstance.destroyInstance()
//                        unbindService(serviceConnection)
//                        stopService(serviceIntent)
//                        bound = false
//
//                    }
//                }
//
//
//                }
//
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//
//            bound = false
//
//
//        }
//
//    }
    
    private inner class StatusServiceConnection:ServiceConnection{

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            statusBinder = service as StatusService.StatusBinder
            statusBinder!!.setStatusCallback {

                runOnUiThread {

                    if(it.equals("down")) {

                        status = it

//                    if (bound) {
//
//                        bound = false
//
//                        //unbindService(serviceConnection)
//                        //stopService(serviceIntent)
//
//                    }

                        runOnUiThread {

                            if (dataSet.get(index).isAnim) {

                                dataSet.get(index).isAnim = false
                                dataSet.get(index).spannableStringBuilder.clear()
//                            dataSet.removeAt(dataSet.size-1)
                                chatAdapter.notifyItemChanged(index)

                                chatEditText.isSelected = false
                                chatEditText.setDefaultState()
                            }


                        }




                        if (!isServiceAlert) {


                            isServiceAlert = true
                            showServiceAlert()

                        }


                    } else {

                        status = it

                        //isServiceAlert = false
                    }
                    Log.d(TAG, "ChatStatus: "+status)


                }



            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {



        }
    }

    var isServiceAlert = false


    private fun showServiceAlert() {

        runOnUiThread{

            var aBuilder = AlertDialog.Builder(this@QuestionGeneratorActivity)
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

    var customDialog:Dialog?=null





    var toDownload:ArrayList<String> = arrayListOf<String>()



    var modelDialog:Dialog?=null
    var progressBar:ProgressBar?=null
    var progressTxt:TextView?=null
    var progressMsg:TextView?=null
    var titleText:TextView?=null

    override fun onDownloadStarted() {

        modelDialog = CustomDialogBox()
            .buildDialog(this@QuestionGeneratorActivity,R.layout.layout_download_model)
            .setSize((Utils.getScreenWidth(this@QuestionGeneratorActivity)*0.60).toInt(),(Utils.getScreenHeight(this@QuestionGeneratorActivity)*0.40).toInt())
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
        progressTxt!!.visibility = View.VISIBLE

        progressBar!!.setProgress(0)
        progressMsg!!.text = "0"
        progressTxt!!.text = "Downloading..."


    }

    override fun onDownloadComplete(result:Int) {

        runOnUiThread {

            progressTxt!!.text = "Success"

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

            progressTxt!!.text = "Download failed"
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