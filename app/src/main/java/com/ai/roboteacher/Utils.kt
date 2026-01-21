package com.ai.roboteacher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.Page
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import com.ai.roboteacher.Models.PdfModel
import ru.noties.jlatexmath.JLatexMathDrawable
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

object Utils {

    fun getScreenWidth(c:Context) : Int {

        return c.resources.displayMetrics.widthPixels

    }

    fun getScreenHeight(c:Context) : Int {

        return c.resources.displayMetrics.heightPixels

    }

    fun markdownToTts(text: String): String {
        var t = text

        t = t.replace(Regex("^\\s*|^[-\\s]*|^[#\\s]*",RegexOption.MULTILINE),"")


        t = t.replace(Regex("^`+\\w+",RegexOption.MULTILINE),"")
        t = t.replace("`","").replace(">","")

        t = t.replace(Regex("^#+\\w+",RegexOption.MULTILINE),"")
        t = t.replace("#","")

        t = t.replace(Regex("^\\|([|\\-]+)\\|", RegexOption.MULTILINE), "")

        t = t.replace("|","")

        // Remove bold / italic
        t = t.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        t = t.replace(Regex("\\*(.*?)\\*"), "$1")

        // Remove LaTeX block math $$ ... $$
        t = t.replace(Regex("\\$\\$(.*?)\\$\\$", RegexOption.DOT_MATCHES_ALL)) { match ->
            match.groupValues[1]
        }

        // Remove LaTeX inline math \( ... \)
        t = t.replace(Regex("\\\\\\((.*?)\\\\\\)")) { match ->
            match.groupValues[1]
        }

        // Convert common math notations to speech
        //t = t.replace("x^{2}", "x squared")
        t = t.replace("^2", " squared")
        t = t.replace("^{2}", " squared")
        t = t.replace("^3", " cubed")
        t = t.replace("^{3}", " cubed")
        t = t.replace("\\times","into")
        t = t.replace("\\pm","plus minus")
        t = t.replace("\\quad","")
        t = t.replace("\\text","")
        // Remove markdown list markers
        t = t.replace(Regex("^\\s*[-*]\\s*", RegexOption.MULTILINE), "")



        t = t.replace("+", " plus ")
        t = t.replace("-", " minus ")
        t = t.replace("=", " equals ")

        var regex = Regex("\\\\frac\\{(.*?)\\}\\{(.*?)\\}")

        var m:MatchResult? = regex.find(t)

        while (m!=null) {

            t = t.replace(m.groupValues[0],"${m.groupValues[1]} divided by ${m.groupValues[2]}")

            m = regex.find(t)

        }



        //t = t.replace("\\frac", " divided by ")
        t = t.replace("\\sqrt", " square root ")



        // Remove remaining LaTeX symbols
        t = t.replace(Regex("[{}\\\\]"), "")


//        // Normalize whitespace
//        t = t.replace(Regex("\\s+",RegexOption.MULTILINE), "")

        return t.trim()
    }

    public fun generatePDF1(c:Context,questionsList:java.util.ArrayList<String?>?):String? {

        for (q in questionsList!!) {

            Log.d("abcde", "generatePDF: "+q)
        }

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        var xPosition = 10f
        var yPosition = 60f
        var pageNumber = 1

        val pageWidth = 595
        val pageHeight = 842

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        for (q in questionsList!!) {
            val words = q!!.split(" ")

            for (word in words) {

                val wordWidth = paint.measureText("$word ")

                // If next word exceeds width, wrap to next line
                if (xPosition + wordWidth > pageWidth - 20) {
                    xPosition = 10f
                    yPosition += 14f
                }

                // If next line exceeds page height, create a new page
                if (yPosition > pageHeight - 60) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 60f
                    xPosition = 10f
                }

                // Draw the word
                canvas.drawText("$word ", xPosition, yPosition, paint)

                // Advance x position
                xPosition += wordWidth
            }

            // Reset for next question
            xPosition = 10f
            yPosition += 22f
        }

        pdfDocument.finishPage(page)

        return savePDF(c,pdfDocument)

//        return null
    }

//    fun renderText(data:String,start:Int,end:Int,canvas: Canvas,p:Paint,pdfDocument: PdfDocument,pgNo:Int) {
//
//        val pageWidth = 595
//        val pageHeight = 842
//
//        var xPosition = 0f
//        var yPosition = 50f
//        var strBUilder = StringBuilder()
//
//
//
//    }
//
//    fun renderEq(eqData:String,canvas: Canvas) {
//
//        xPosition = 0f
//        yPosition+=35f
//
//        val drawable = JLatexMathDrawable.builder(eqData)
//            .textSize(20f)
//            .build()
//
//// Create bitmap
//        val bitmap = Bitmap.createBitmap(
//            drawable.intrinsicWidth,
//            drawable.intrinsicHeight,
//            Bitmap.Config.ARGB_8888
//        )
//        val mCanvas = Canvas(bitmap)
//        //drawable.setBounds(0, 0, canvas.width, canvas.height)
//        drawable.draw(mCanvas)
//
//        canvas.drawBitmap(bitmap,xPosition,yPosition,null)
//
//        xPosition = 0f
//        yPosition+=drawable.intrinsicHeight+30
//
//    }

    //    fun generatePDF3(c: Context, pair: Pair<String, StringBuilder>): String {
