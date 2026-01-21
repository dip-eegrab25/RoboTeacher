package com.ai.roboteacher.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai.roboteacher.R
import com.ai.roboteacher.Utils
import java.io.File

class PdfListActivity: AppCompatActivity() {

    var pdfRecycler:RecyclerView? = null
    var pdfList:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Utils.enterFullScreenMode(window)
        setContentView(R.layout.activity_pdf_list)

        val back:ImageView = findViewById(R.id.yellow_panda_menu)

        back!!.setOnClickListener {

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

        pdfRecycler = findViewById(R.id.pdf_recycler)
        pdfRecycler!!.layoutManager = GridLayoutManager(this,3)

        getPdfs()
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    private fun getPdfs() {

        var fileRoot = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        var files = fileRoot!!.listFiles()

        for (file in files) {

            if (file.isFile) {

                if (file.name.endsWith(".pdf")) {

                    pdfList.add(file.name)
                }
            }
        }

        pdfRecycler!!.adapter = PdfAdapter()
    }

    private inner class PdfAdapter : RecyclerView.Adapter<PdfAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.single_item_pdf, parent, false)
            )

        }

        override fun getItemCount(): Int {

            return pdfList.size

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.pdfName.setText(pdfList.get(position))

            holder.pdfViewRoot.setOnClickListener {

                var pdfFile:File = File(this@PdfListActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),pdfList.get(position))

                if (pdfFile.exists()) {

                    var intent = Intent(this@PdfListActivity,PdfActivity::class.java)
                    intent.putExtra("pdfFile",pdfFile.absolutePath)
                    startActivity(intent)
                }
            }


        }


        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

            var pdfViewRoot:View = v.findViewById(R.id.pdf_view_root)
            var pdfName:TextView = v.findViewById(R.id.textPdfName)

        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}