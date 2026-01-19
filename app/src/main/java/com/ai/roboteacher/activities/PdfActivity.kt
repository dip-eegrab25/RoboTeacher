package com.ai.roboteacher.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.ai.roboteacher.R
import com.github.barteksc.pdfviewer.PDFView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import java.io.File

class PdfActivity:AppCompatActivity() {

    var pdfIntent:Intent?=null
    var filePath:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode(window)
        setContentView(R.layout.activity_pdf)

        pdfIntent = intent

        val back:ImageView = findViewById(R.id.yellow_panda_menu)
        filePath = pdfIntent!!.getStringExtra("pdfFile")

        back.setOnClickListener {

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

//       var markWon = Markwon.builder(this)
//            .usePlugin(MarkwonInlineParserPlugin.create())
////            .usePlugin(ImagesPlugin.create(ImagesPlugin.))
//            .usePlugin(TablePlugin.create(this)) // <-- important
//            .usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
//            .usePlugin((JLatexMathPlugin.create(16f,object: JLatexMathPlugin.BuilderConfigure{
//                override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
//
//                    builder.inlinesEnabled(true)
//
//
//                    builder.blocksEnabled(true);
//                }
//
//
//            })))
//            .build()
//
//        pdfIntent = intent
//
//        val txt:TextView = findViewById(R.id.txt_sample)
//
//        markWon.setParsedMarkdown(txt,markWon.toMarkdown(pdfIntent!!.getStringExtra("data")!!))




        val pdfView: PDFView = findViewById(R.id.pdf_view)


        pdfView.fromFile(File(filePath!!))
            .defaultPage(pdfIntent!!.getIntExtra("page",0))
            .enableSwipe(true)
            .swipeHorizontal(false)
            .load()
    }

    override fun onBackPressed() {
//        super.onBackPressed()
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

    override fun onDestroy() {
        super.onDestroy()

        val f = File(filePath!!)
        if (f.exists()) {

            f.delete()
        }
    }
}