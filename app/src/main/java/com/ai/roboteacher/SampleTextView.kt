package com.ai.roboteacher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import ru.noties.jlatexmath.JLatexMathDrawable

class SampleTextView : View {

    val dReg = Regex("""\$\$(.*?)\$\$|\$(.*?)\$""",RegexOption.DOT_MATCHES_ALL)
    var mWidth = 0
    var mHeight = 0
    var textData = ""
    var xPosition = 0f
    var yPosition = 50f
    var p:Paint?=null
    val strBUilder = StringBuilder()
    var data:String = ""
    var process = false



     constructor(c: Context, a: AttributeSet) : super(c,a) {

         init()

    }

    constructor(c: Context, a: AttributeSet, defStyle:Int) : super(c,a,defStyle) {

        init()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mWidth = w
        mHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d(SampleTextView::class.java.name, "onDraw: ")

//        if (process) {

            var startIndex = 0

            var index = 0
            var endIndex = 0
            var eqData = ""

//            var m: MatchResult? = dReg.find(data)
//
//            if (m == null) {
//
//                Log.d(SampleTextView::class.java.name, "Null")
//            }


//            while (m != null) {
//                Log.d(SampleTextView::class.java.name, "Found: ${m.value}")
//
//                eqData = m.value
//
//                Log.d("abcde", "onDraw: "+eqData)
//
//                index = data.indexOf(m.value,startIndex)
//
//                renderText(data,startIndex,index,canvas)//startIndex,index
//                renderEq(eqData,canvas)
//                startIndex = index+eqData.length
//
//                m = m.next() // Move to the next match
//
//            }

            Log.d("abcde", "onDraw: ")


        //}



    }

    fun setTextttt(data:String) {

        this@SampleTextView.data = data

        process  = true

        invalidate()

//        this.post {
//
//
//        }





    }

    fun renderText(data:String,start:Int,end:Int,canvas: Canvas) {

        textData = data.substring(start,end)


        if (!textData.equals("")) {

            val lines = textData.split("\n")

            for (l in lines) {

                val words = l.split(" ")

                for (ww in words) {

                    Log.d(SampleTextView::class.java.name, "Word "+ww)
                }


                for (w in words) {

                    if (xPosition == 0f) {

                        canvas.drawText("$w ", xPosition, yPosition, p!!)
                        xPosition+=p!!.measureText("$w ")
                        strBUilder.append("$w ")



                    } else {

                        var x = p!!.measureText(strBUilder.toString())
                        var wLength = p!!.measureText("$w ")

                        if (x + wLength >= mWidth) {

                            xPosition = 0f
                            yPosition += 25f
                            strBUilder.clear()

                            canvas.drawText("$w ", xPosition, yPosition, p!!)
                            xPosition+=wLength
                            strBUilder.append("$w ")

                        } else {

                            canvas.drawText("$w ", xPosition, yPosition, p!!)
                            xPosition+=wLength
                            strBUilder.append("$w ")

                        }


                    }

                }

                xPosition = 0f
                yPosition+=25f
            }



        }

    }

    fun renderEq(eqData:String,canvas: Canvas) {

        xPosition = 0f
        yPosition+=35f

        val drawable = JLatexMathDrawable.builder(eqData)
            .textSize(20f)
            .build()

// Create bitmap
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val mCanvas = Canvas(bitmap)
        //drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(mCanvas)

        canvas.drawBitmap(bitmap,xPosition,yPosition,null)

        xPosition = 0f
        yPosition+=drawable.intrinsicHeight+30

    }

    private fun init() {

        p = Paint()
        p!!.isAntiAlias = true
        p!!.textSize = 20f
        p!!.color = Color.BLACK
        p!!.style = Paint.Style.FILL_AND_STROKE




    }


}
