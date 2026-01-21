package com.ai.roboteacher.activities


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ai.roboteacher.R
import com.ai.roboteacher.Utils
import java.io.File


class PdfViewActivity: AppCompatActivity(),ScaleGestureDetector.OnScaleGestureListener {

    private var imageView: ImageView? = null
    private var matrix: Matrix? = null
    private var scaleDetector: ScaleGestureDetector? = null

    private  var scaleFactor = 1f
    private  val minScale = 1f
    private  val maxScale = 5f
    private val fArray = floatArrayOf(0f,0f)

    private var width = 0
    private var height = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        imageView = findViewById<ImageView>(R.id.pdfImageView)

        imageView!!.post {

            imageView!!.setImageDrawable(resources.getDrawable(R.drawable.panda))

            matrix = imageView!!.matrix

            //matrix!!.postTranslate(120f,120f)

            //imageView!!.imageMatrix = matrix

            //matrix!!.postTranslate(0f,0f)

            //matrix!!.mapPoints(fArray)

            //imageView!!.matrix!!.mapPoints(fArray)

//             initialX = fArray[0]
//             initialY = fArray[1]

             width = imageView!!.width
             height = imageView!!.height

//            initialX = fArray[0]
//            initialY = fArray[1]

//            fArray[0] = initialX
//            fArray[1] = initialY

            Log.d(PdfViewActivity::class.java.name, "onCreate: "+fArray[0])
            Log.d(PdfViewActivity::class.java.name, "onCreate: "+width)

            Log.d(PdfViewActivity::class.java.name, "onCreate: "+fArray[1])
            Log.d(PdfViewActivity::class.java.name, "onCreate: "+height)

            Log.d(PdfViewActivity::class.java.name, "onCreate: "+Utils.getScreenHeight(applicationContext))




        }

        imageView!!.setImageDrawable(resources.getDrawable(R.drawable.panda))

        imageView!!.setOnTouchListener(object : OnTouchListener {
            var lastX: Float = 0f
            var lastY: Float = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                //scaleDetector!!.onTouchEvent(event)

                if (event.pointerCount == 1) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            lastX = event.x
                            lastY = event.y

//                            fArray[0] = initialX
//                            fArray[1] = initialY

                        }

                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.x - lastX
                            val dy = event.y - lastY

//                            fArray[0] = dx
//                            fArray[1] = dy



                            if (fArray[0]+width>=Utils.getScreenWidth(applicationContext)
                                && fArray[1]+height>=Utils.getScreenHeight(applicationContext)) {

                                return false
                            }

                            matrix!!.postTranslate(dx, dy)
                            matrix!!.mapPoints(fArray)

                            imageView!!.imageMatrix = matrix

                            lastX = event.x
                            lastY = event.y





                            Log.d(PdfViewActivity::class.java.name, "onTouch: "+fArray[0])

                        }

//                        MotionEvent.ACTION_MOVE -> {
//                            val dx = event.x - lastX
//                            val dy = event.y - lastY
//
//                            // Tentative translation
//                            val tempMatrix = Matrix(matrix)
//                            tempMatrix.postTranslate(dx, dy)
//
//                            val values = FloatArray(9)
//                            tempMatrix.getValues(values)
//
//                            val transX = values[Matrix.MTRANS_X]
//                            val transY = values[Matrix.MTRANS_Y]
//
//                            // Clamp boundaries
//                            val maxTransX = 0f
//                            val maxTransY = 0f
//                            val minTransX = Utils.getScreenWidth(applicationContext) - width.toFloat()
//                            val minTransY = Utils.getScreenHeight(applicationContext) - height.toFloat()
//
//                            val clampedX = transX.coerceIn(minTransX, maxTransX)
//                            val clampedY = transY.coerceIn(minTransY, maxTransY)
//
//                            matrix!!.postTranslate(clampedX, clampedY)
//                            imageView!!.imageMatrix = matrix
//
//                            lastX = event.x
//                            lastY = event.y
//                        }
                    }
                }
                return true
            }
        })

        scaleDetector = ScaleGestureDetector(this, this)

        loadPdfPage()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
       return scaleDetector!!.onTouchEvent(event!!);
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {

        val scale = detector.scaleFactor
        val newScale = scaleFactor * scale

        if (newScale >= minScale && newScale <= maxScale) {
            scaleFactor = newScale;
            matrix!!.postScale(scale, scale,
                detector.getFocusX(), detector.getFocusY());
            imageView!!.setImageMatrix(matrix);
        }
        return true;

    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {

        return true

    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {


    }

    var bitmap:Bitmap?=null

    private fun loadPdfPage() {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"09_01_2026_12_27_57.pdf")
            val fd =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

            val renderer = PdfRenderer(fd)
            val page = renderer.openPage(0)

            bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )



            page.render(
                bitmap!!, null, null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )

            imageView!!.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}