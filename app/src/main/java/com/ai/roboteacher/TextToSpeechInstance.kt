package com.ai.roboteacher

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedHashMap
import java.util.Locale

object TextToSpeechInstance {

    private var tts: TextToSpeech? = null
    private var isReady:Boolean = false
    //private var speechMap:LinkedHashMap<Int,Pair<Int,Int>> = LinkedHashMap()

    private var isStart:Boolean = false


    var speechQueue:ArrayDeque<Pair<String,String>> = ArrayDeque()
    private var speechStringBuilder:StringBuilder = StringBuilder()

    fun getInstance(c:Context) {

        if (tts == null) {

            tts = TextToSpeech(c) {status->

                if (status == TextToSpeech.SUCCESS) {

                    tts!!.setLanguage(Locale("en","IN"))

                    //tts!!.speak("    ",TextToSpeech.QUEUE_FLUSH,null,"first")

                    isReady = true

                }


            }

        }

    }

    fun speak(word: String,startIndex:Int,endIndex:Int) {

        speechQueue.add(Pair("$startIndex:$endIndex",word))

        if (isReady) {

            if (!isStart) {

                isStart = true

                Handler().postDelayed({

                    startUtter()

                }, 3000)

            }
        }
//
//
//
////            tts!!.speak(stringBuilder.substring(startIndex,endIndex), TextToSpeech.QUEUE_ADD, null, startIndex.toString())
//
//
////            CoroutineScope(Dispatchers.IO).launch {
////
////                val text = stringBuilder.trim().split("\\s+".toRegex())
////
////
////
////                text.forEachIndexed { index,word->
////
////                    tts!!.speak(word, TextToSpeech.QUEUE_ADD, null, index.toString())
////
////                }
////
////
////            }
//
//
//
//
//            //tts?.speak(stringBuilder, TextToSpeech.QUEUE_FLUSH, null, System.currentTimeMillis().toString())
//
//        }

//        if (isReady) {
//
//            tts!!.speak(word,TextToSpeech.QUEUE_ADD,null,"$startIndex:$endIndex")
//        }

    }



    fun destroyInstance() {

        if (tts != null) {
            tts!!.stop()
            //tts!!.shutdown()
            speechQueue.clear()
            //tts = null
            //isReady = false
            isStart = false

            Log.d(TextToSpeechInstance::class.java.name, "TTS destroyed")

        }

    }

    private fun startUtter() {

        CoroutineScope(Dispatchers.IO).launch {

            while (!speechQueue.isEmpty()) {

                var p:Pair<String,String> = speechQueue.removeFirst()

                try {

                    tts?.speak(
                        p.second,
                        TextToSpeech.QUEUE_ADD,
                        null,
                        p.first
                    )

                } catch (ex:Exception) {


                }


            }

//            while (!speechMap.isEmpty()) {
//
//
//
//
//            }

//            for (s in speechMap) {
//
//                tts!!.speak(
//                        s.value,
//                        TextToSpeech.QUEUE_ADD,
//                        null,
//                        "${s.key}"
//                    )
//
//
//            }

            isStart = false
            speechStringBuilder.clear()


        }




    }

    fun setProgressListener(utteranceProgressListener: UtteranceProgressListener) {

        tts!!.setOnUtteranceProgressListener(utteranceProgressListener)


    }







}