//
//        val cleanData = pair.second.toString()
//            .replace(Regex("""\*+|#+"""), "")
//            .replace("`", "")
//            .trim()
//
//        val blocks = parseBlocks(cleanData)
//
//        val pdf = PdfDocument()
//        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            textSize = 20f
//            color = Color.BLACK
//        }
//
//        val tableBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = Color.BLACK
//            style = Paint.Style.STROKE
//            strokeWidth = 2f
//        }
//
//        val pageWidth = getScreenWidth(c)
//        val pageHeight = getScreenHeight(c)
//
//        val startX = pageWidth * 0.20f
//        val maxX = pageWidth - startX
//
//        var pageNumber = 1
//        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
//        var page = pdf.startPage(pageInfo)
//
//        val state = RenderState(
//            page = page,
//            canvas = page.canvas,
//            pageNumber = pageNumber,
//            x = startX,
//            y = 50f,
//            startX = startX,
//            maxX = maxX
//        )
//
//        for (block in blocks) {
//
//            when (block) {
//
//                is PdfBlock.Text -> {
//                    block.text.lines().forEach { line ->
//                        val words = line.split(Regex("\\s+"))
//                        for (w in words) {
//                            val width = paint.measureText("$w ")
//                            if (state.x + width > state.maxX) {
//                                state.x = state.startX
//                                state.y += 25f
//                            }
//                            ensurePage(pdf, pageInfo, pageHeight, state)
//                            state.canvas.drawText("$w ", state.x, state.y, paint)
//                            state.x += width
//                        }
//                        state.x = state.startX
//                        state.y += 25f
//                    }
//                }
//
//                is PdfBlock.Equation -> {
//                    ensurePage(pdf, pageInfo, pageHeight, state)
//                    val drawable = JLatexMathDrawable.builder(block.latex)
//                        .textSize(22f)
//                        .build()
//
//                    val bitmap = Bitmap.createBitmap(
//                        drawable.intrinsicWidth,
//                        drawable.intrinsicHeight,
//                        Bitmap.Config.ARGB_8888
//                    )
//                    val c2 = Canvas(bitmap)
//                    drawable.draw(c2)
//
//                    state.canvas.drawBitmap(bitmap, state.startX, state.y, null)
//                    state.y += drawable.intrinsicHeight + 30f
//                    state.x = state.startX
//                }
//
//                is PdfBlock.Table -> {
//                    val colCount = block.rows.first().size
//                    val cellWidth = (state.maxX - state.startX) / colCount
//                    val padding = 10f
//                    val rowHeight = paint.fontSpacing + padding * 2
//
//                    for (row in block.rows) {
//                        ensurePage(pdf, pageInfo, pageHeight, state)
//                        var x = state.startX
//                        row.forEach { cell ->
//                            state.canvas.drawRect(
//                                x,
//                                state.y,
//                                x + cellWidth,
//                                state.y + rowHeight,
//                                tableBorderPaint
//                            )
//                            state.canvas.drawText(
//                                cell,
//                                x + padding,
//                                state.y + padding + paint.textSize,
//                                paint
//                            )
//                            x += cellWidth
//                        }
//                        state.y += rowHeight
//                    }
//                    state.x = state.startX
//                }
//            }
//        }
//
//        pdf.finishPage(state.page)
//        return savePDF(c, pdf)
//    }

    data class RenderState(
        var page: Page,
        var canvas: Canvas,
        var pageNumber: Int,
        var x: Float,
        var y: Float,
        val startX: Float,
        val maxX: Float
    )

    public fun generatePdfNew1(c:Context,lStr:String):String {

        //var s = normalizeLatexDelimiters(pair.second.toString())

        //val qRegex = Regex("<sugq>(.*)</sugq>",RegexOption.DOT_MATCHES_ALL)

//        val qResult:MatchResult? = qRegex.find(s);
//
//        if (qResult!=null) {
//
//            var sugq:String = "**Suggested questions:**\n\n${qResult.value}"
//            sugq = sugq.replace("<sugq>","-")
//                .replace("</sugq>","")
//
//            s = s.replace(qRegex) {
//                sugq
//            }
//
//        }

        val s = lStr
//
//        s = s.replace(Regex("<mode>(.)*</mode>"),"")

        var regex:Regex = Regex("""\*+|###""",RegexOption.DOT_MATCHES_ALL)

        var headerRegex = Regex("^`+\\w+",RegexOption.MULTILINE)

        val data:String = s.replace(regex,"")
            .replace(headerRegex,"")
            .replace("`","")
            .replace("-","")
            .trim()

        val pdfDocument = PdfDocument()

        val p = Paint()
        p!!.isAntiAlias = true
        p!!.textSize = 20f
        p!!.color = Color.BLACK


//        val pageWidth = getScreenWidth(c)
//        val pageWidth = 842f
        val pagewidth = getScreenWidth(c)
        val pageHeight = getScreenHeight(c)

//        var xPosition = 295f
//        var yPosition = 25f
        var pageNumber = 1

        var startX = pagewidth*0.20f
        val x1 = startX
        val pLimit = pagewidth-startX
        var startY = 50f
        var pWidth = pLimit-x1

        var pageInfo = PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas



        val dReg = Regex("""\$\$(.*?)\$\$|\$(.*?)\$|\|(.)+\|""",RegexOption.DOT_MATCHES_ALL)

        var startIndex = 0
//
//        val lines = pair.first.split("\n")
//
//        for (l in lines) {
//
//            val words = pair.first.split(" ")
//
//            for (w in words) {
//
//                val wLength = p.measureText("$w ")
//
//                if (startX+wLength>=pLimit) {
//
//                    startX = x1
//                    startY += 25f
//
//                }
//
//                if (startY>pageHeight-25) {
//
//                    pdfDocument.finishPage(page)
//                    pageNumber++
//                    pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                    page = pdfDocument.startPage(pageInfo)
//                    canvas = page.canvas
//                    startX = x1
//                    startY = 50f
//
//                }
//
//                canvas.drawText("$w ", startX, startY, p!!)
//                startX+=wLength
//
//            }
//
//        }
//
//        startX = x1;
//        startY+=50

        var pdfModel:PdfModel = PdfModel(data, pageNumber , PointF(startX,startY))

        var m:MatchResult? = dReg.find(data)

        if (m!=null) {

            while (m!=null) {

                val match = m.value
                val tValue = m.groupValues[1]

                Log.d(Utils::class.java.name, "Latex "+tValue)

                Log.d(Utils::class.java.name, "Matches "+match)

                if (match.contains(Regex("\\|(.)+\\|",RegexOption.DOT_MATCHES_ALL))) {

                    Log.d(Utils::class.java.name, "generatePdfNew: "+m.value)
                    Log.d(Utils::class.java.name, "generatePdfNew: "+m.value)

                    val index = data.indexOf(match,startIndex)

                    if (index!=-1) {

                        val text = data.substring(startIndex,index)

                        if (!text.equals("")) {

                            page = checkPageAndPrint(text,pdfModel,page,pdfDocument
                                ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

                            pdfModel.p.x = startX
                            pdfModel.p.y+=25f

                        }

                        page = renderTable(match,pdfDocument,page,pdfModel,pWidth,pLimit
                            ,pagewidth
                            ,pageHeight
                            ,startX,startY)

                        pdfModel.p.x = startX
                        pdfModel.p.y+=25f

                        startIndex = index+match.length

                    }

                    //m = m.next()

                }

                else if (match.contains(Regex("\\\$\\\$(.*?)\\\$\\\$|\\\$(.*?)\\\$"))) {

                    val index = data.indexOf(match,startIndex)

                    if (index!=-1) {

                        val text = data.substring(startIndex,index)

                        if (!text.equals("")) {

                            page = checkPageAndPrint(text,pdfModel,page,pdfDocument
                                ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

                        }

                        page = renderEquation(match,pdfDocument,page,pdfModel,pLimit
                            ,pagewidth
                            ,pageHeight,startX,startY,p)

                        startIndex = index+match.length

                    }

                }

                m = m.next()

            }

            val residualText = data.substring(startIndex)

            if (!residualText.equals("")) {

                page = checkPageAndPrint(residualText,pdfModel,page,pdfDocument
                    ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

            }

        } else {

            //normal render
            page = checkPageAndPrint(data,pdfModel,page,pdfDocument
                ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

        }


        pdfDocument.finishPage(page)

        return savePDF(c,pdfDocument)


        //normal render/residual data

    }

    public fun generatePdfNew1(c:Context,pair:Pair<String,StringBuilder>):String {

        var s = normalizeLatexDelimiters(pair.second.toString())

        val qRegex = Regex("<sugq>(.*)</sugq>",RegexOption.DOT_MATCHES_ALL)

        val qResult:MatchResult? = qRegex.find(s);

        if (qResult!=null) {

            var sugq:String = "**Suggested questions:**\n\n${qResult.value}"
            sugq = sugq.replace("<sugq>","-")
                .replace("</sugq>","")

            s = s.replace(qRegex) {
                sugq
            }

        }

        s = s.replace(Regex("<mode>(.)*</mode>"),"")

        var regex:Regex = Regex("""\*{2,}|#{2,}""",RegexOption.DOT_MATCHES_ALL)

        var headerRegex = Regex("^`+\\w+",RegexOption.MULTILINE)

        var tableRegex = Regex("^\\|([|\\-]+)\\|",RegexOption.MULTILINE)

        val data:String = s.replace(regex,"")
            .replace(headerRegex,"")
            .replace("`","")
            .replace(tableRegex,"")
            .trim()

        Log.d(Utils::class.java.name, "Data: ${data}")

        val pdfDocument = PdfDocument()

        val p = Paint()
        p!!.isAntiAlias = true
        p!!.textSize = 20f
        p!!.color = Color.BLACK


//        val pageWidth = getScreenWidth(c)
//        val pageWidth = 842f
        val pagewidth = getScreenWidth(c)
        val pageHeight = getScreenHeight(c)

//        var xPosition = 295f
//        var yPosition = 25f
        var pageNumber = 1

        var startX = pagewidth*0.20f
        val x1 = startX
        val pLimit = pagewidth-startX
        var startY = 50f
        var pWidth = pLimit-x1

        var pageInfo = PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas



        val dReg = Regex("""\$\$(.*?)\$\$|\$(.*?)\$|\|(.)+\|""",RegexOption.DOT_MATCHES_ALL)

        var startIndex = 0

        val lines = pair.first.split("\n")

        for (l in lines) {

            val words = pair.first.split(" ")

            for (w in words) {

                val wLength = p.measureText("$w ")

                if (startX+wLength>=pLimit) {

                    startX = x1
                    startY += 25f

                }

                if (startY>pageHeight-25) {

                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    startX = x1
                    startY = 50f

                }

                canvas.drawText("$w ", startX, startY, p!!)
                startX+=wLength

            }

        }

        startX = x1;
        startY+=50

        var pdfModel:PdfModel = PdfModel(data, pageNumber , PointF(startX,startY))

        var m:MatchResult? = dReg.find(data)

        if (m!=null) {

            while (m!=null) {

                val match = m.value
                val tValue = m.groupValues[1]



                if (match.contains(Regex("\\|(.)+\\|",RegexOption.DOT_MATCHES_ALL))) {



                    val index = data.indexOf(match,startIndex)

                    if (index!=-1) {

                        val text = data.substring(startIndex,index)

                        if (!text.equals("")) {

                           page = checkPageAndPrint(text,pdfModel,page,pdfDocument
                               ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

                            pdfModel.p.x = startX
                            pdfModel.p.y+=25f

                        }

                        page = renderTable(match,pdfDocument,page,pdfModel,pWidth,pLimit
                            ,pagewidth
                            ,pageHeight
                            ,startX,startY)

                        pdfModel.p.x = startX
                        pdfModel.p.y+=25f

                        startIndex = index+match.length

                    }

                    //m = m.next()

                }

                else if (match.contains(Regex("\\\$\\\$(.*?)\\\$\\\$|\\\$(.*?)\\\$"))) {

                    val index = data.indexOf(match,startIndex)

                    if (index!=-1) {

                        val text = data.substring(startIndex,index)

                        if (!text.equals("")) {

                            page = checkPageAndPrint(text,pdfModel,page,pdfDocument
                                ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

                        }

                        page = renderEquation(match,pdfDocument,page,pdfModel,pLimit
                            ,pagewidth
                            ,pageHeight,startX,startY,p)

                        startIndex = index+match.length

                    }

                }

                m = m.next()

            }

            val residualText = data.substring(startIndex)

            if (!residualText.equals("")) {

                page = checkPageAndPrint(residualText,pdfModel,page,pdfDocument
                    ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

            }

        } else {

            //normal render
            page = checkPageAndPrint(data,pdfModel,page,pdfDocument
                ,p,pLimit,pageHeight-25f,pagewidth,pageHeight,startX,startY)

        }


        pdfDocument.finishPage(page)

        return savePDF(c,pdfDocument)


        //normal render/residual data

    }

    private fun checkPageAndPrint(text:String
                                  ,pdfModel: PdfModel
                                  ,page: Page
                                  ,pdfDocument: PdfDocument
                                  ,paint:Paint
                                  ,pLimit:Float
                                  ,maxHeight:Float
                                  ,pagewidth: Int
                                  ,pageHeight: Int
                                  ,startX:Float
                                  ,startY:Float):Page {

        var newPage:Page = page
        var canvas:Canvas=newPage.canvas

        val lines = text.split("\n")

        for (i in  lines.indices) {

            val words = lines[i].split(Regex("\\s+"))

            for (w in words) {

                val wLength = paint.measureText("$w ")

                if (pdfModel.p.x+wLength>=pLimit) {

                    pdfModel.p.x = startX
                    pdfModel.p.y+= 25f
                }

                if (pdfModel.p.y>=pageHeight-25) {

                    newPage = startNewPage(pdfModel,pdfDocument,newPage,pagewidth, pageHeight)
                    canvas = newPage.canvas

                    pdfModel.p.x = startX
                    pdfModel.p.y = startY

                }

                canvas.drawText("$w ",pdfModel.p.x,pdfModel.p.y,paint)
                pdfModel.p.x+=wLength

            }

            if (i!=lines.size-1) {

                pdfModel.p.x = startX
                pdfModel.p.y += 25

            }


        }

        return newPage

    }

    private fun startNewPage(pdfModel: PdfModel
                             ,pdfDocument: PdfDocument
                             ,page:Page
    ,pagewidth:Int,pageHeight:Int):Page {

        pdfDocument.finishPage(page)
        pdfModel.page++
//        pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
        val mPage = pdfDocument.startPage(PageInfo.Builder(pagewidth,pageHeight,pdfModel.page).create())

        return mPage



    }

    private fun renderTable(match:String,pdfDocument: PdfDocument,page: Page
                            ,pdfModel: PdfModel,pWidth:Float,pLimit: Float,pagewidth: Int,pageHeight: Int,
                            startX: Float,startY: Float
    ) : Page{

        var mPage = page
        var canvas = page.canvas


        val lines = match.split("\n")

        for (l in lines) {

            Log.d(Utils::class.java.name, "renderTable: ${l}")

            if (l.isNotBlank()) {

                val rList = l.split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }


                val paint = Paint().apply {

                    isAntiAlias = true
                    color = Color.BLACK
                    textSize = 16f
                    style = Paint.Style.STROKE

                }

//            val textPaint = Paint().apply {
//
//                isAntiAlias = true
//                color = Color.BLACK
//                textSize = 10f
//                style = Paint.Style.STROKE
//
//            }

                val cellWidth = pWidth/rList.size   // fixed width for all columns
                val padding = 10f
                val lineSpacing = paint.fontSpacing

                val lineCounts = rList.map { wrapText(it, cellWidth, paint).size }
                val maxLines = lineCounts.maxOrNull() ?: 1
                val rowHeight = maxLines * lineSpacing + padding * 2

                var x = pdfModel.p.x
                var y = pdfModel.p.y

                for (text in rList) {

                    if (y+rowHeight>=pageHeight-25) {

                        pdfDocument.finishPage(mPage)
                        pdfModel.page++
                        mPage = pdfDocument.startPage(PageInfo.Builder(pagewidth,pageHeight,pdfModel.page).create())
                        canvas = mPage.canvas

                        pdfModel.p.x = startX
                        pdfModel.p.y = startY

                        x = startX
                        y = startY

                    }



                    // Draw cell rectangle
                    canvas!!.drawRect(x, y, x+cellWidth, y + rowHeight, paint)

                    // Draw wrapped text
                    val mlines = wrapText(text, cellWidth, paint)
                    //paint.style = Paint.Style.FILL

                    for (line in mlines) {

                        if (line.contains(Regex("\\\$\\\$(.*?)\\\$\\\$|\\\$(.*?)\\\$"))) {

                            val drawable = JLatexMathDrawable.builder(line)
                                .textSize(16f)
                                .build()

                            val bitmap = Bitmap.createBitmap(
                                drawable.intrinsicWidth,
                                drawable.intrinsicHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            val mCanvas = Canvas(bitmap)
                            //drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(mCanvas)

                            canvas.drawBitmap(bitmap,x+padding,y+lineSpacing,null)


                        } else {

                            canvas.drawText(line, x + padding, y+lineSpacing, paint)
                            y += lineSpacing

                        }

                    }

                    x += cellWidth
                    y = pdfModel.p.y
//                pdfModel.p.y = startY
                }

                pdfModel.p.x = startX
                pdfModel.p.y+=rowHeight

            }
            }
        return mPage

    }

    private fun renderEquation(match:String,pdfDocument: PdfDocument,page: Page
                               ,pdfModel: PdfModel,pLimit: Float,pagewidth: Int,pageHeight: Int,
                               startX: Float,startY: Float,p:Paint) : Page {

        var mPage = page
        var canvas = mPage.canvas

        val drawable = JLatexMathDrawable.builder(match)
            .textSize(16f)
            .build()



        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)


        var x = pdfModel.p.x
        var y = pdfModel.p.y

        if (match.startsWith("$$")) {

            x = startX
            y+=25f


            if (y+drawable.intrinsicHeight>=pageHeight-25f) {

                mPage = startNewPage(pdfModel,pdfDocument, mPage, pagewidth, pageHeight)

                canvas = mPage.canvas

                x = startX
                y = startY

                pdfModel.p.x = startX
                pdfModel.p.y = startY



            }

            val mCanvas = Canvas(bitmap)
            drawable.draw(mCanvas)

            canvas.drawBitmap(bitmap,x,y,null)

            x = startX
            y+=drawable.intrinsicHeight+25f


        } else {

            if (x+drawable.intrinsicWidth>=pLimit) {

                x = startX
                y+=25

            }

            val mCanvas = Canvas(bitmap)
            drawable.draw(mCanvas)

            val fm =p.fontMetrics

            val textCenter = y + (fm.ascent + fm.descent) / 2f
            val centeredY = textCenter - drawable.intrinsicHeight / 2f

            canvas.drawBitmap(bitmap,x,centeredY,null)

            x+=drawable.intrinsicWidth


        }

        pdfModel.p.x = x
        pdfModel.p.y = y

        return mPage


    }


    //    public fun generatePDF3(c:Context,d:String):String? {
//
//        var regex:Regex = Regex("""\*+|#+""",RegexOption.DOT_MATCHES_ALL)
//
//        var headerRegex = Regex("^`+\\w+",RegexOption.MULTILINE)
//
//        val data:String = d.replace(regex,"").replace(headerRegex,"").replace("```","")
//
//        val pdfDocument = PdfDocument()
//
//        val p = Paint()
//        p!!.isAntiAlias = true
//        p!!.textSize = 20f
//        p!!.color = Color.BLACK
//
//        val titlePaint = Paint()
//
////        val pageWidth = getScreenWidth(c)
////        val pageWidth = 842f
//        val pagewidth = getScreenWidth(c)
//        val pageHeight = getScreenHeight(c)
//
////        var xPosition = 295f
////        var yPosition = 25f
//        var pageNumber = 1
//
//        var startX = pagewidth*0.20f
//        var x1 = startX
//        var pLimit = pagewidth-startX
//        var startY = 100f
//        var pWidth = pLimit-x1
//
//        var pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//        var page = pdfDocument.startPage(pageInfo)
//        var canvas = page.canvas
//
//        val dReg = Regex("""\$\$(.*?)\$\$|\$(.*?)\$|\|(.)+\|""",RegexOption.DOT_MATCHES_ALL)
//
//        var startIndex = 0
//
//        var index = 0
//        var endIndex = 0
//        var eqData = ""
//        var strBUilder = StringBuilder()
//
//        var m: MatchResult? = dReg.find(data)
//
//        if (m!=null) {
//
//            while (m != null) {
//                println("Found: ${m.value}")
//
//                eqData = m.value
//
//                if (eqData.contains("$")) {
//
//                    //math
//
//                    index = data.indexOf(m.value,startIndex)
//
//                    var textData = data.substring(startIndex,index)
//
//                    if (!textData.equals("")) {
//
//                        val lines = textData.split("\n")
//
//                        for (l in lines) {
//
//                            val words = l.split(Regex("\\s+"))
//
//                            for (w in words) {
//
//                                if (startX == x1) {
//
//                                    if (startY>pageHeight-25) {
//
//                                        pdfDocument.finishPage(page)
//                                        pageNumber++
//                                        pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                        page = pdfDocument.startPage(pageInfo)
//                                        canvas = page.canvas
//                                        startY = 100f
//
//                                    }
//
//                                    canvas.drawText("$w ", startX, startY, p!!)
//                                    startX+=p!!.measureText("$w ")
//                                    strBUilder.append("$w ")
//
//
//
//                                } else {
//
//                                    var x = p!!.measureText(strBUilder.toString())
//                                    var wLength = p!!.measureText("$w ")
//
//                                    if (startX + wLength >= pLimit) {
//
//
//                                        startX = x1
//                                        startY += 25f
//                                        strBUilder.clear()
//
//                                        if (startY>pageHeight-25) {
//
//                                            pdfDocument.finishPage(page)
//                                            pageNumber++
//                                            pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                            page = pdfDocument.startPage(pageInfo)
//                                            canvas = page.canvas
//                                            startX = x1
//                                            startY = 100f
//
//                                        }
//
//                                        canvas.drawText("$w ", startX, startY, p!!)
//                                        startX+=wLength
//                                        strBUilder.append("$w ")
//
//                                    } else {
//
//                                        if (startY>pageHeight-25) {
//
//                                            pdfDocument.finishPage(page)
//                                            pageNumber++
//                                            pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                            page = pdfDocument.startPage(pageInfo)
//                                            canvas = page.canvas
//                                            startX = x1
//                                            startY = 100f
//
//                                        }
//
//                                        canvas.drawText("$w ", startX, startY, p!!)
//                                        startX+=wLength
//                                        strBUilder.append("$w ")
//
//                                    }
//
//                                }
//
//                            }
//
//                            startX = x1
//                            startY+=25f
//                        }
//
//                    }
//
////render equation
//                    startX = x1
//                    //yPosition+=25f
//
//                    if (startY>pageHeight-25) {
//
//                        pdfDocument.finishPage(page)
//                        pageNumber++
//                        pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                        page = pdfDocument.startPage(pageInfo)
//                        canvas = page.canvas
//                        startX = x1
//                        startY = 100f
//
//                    }
//
//                    val drawable = JLatexMathDrawable.builder(eqData)
//                        .textSize(20f)
//                        .build()
//
//// Create bitmap
//                    val bitmap = Bitmap.createBitmap(
//                        drawable.intrinsicWidth,
//                        drawable.intrinsicHeight,
//                        Bitmap.Config.ARGB_8888
//                    )
//                    val mCanvas = Canvas(bitmap)
//                    //drawable.setBounds(0, 0, canvas.width, canvas.height)
//                    drawable.draw(mCanvas)
//
//                    canvas.drawBitmap(bitmap,startX,startY,null)
//
//                    startX = x1
//                    startY+=drawable.intrinsicHeight+25
//
//                    startIndex = index+eqData.length
//
//                    m = m.next() // Move to the next match
//
//                } else if (eqData.contains("|")) {
//
//                    index = data.indexOf(eqData,startIndex)
//                    var textData = data.substring(startIndex,index)
//
//                    if (!textData.equals("")) {
//
//                        val lines = textData.split("\n")
//
//                        for (l in lines) {
//
//                            val words = l.split(Regex("\\s+"))
//
//                            for (w in words) {
//
//                                if (startX == x1) {
//
//                                    if (startY>pageHeight-25) {
//
//                                        pdfDocument.finishPage(page)
//                                        pageNumber++
//                                        pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                        page = pdfDocument.startPage(pageInfo)
//                                        canvas = page.canvas
//                                        startY = 100f
//
//                                    }
//
//                                    canvas.drawText("$w ", startX, startY, p!!)
//                                    startX+=p!!.measureText("$w ")
//                                    strBUilder.append("$w ")
//
//
//
//                                } else {
//
//                                    //var x = p!!.measureText(strBUilder.toString())
//                                    var wLength = p!!.measureText("$w ")
//
//                                    if (startX + wLength >= pLimit) {
//
//
//                                        startX = x1
//                                        startY += 25f
//                                        strBUilder.clear()
//
//                                        if (startY>pageHeight-25) {
//
//                                            pdfDocument.finishPage(page)
//                                            pageNumber++
//                                            pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                            page = pdfDocument.startPage(pageInfo)
//                                            canvas = page.canvas
//                                            startX = x1
//                                            startY = 100f
//
//                                        }
//
//                                        canvas.drawText("$w ", startX, startY, p!!)
//                                        startX+=wLength
//                                        strBUilder.append("$w ")
//
//                                    } else {
//
//                                        if (startY>pageHeight-25) {
//
//                                            pdfDocument.finishPage(page)
//                                            pageNumber++
//                                            pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                            page = pdfDocument.startPage(pageInfo)
//                                            canvas = page.canvas
//                                            startX = x1
//                                            startY = 100f
//
//                                        }
//
//                                        canvas.drawText("$w ", startX, startY, p!!)
//                                        startX+=wLength
//                                        strBUilder.append("$w ")
//
//                                    }
//
//                                }
//
//                            }
//
//                            startX = x1
//                            startY+=25f
//                        }
//
//                    }
//
//                    startX = x1
//
//                    Log.d(Utils::class.java.name, "generatePDF2: ${eqData}")
//
//
//                    //textData = eqData
//
//                    if (!eqData.equals("")) {
//
//                        val lines = eqData.split("\n")
//
//                        for (l in lines) {
//
//                            val r:Regex = Regex("\\|(.*?)\\|",RegexOption.DOT_MATCHES_ALL)
//
//                            var m1: MatchResult? = r.find(l)
//
//                            if (m1!=null) {
//
//                                //val rList = ArrayList<String>()
//
//                                val rList = l.split("|")
//                                    .map { it.trim() }
//                                    .filter { it.isNotEmpty() }
//
//
//                                val paint = Paint().apply {
//                                    color = Color.BLACK
//                                    textSize = 14f
//                                    style = Paint.Style.STROKE
//                                }
//
//                                val cellWidth = pWidth/rList.size   // fixed width for all columns
//                                val padding = 10f
//                                val lineSpacing = paint.fontSpacing
//
//                                val lineCounts = rList.map { wrapText(it, cellWidth, paint).size }
//                                val maxLines = lineCounts.maxOrNull() ?: 1
//                                val rowHeight = maxLines * lineSpacing + padding * 2
//
//                                var x = startX
//                                for (text in rList) {
//
//                                    if (startY+rowHeight>pageHeight-25) {
//
//                                        pdfDocument.finishPage(page)
//                                        pageNumber++
//                                        pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                        page = pdfDocument.startPage(pageInfo)
//                                        canvas = page!!.canvas
//                                        //yPosition = 25f
//
//                                        x = startX
//                                        startY = 100f
//
//                                    }
//                                    // Draw cell rectangle
//                                    canvas!!.drawRect(x, startY, x + cellWidth, startY + rowHeight, paint)
//
//                                    // Draw wrapped text
//                                    val lines = wrapText(text, cellWidth, paint)
//                                    paint.style = Paint.Style.FILL
//                                    var textY = startY + padding + paint.textSize
//                                    for (line in lines) {
//                                        canvas!!.drawText(line, x + padding, textY, paint)
//                                        textY += lineSpacing
//                                    }
//                                    paint.style = Paint.Style.STROKE
//                                    x += cellWidth
//                                }
//
//                                startY+=rowHeight
//                                Log.d(SampleActivity::class.java.name, "onCreate: ${rList.toString()}")
//
//                            }
//
//                        }
//
//                    }
//
//                    startX = x1
//
//
//                    startIndex = index+eqData.length
//                    m = m.next()
//
//                    //table
//                }
//
//                Log.d("abcde", "onDraw: "+eqData)
//
//
//            }
//
//            var textData = data.substring(startIndex,data.length)
//
//            if (!textData.equals("")) {
//
//                val lines = textData.split("\n")
//
//                for (l in lines) {
//
//                    val words = l.split(" ")
//
//                    for (w in words) {
//
//                        if (startX == x1) {
//
//                            if (startY>pageHeight-25) {
//
//                                pdfDocument.finishPage(page)
//                                pageNumber++
//                                pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                page = pdfDocument.startPage(pageInfo)
//                                canvas = page.canvas
//                                startY = 100f
//
//                            }
//
//                            canvas.drawText("$w ", startX, startY, p!!)
//                            startX+=p!!.measureText("$w ")
//                            strBUilder.append("$w ")
//
//
//
//                        } else {
//
//                            var x = startX+ p!!.measureText(strBUilder.toString())
//                            var wLength = p!!.measureText("$w ")
//
//                            if (startX + wLength >= pLimit) {
//
//                                startX = x1
//                                startY += 25f
//                                strBUilder.clear()
//
//                                if (startY>pageHeight-25) {
//
//                                    pdfDocument.finishPage(page)
//                                    pageNumber++
//                                    pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                    page = pdfDocument.startPage(pageInfo)
//                                    canvas = page.canvas
//                                    startX = x1
//                                    startY = 100f
//
//                                }
//
//                                canvas.drawText("$w ", startX, startY, p!!)
//                                startX+=wLength
//                                strBUilder.append("$w ")
//
//                            } else {
//
//                                if (startY>pageHeight-25) {
//
//                                    pdfDocument.finishPage(page)
//                                    pageNumber++
//                                    pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                    page = pdfDocument.startPage(pageInfo)
//                                    canvas = page.canvas
//                                    startX = x1
//                                    startY = 100f
//
//                                }
//
//                                canvas.drawText("$w ", startX, startY, p!!)
//                                startX+=wLength
//                                strBUilder.append("$w ")
//
//                            }
//
//                        }
//
//                    }
//
//                    startX = x1
//                    startY+=25f
//                }
//
//            }
//
////
//
//        } else {
//
//            if (!data.equals("")) {
//
//                val lines = data.split("\n")
//
//                for (l in lines) {
//
//                    val words = l.split(" ")
//
//                    for (ww in words) {
//
//                        Log.d(SampleTextView::class.java.name, "Word "+ww)
//                    }
//
//
//                    for (w in words) {
//
//                        if (startX == x1) {
//
//                            if (startY>pageHeight-25) {
//
//                                pdfDocument.finishPage(page)
//                                pageNumber++
//                                pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                page = pdfDocument.startPage(pageInfo)
//                                canvas = page.canvas
//                                startY = 100f
//
//                            }
//
//                            canvas.drawText("$w ", startX, startY, p!!)
//                            startX+=p!!.measureText("$w ")
//                            strBUilder.append("$w ")
//
//
//
//                        } else {
//
//                            var x = p!!.measureText(strBUilder.toString())
//                            var wLength = p!!.measureText("$w ")
//
//                            if (startX + wLength >= pLimit) {
//
//
//                                startX = x1
//                                startY += 25f
//                                strBUilder.clear()
//
//                                if (startY>pageHeight-25) {
//
//                                    pdfDocument.finishPage(page)
//                                    pageNumber++
//                                    pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                    page = pdfDocument.startPage(pageInfo)
//                                    canvas = page.canvas
//                                    startX = x1
//                                    startY = 100f
//
//                                }
//
//                                canvas.drawText("$w ", startX, startY, p!!)
//                                startX+=wLength
//                                strBUilder.append("$w ")
//
//                            } else {
//
//                                if (startY>pageHeight-25) {
//
//                                    pdfDocument.finishPage(page)
//                                    pageNumber++
//                                    pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//                                    page = pdfDocument.startPage(pageInfo)
//                                    canvas = page.canvas
//                                    startX = x1
//                                    startY = 100f
//
//                                }
//
//                                canvas.drawText("$w ", startX, startY, p!!)
//                                startX+=wLength
//                                strBUilder.append("$w ")
//
//                            }
//
//                        }
//
//                    }
//
//                    startX = x1
//                    startY+=25f
//                }
//
//            }
//        }
//
//        pdfDocument.finishPage(page)
//
//        return savePDF(c,pdfDocument)
//
////        return null
//    }

    fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val padding = 10f
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else currentLine.toString() + " " + word
            if (paint.measureText(testLine) > maxWidth - 2 * padding) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    fun savePDF(c:Context,pdfDocument: PdfDocument):String {

        val dir = c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        val fileName:String = SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(Date(System.currentTimeMillis()))+".pdf"

        val file = File(dir,fileName)

        try {

            pdfDocument.writeTo(FileOutputStream(file))

            Handler(Looper.getMainLooper()).post {

                Toast.makeText(c,"Pdf Saved",Toast.LENGTH_SHORT).show()
            }


        } catch (ex:Exception) {

            Log.d(Utils::class.java.name, "Error: ${ex.message}")

            ex.printStackTrace()

            Handler(Looper.getMainLooper()).post {

                Toast.makeText(c,"Error generating pdf",Toast.LENGTH_SHORT).show()
            }

        }

        pdfDocument.close()

        return file.absolutePath

    }

    fun enterFullScreenMode(window: Window) {


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

    fun normalizeLatexDelimiters(input: String): String {

            return input
                .replace(
                    Regex("""\\\[\s*(.*?)\s*\\\]""", RegexOption.DOT_MATCHES_ALL)
                ) {
                    "$$ ${it.groupValues[1]} $$"
                }
                .replace(
                    Regex("""\\\(\s*(.*?)\s*\\\)""", RegexOption.DOT_MATCHES_ALL)
                ) {
                    "$ ${it.groupValues[1]} $"
                }


    }
 }
