//package com.ai.roboteacher.whisperasr
//
//import android.content.Context
//import android.util.Log
//import com.whispertflite.asr.Recorder
//import com.whispertflite.asr.Recorder.RecorderListener
//import com.whispertflite.asr.Whisper
//import com.whispertflite.asr.Whisper.WhisperListener
//import java.io.File
//
//object WhisperInstance {
//
//    internal object wc{
//        const val ENGLISH_ONLY_MODEL_EXTENSION: String = ".en.tflite"
//        const val ENGLISH_ONLY_VOCAB_FILE: String = "filters_vocab_en.bin"
//        const val MULTILINGUAL_VOCAB_FILE: String = "filters_vocab_multilingual.bin"
//        const val BASE_MODEL_ENGLISH = "whisper-base.tflite"
//        const val BASE_MODEL_MULTILINGUAL = "whisper-base.TOP_WORLD.tflite"
//        val MODEL_ARR:Array<String> = arrayOf("whisper-base.tflite","filters_vocab_multilingual.bin")
//
//        var MODEL_ARR_URLS = arrayOf("http://192.168.1.41/panda/aimodel/whisper-base.tflite"
//            ,"http://192.168.1.41/panda/aimodel/filters_vocab_multilingual.bin")
//
//        //const val BASE_MODEL_ENGLISH = "whisper-tiny.tflite"
//    }
//
//    private val TAG:String = WhisperInstance::class.java.name
//
//    var modelFile:File?=null
//    var vocabFile:File?=null
//    var mWhisper:Whisper?=null
//    var mRecorder:Recorder?=null
//    var recorderListener:RecorderListener?= null
//    var whisperListener:WhisperListener?= null
//    var vocabFileName:String?=null
//
//    fun getInstance(c:Context) {
//
//        if (modelFile == null) {
//
//            modelFile = File(c.getExternalFilesDir(null), wc.BASE_MODEL_ENGLISH)
//
//            val isMultilingualModel =
//                !(modelFile!!.name.endsWith(wc.ENGLISH_ONLY_MODEL_EXTENSION))
//            vocabFileName =
//                if (isMultilingualModel) wc.MULTILINGUAL_VOCAB_FILE else wc.ENGLISH_ONLY_VOCAB_FILE
//            vocabFile = File(c.getExternalFilesDir(null), "filters_vocab_multilingual.bin")
//        }
//
//        if (mWhisper == null) {
//
//            mWhisper = Whisper(c.applicationContext)
//            mWhisper!!.loadModel(modelFile, vocabFile, true)
////            mWhisper!!.setAction(Whisper.ACTION_TRANSCRIBE)
////            mWhisper!!.setLanguage(50276)
//            Log.d(TAG, "Initialized: " + modelFile!!.name)
////            mWhisper!!.setListener(object : WhisperListener {
////                override fun onUpdateReceived(message: String) {
////                    Log.d(TAG, "Update is received, Message: $message")
////
//////                if (message == Whisper.MSG_PROCESSING) {
//////                    runOnUiThread { tvStatus.setText(getString(R.string.processing)) }
//////                    startTime = System.currentTimeMillis()
//////                    runOnUiThread { spinnerTflite.setEnabled(false) }
//////                }
////                }
////
////                override fun onResultReceived(whisperResult: WhisperResult) {
////
////                    Log.d(TAG, "onResultReceived: "+whisperResult.result)
////
////                    runOnUiThread {
////
////                        customDialog!!.dismiss()
////
////                        editTextQuery!!.append(whisperResult.result) }
////
////                    speechHandler.postDelayed(speechRunnable!!,3000)
////
////                    //speechHandler.postDelayed(speechRunnable!!, 3000)
////
//////                if ((whisperResult.language == "zh") && (whisperResult.task == Whisper.Action.TRANSCRIBE)) {
//////                    runOnUiThread { layoutModeChinese.setVisibility(View.VISIBLE) }
//////                    val simpleChinese: Boolean =
//////                        sp.getBoolean("simpleChinese", false) //convert to desired Chinese mode
//////                    val result: String =
//////                        if (simpleChinese) ZhConverterUtil.toSimple(whisperResult.result) else ZhConverterUtil.toTraditional(
//////                            whisperResult.result
//////                        )
//////                    runOnUiThread { tvResult.append(result) }
//////                } else {
//////                    runOnUiThread { layoutModeChinese.setVisibility(View.GONE) }
////////                    runOnUiThread { tvResult.append(whisperResult.result) }
//////                }
//////                runOnUiThread { spinnerTflite.setEnabled(true) }
////
////                }
////            })
//
//
//        }
//
//        if (mRecorder==null) {
//
//            mRecorder = Recorder(c.applicationContext)
////            mRecorder!!.setListener(Recorder.RecorderListener { message ->
////
////                if (message.equals(Recorder.MSG_RECORDING_DONE)) {
////
////                    speechHandler.removeCallbacks(speechRunnable!!)
////
////                    runOnUiThread {
////
////                        if (customDialog == null) {
////
////                            customDialog = CustomDialogBox()
////                                .buildDialog(this@GeneralActivity, R.layout.layout_processing_audio)
////                                .setSize(
////                                    (com.ai.roboteacher.Utils.getScreenWidth(this@GeneralActivity) * 0.60).toInt(),
////                                    (com.ai.roboteacher.Utils.getScreenHeight(this@GeneralActivity) * 0.30).toInt()
////                                )
////                                .setCancelable(false)
////                                .createDialog()
////
////                        }
////
////
////
////                        customDialog!!.show()
////
////                        customDialog!!.findViewById<TextView>(R.id.progress_text).apply {
////
////                            setText("Processing...")
////                            setTextColor(ContextCompat.getColor(this@GeneralActivity, R.color.black))
////                        }
////
////                    }
////
////                    Log.d(TAG, "Recorder:Update is received")
////                    startProcessing(Whisper.ACTION_TRANSCRIBE)
////
////                }
////
////
////                //startProcessing(Whisper.ACTION_TRANSCRIBE)
//////            Log.d(MainActivity.TAG, "Update is received, Message: $message")
//////            if (message == Recorder.MSG_RECORDING) {
//////                runOnUiThread { tvStatus.setText(getString(R.string.record_button) + "…") }
//////                if (!append.isChecked()) runOnUiThread { tvResult.setText("") }
//////                runOnUiThread { btnRecord.setBackgroundResource(R.drawable.rounded_button_background_pressed) }
//////            } else if (message == Recorder.MSG_RECORDING_DONE) {
//////                HapticFeedback.vibrate(mContext)
//////                runOnUiThread { btnRecord.setBackgroundResource(R.drawable.rounded_button_background) }
//////
//////                if (translate.isChecked()) startProcessing(Whisper.ACTION_TRANSLATE)
//////                else startProcessing(Whisper.ACTION_TRANSCRIBE)
//////            } else if (message == Recorder.MSG_RECORDING_ERROR) {
//////                HapticFeedback.vibrate(mContext)
//////                if (countDownTimer != null) {
//////                    countDownTimer.cancel()
//////                }
//////                runOnUiThread {
//////                    btnRecord.setBackgroundResource(R.drawable.rounded_button_background)
//////                    processingBar.setProgress(0)
//////                    tvStatus.setText(getString(R.string.error_no_input))
//////                }
//////            }
////            })
//
//
//        }
//
//        // Audio recording functionality
//
//
//
//    }
//
//    public fun setListener(c:Context?) {
//
//        recorderListener = c as RecorderListener?
//        whisperListener = c as WhisperListener?
//
//        mWhisper?.let {
//
//            mWhisper!!.setListener(whisperListener)
//        }
//
//        mRecorder?.let {
//
//            mRecorder!!.setListener(recorderListener)
//        }
//
//    }
//
//    fun startRecording() {
//
//        mRecorder?.start()
//    }
//
//    public fun stopRecording() {
//        //checkPermissions()
//        mRecorder?.stop()
//    }
//
//    public fun startProcessing(samples:ByteArray) {
//
//        mWhisper?.start(samples)
//
//    }
//
//    public fun startProcessing(samples:ByteArray,langToken:Int) {
//
//        mWhisper!!.setAction(Whisper.ACTION_TRANSCRIBE)
//        mWhisper!!.setLanguage(langToken)
//
//        mWhisper?.start(samples)
//
//    }
//
//
//}