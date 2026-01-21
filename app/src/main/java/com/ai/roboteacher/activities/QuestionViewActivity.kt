package com.ai.roboteacher.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ai.roboteacher.LinearLayoutManagerWrapper
import com.ai.roboteacher.R
import com.ai.roboteacher.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.ArrayList


class QuestionViewActivity : AppCompatActivity() {

    private val TAG = QuestionViewActivity::class.java.name
    private var dataList: ArrayList<String?>? = ArrayList()
    private var questionRecycler:RecyclerView?=null
    private var btn_download:FloatingActionButton?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_view)

        val btnBack:ImageView = findViewById(R.id.button_back)

        btnBack.setOnClickListener {

            finish()
        }





        questionRecycler = findViewById(R.id.ques_recycler)
        btn_download = findViewById(R.id.download_pdf)

        questionRecycler!!.layoutManager = LinearLayoutManagerWrapper(this)
        btn_download!!.setOnClickListener {

            var file = Utils.generatePDF1(this@QuestionViewActivity,dataList)

            var intent = Intent(this@QuestionViewActivity,PdfActivity::class.java)
            intent.putExtra("pdfFile",file)
            startActivity(intent)


        }

        var pdfIntent = intent

        pdfIntent?.let {

            handleIntent(pdfIntent)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        intent.let {

            handleIntent(intent)

        }

    }

    private fun handleIntent(pdfIntent:Intent) {

        dataList = pdfIntent.getStringArrayListExtra("dataList")
        //dataList!!.add("Download Pdf")

        Log.d(TAG, "handleIntent: ${dataList!!.get(0)}")

        var adapter = ChatAdapter()
        questionRecycler!!.adapter = adapter

    }

    private inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.single_item_chat_question, parent, false)
            )

        }

        override fun getItemCount(): Int {

            return dataList!!.size

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.logo.visibility = View.GONE

            holder.textView.text = dataList!!.get(position)
            holder.textView.setTextColor(Color.BLACK)

//            if (!dataList!!.get(position).equals("Download Pdf")) {
//
//                holder.textView.setText(dataList!!.get(position))
//                holder.textView.setTextColor(Color.BLACK)
//
//            } else {
//
//                holder.textView.setText(dataList!!.get(position))
//                holder.textView.setTextColor(Color.BLUE)
//                holder.textView.movementMethod = LinkMovementMethod.getInstance()
//
//            }

        }


        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

            var textView: TextView = v.findViewById(R.id.text_query)
            var logo: ImageView = v.findViewById(R.id.text_logo)

        }

    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }
}