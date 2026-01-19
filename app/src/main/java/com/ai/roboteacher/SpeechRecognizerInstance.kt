package com.ai.roboteacher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.speech.ModelDownloadListener
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale
import java.util.concurrent.Executors


object SpeechRecognizerInstance {

     var speechRecognizer: SpeechRecognizer?=null
     var intent:Intent?=null

     fun getInstance(c:Context,recognitionListener: RecognitionListener,duration:Int=2000,lang:String="en-IN") {

         
            if (speechRecognizer==null) {

                if (SpeechRecognizer.isRecognitionAvailable(c)) {

                    Log.d(SpeechRecognizerInstance::class.java.name, "getInstance: ")
                }

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(c)

//                intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-us")
//
////            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000) // 10 sec
////            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
//                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,duration)
//                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
//                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
//                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true)

                    // better to use silence length instead of minimum length
                    //putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000)
                    //putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000)
                }

                intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

                    // Correct language
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)


                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 700)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 100)

                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    //putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, MediaRecorder.AudioSource.VOICE_RECOGNITION)
//                }

                speechRecognizer!!.setRecognitionListener(recognitionListener)

            }


    }

    public fun setSpeechListener(recognitionListener: RecognitionListener) {

        speechRecognizer!!.setRecognitionListener(recognitionListener)


    }

    public fun startSpeech() {

//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("en","IN"))
//
////            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000) // 10 sec
////            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
//            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000)
//            // better to use silence length instead of minimum length
//            //putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000)
//            //putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000)
//        }

        speechRecognizer!!.startListening(intent)





//        if (Build.VERSION.SDK_INT>=33) {
//
//            speechRecognizer!!.checkRecognitionSupport(intent
//                ,Executors.newSingleThreadExecutor()
//            ,object : RecognitionSupportCallback{
//                    override fun onSupportResult(recognitionSupport: RecognitionSupport) {
//
//                        val supportedLanguages = recognitionSupport.installedOnDeviceLanguages
//                        val needsDownload = recognitionSupport.supportedOnDeviceLanguages
//
//                        if (supportedLanguages.contains("bn-IN")) {
//
//                            speechRecognizer!!.startListening(intent)
//
//                        } else {
//
//                            triggerModelDownload(intent)
//
//                        }
//
//                    }
//
//                    override fun onError(error: Int) {
//
//                    }
//
//
//                })
//
//
//        }

    }

    public fun stopSpeech() {

        speechRecognizer!!.stopListening()
    }

    private fun triggerModelDownload(intent:Intent) {

        if (Build.VERSION.SDK_INT>=34) {


            speechRecognizer!!.triggerModelDownload(intent
                ,Executors.newSingleThreadExecutor()
                ,object:ModelDownloadListener{

                    override fun onProgress(completedPercent: Int) {

                        Log.d(SpeechRecognizerInstance::class.java.name
                            , "onProgress: "+completedPercent)

                    }

                    override fun onSuccess() {

                        speechRecognizer!!.startListening(intent)

                    }

                    override fun onScheduled() {


                    }

                    override fun onError(error: Int) {


                    }


                })
        }


    }

    fun destroyInstance() {

        speechRecognizer?.destroy()
        speechRecognizer?.setRecognitionListener(null)
        speechRecognizer = null

    }

//    fun stopSpeech() {
////        SpeechRecognizerInstance!!.stopListening()
//    }


//    private fun startSpeechRecognizer() {
//
//        if (SpeechRecognizer.isRecognitionAvailable(this@ChatActivity)) {
//
//            Log.d(SelectClassActivity::class.java.name, "startSpeechRecognizerAvailable")
//
//            val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
//
//            var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//            intent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)
//            //intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak Now...")
//
//            speechRecognizer.setRecognitionListener(object : RecognitionListener {
//                override fun onReadyForSpeech(params: Bundle?) {
//
//                    Toast.makeText(this@ChatActivity
//                        ,"Speak Now..."
//                        , Toast.LENGTH_SHORT).show()
//
//                }
//
//                override fun onBeginningOfSpeech() {
//
//                }
//
//                override fun onRmsChanged(rmsdB: Float) {
//
//                }
//
//                override fun onBufferReceived(buffer: ByteArray?) {
//
//                }
//
//                override fun onEndOfSpeech() {
//
//                }
//
//                override fun onError(error: Int) {
//
//                    Toast.makeText(this@ChatActivity,"Error", Toast.LENGTH_SHORT).show()
//
//                    Log.d(SelectClassActivity::class.java.name, "onError: ${error}")
//
//                }
//
//                override fun onResults(results: Bundle?) {
//
//                    var speechResultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                    Log.d(SelectClassActivity::class.java.name, "Results: ${speechResultList?.get(0)}")
//
//                    speechResultList?.let {
//
//                        chatEditText.setText(speechResultList.get(0))
//
//                        processData(speechResultList.get(0))
//
//
//                    }
//
//                }
//
//                override fun onPartialResults(partialResults: Bundle?) {
//
//                }
//
//                override fun onEvent(eventType: Int, params: Bundle?) {
//
//                }
//
//
//            })
//
//            speechRecognizer.startListening(intent)
//
//
//        }
//
//
//    }
}