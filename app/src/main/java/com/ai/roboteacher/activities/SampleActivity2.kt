package com.ai.roboteacher.activities

import CustomDialogBox
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.speech.tts.UtteranceProgressListener
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ai.roboteacher.KtorDataReceiver
import com.ai.roboteacher.R
import com.ai.roboteacher.TextToSpeechInstance
import com.ai.roboteacher.Utils
import com.github.barteksc.pdfviewer.PDFView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


class SampleActivity2:AppCompatActivity() {

    var audio:AudioRecord?=null
    var bufferSize = 0
    val pdfView:PDFView?=null

    val d = "**छोटी-छोटी बातें**\n" +
            "\n" +
            "नन्हा सूरज धीरे-धीरे उग आया,  \n" +
            "फूलों ने चुपचाप मुस्कान बाँटा।  \n" +
            "पंछियों के गीत से हवा भर गई,  \n" +
            "क्लास में सब मिलकर ख़ुशी बाँट गई।  \n" +
            "\n" +
            "किताबें खुलें, कहानियाँ बोलें,  \n" +
            "गणित के प्रश्न हल हो जाएँ।  \n" +
            "हाथ मिलाकर सब साथ खेलें,  \n" +
            "सीखें, गाएँ और मज़े करें[25]।"

    val urlStr = "http://192.168.1.41/pdf/%d/science/science_book.pdf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample2)

//        val urlStr = "http://192.168.1.41/pdf/6/science/science_book.pdf"
//
//        val d = 6
//
//        val s = String.format(urlStr,d)
//
//        val txtView:TextView = findViewById(R.id.text_view)
//        val strBuilder = StringBuilder()
//
//
//        var customDialog = CustomDialogBox()
//            .buildDialog(this,R.layout.layout_download_pdf)
//            .setSize((Utils.getScreenWidth(this)*0.60).toInt(),(Utils.getScreenHeight(this)*0.40).toInt())
//            .createDialog()
//
//        customDialog.show()
//
//        customDialog.findViewById<TextView>(R.id.progress_text).apply {
//
//            setText("Opening PDF")
//            setTextColor(ContextCompat.getColor(this@SampleActivity2,R.color.black))
//        }



//        val markwon = Markwon.builder(this)
//            .usePlugin(io.noties.markwon.ext.tables.TablePlugin.create(this))
//            .usePlugin(io.noties.markwon.image.coil.CoilImagesPlugin.create(this))
//            .build()

//        val markwon = Markwon.builder(this)
//
//            .usePlugin(TablePlugin.create(this)) // <-- important
//            .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
//            .build()
//
//        val dataThread = KtorDataReceiver(System.currentTimeMillis().toString(),"Tell a story in table format",6,"English",object:KtorDataReceiver.KtorDataListener{
//            override suspend fun onDataReceived(line: String) {
//
//                Log.d(SampleActivity2::class.java.name, "onDataReceived: "+line)
//
//                strBuilder.append(line)
//
//                runOnUiThread {
//
//                    markwon.setMarkdown(txtView,strBuilder.toString())
//                }
//
//            }
//
//            override suspend fun onError(ex: Exception) {
//
//
//            }
//
//
//        })
//        dataThread!!.run()



//        CoroutineScope(Dispatchers.IO).launch {
//
//            try {
//
//                val url:URL = URL(urlStr)
//
//                val connection = url.openConnection() as HttpURLConnection
//
//                connection.connect()
//
//                val ipStream = connection.inputStream
//                val total = connection.contentLength
//
//                var downloaded = 0
//
//                var root:File = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"sample.pdf")
//
//                var fos = FileOutputStream(root)
//
//                var buffer = ByteArray(8192)
//
//                var i = 0
//
//                while (ipStream.read(buffer).also { i = it } != -1) {
//
//                    downloaded+=i
//                    fos.write(buffer, 0, i)
//
//                    withContext(Dispatchers.Main){
//
//                        var p:Float = ((downloaded.toFloat()/total.toFloat())*100f).toFloat()
//
//                        customDialog.findViewById<ProgressBar>(R.id.progressRing).progress = p.toInt()
//                        customDialog.findViewById<TextView>(R.id.progress_msg).text = p.toInt().toString()
//
//
//                    }
//
//
//                }
//
//                fos.flush()
//
//                runOnUiThread {
//
//                    Toast.makeText(this@SampleActivity2,"Pdf downloaded",Toast.LENGTH_SHORT).show()
//
//                    //openPdfAtPage(25,root)
//                }
//
//            } catch (ex:Exception) {
//
//                Log.d(SampleActivity2::class.java.name, "Error: "+ex.message)
//
//
//            }
//
//        }






//        if (d.endsWith("\\[0-9+\\]"))

//        TextToSpeechInstance.getInstance(this)
//
//        TextToSpeechInstance.setProgressListener(object : UtteranceProgressListener(){
//            override fun onStart(utteranceId: String?) {
//
//                Log.d(SampleActivity2::class.java.name, "onStart: ")
//            }
//
//            override fun onDone(utteranceId: String?) {
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
//        Handler().postDelayed({TextToSpeechInstance.speak(d,0,0)},6000)




//        val ll:ImageView = findViewById(R.id.holder)
//
//        val bmp:Bitmap = Bitmap.createBitmap(500,500,Bitmap.Config.ARGB_8888)
//
//        val canvas:Canvas = Canvas(bmp)
//
//        val paint = Paint()
//        paint.color = Color.RED
//        paint.style = Paint.Style.FILL
//
//        canvas.save()
//
//
//// Skew canvas by 0.5 in X (≈ 26.5 degrees)
//        canvas.skew(0f, 0.2f)
//
//
//// Draw rectangle → it appears slanted
//        canvas.drawRect(100f, 100f, 300f, 200f, paint)
//
//        canvas.restore()
//
//        ll.setImageBitmap(bmp)
//
//        //ll.draw(canvas)
//
//
////        var roboLip:RoboLip = findViewById(R.id.robo_lip)
////
////        var listSc = arrayListOf(Pair<String,Float>("scaleY",0.5f)
////            ,Pair<String,Float>("scaleY",0.3f)
////        ,Pair<String,Float>("scaleY",1.0f)
////        ,Pair<String,Float>("scaleY",1.2f)
////        ,Pair<String,Float>("scaleY",0.7f)
////        ,Pair<String,Float>("scaleY",0.9f)
////        ,Pair<String,Float>("scaleY",0.2f))
////
////        roboLip.post {
////
////            CoroutineScope(Dispatchers.Main).launch {
////
////                while (true) {
////
////                    var p = listSc.get(Random.nextInt(listSc.size))
////
////                    if (p.first == "scaleX") {
////
////                        runOnUiThread {
////
////                            roboLip.scaleX = p.second
////
////                        }
////
////                    } else if (p.first == "scaleY") {
////
////                        runOnUiThread {
////
////                            roboLip.scaleY = p.second
////
////
////                        }
////
////                    }
////
////                    delay(300)
////
////                }
////
////
////            }
////
////
////        }
////
////
////
////
////
////        var click:Button = findViewById(R.id.click)
////
////        click.setOnClickListener {
////
////            roboLip.startTalk()
////
////
////        }
//
////        initRecorder()
////
////        var bStart:Button = findViewById(R.id.start)
////
////        bStart.setOnClickListener {
////            audio?.startRecording()
//
//            Thread {
//                val buffer = ByteArray(bufferSize)
//                var sumSq: Long = 0
//                var totalSamples = 0
//
//                val startTime = System.currentTimeMillis()
//                while (System.currentTimeMillis() - startTime < 5000) {
//                    val bytesRead = audio!!.read(buffer, 0, buffer.size)
//                    val samples = bytesRead / 2
//
//                    for (i in 0 until samples) {
//                        val low = buffer[2 * i].toInt() and 0xFF
//                        val high = buffer[2 * i + 1].toInt()
//                        val s: Short = ((low) or (high shl 8)).toShort()
//                        sumSq += s.toLong() * s.toLong()
//                    }
//
//                    totalSamples += samples
//                }
//
//                audio?.stop()
//
//                if (totalSamples > 0) {
//                    val rmsRaw = Math.sqrt(sumSq.toDouble() / totalSamples)
//                    val rmsNorm = rmsRaw / Short.MAX_VALUE
//                    val dbFS = 20.0 * kotlin.math.log10(rmsNorm.coerceAtLeast(1e-12))
//
//                    Log.d("SampleActivity2", "RMS raw=$rmsRaw norm=$rmsNorm dBFS=$dbFS")
//                }
//            }.start()
//        }
    }

//    fun openPdfAtPage(page:Int,pdfFile: File) {
//
//        val pdfView:PDFView = findViewById(R.id.pdf_view)
//
//        pdfView!!.fromFile(pdfFile)
//            .defaultPage(page)
//            .enableSwipe(true)
//            .swipeHorizontal(false)
//            .load()
//
//
//
//
//    }

    private fun initRecorder() {

        val sampleRate = 16000
        try {
            bufferSize = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audio = AudioRecord(
                MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize
            )
        } catch (e: Exception) {
            Log.e("TrackingFlow", "Exception", e)
        }
    }
}