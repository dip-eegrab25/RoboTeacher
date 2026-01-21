package com.ai.roboteacher.miko

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.ai.roboteacher.R
import androidx.core.content.ContextCompat

class StarsView:View {

    var c:Context?=null
    var RADIUS = 10f
    var angleArr = floatArrayOf(270f,350f)
    var CENTER_X = 0f
    var CENTER_Y = 0f
    var vAnim:ValueAnimator?=null
    var rAnim:ValueAnimator?=null

    constructor(context: Context?) : super(context) {

        this.c = context
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        this.c = context
        init()

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        this.c = context
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

        this.c = context
        init()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var scFactor:Float = c!!.resources.displayMetrics.density

        RADIUS = RADIUS*scFactor

        var mWidth = 150*scFactor
        var mHeight = 150*scFactor

        var width = resolveSize(mWidth.toInt(),widthMeasureSpec)
        var height = resolveSize(mHeight.toInt(),heightMeasureSpec)

        setMeasuredDimension(width,height)
        scaleX = 1f
        scaleY = 1f

        CENTER_X = width/2f
        CENTER_Y = height/2f

        pivotX = CENTER_X
        pivotY = CENTER_Y
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var d:Bitmap = getBitmapFromVectorDrawable(R.drawable.star_yellow)
        canvas.drawBitmap(d,CENTER_X-(d.width/2f),CENTER_Y-(d.height/2f),null)



        for (i in 0 until angleArr.size) {

//            if (i == 0) {
//
//                RADIUS = 1f
//
//                var x:Float = CENTER_X + (RADIUS * Math.cos(Math.toRadians(angleArr.get(i).toDouble())).toFloat())
//                var y:Float = CENTER_Y + (RADIUS * Math.sin(Math.toRadians(angleArr.get(i).toDouble())).toFloat())
//
//                var d:Bitmap = getBitmapFromVectorDrawable(R.drawable.star_yellow)
//                canvas.drawBitmap(d,x,y,null)
//            }

            if (i == 0) {

                RADIUS = 25f

                var x:Float = CENTER_X + (RADIUS * Math.cos(Math.toRadians(angleArr.get(i).toDouble())).toFloat())
                var y:Float = CENTER_Y + (RADIUS * Math.sin(Math.toRadians(angleArr.get(i).toDouble())).toFloat())


                var d:Bitmap = getBitmapFromVectorDrawable(R.drawable.star_blue)
                canvas.drawBitmap(d,x,y,null)

            }

            if (i == 1) {

                RADIUS = 10f

                var x:Float = CENTER_X + (RADIUS * Math.cos(Math.toRadians(angleArr.get(i).toDouble())).toFloat())
                var y:Float = CENTER_Y + (RADIUS * Math.sin(Math.toRadians(angleArr.get(i).toDouble())).toFloat())



                var d:Bitmap = getBitmapFromVectorDrawable(R.drawable.star_red)
                canvas.drawBitmap(d,x,y,null)

            }
        }

    }

    fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(c!!, drawableId)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun init() {

//        mPaint = Paint()
//        mPaint!!.isAntiAlias = true
//        mPaint!!.style = Paint.Style.FILL
//        mPaint!!.color = Color.YELLOW

        vAnim = ValueAnimator.ofFloat(0f,1f)
        vAnim!!.duration = 1000
        vAnim!!.repeatCount = ValueAnimator.INFINITE
        vAnim!!.repeatMode = ValueAnimator.REVERSE
        vAnim!!.addUpdateListener {

            scaleX = it.animatedValue as Float
            scaleY = it.animatedValue as Float

        }

        rAnim = ValueAnimator.ofFloat(0f,360f)
        rAnim!!.duration = 1000
        rAnim!!.repeatCount = ValueAnimator.INFINITE
        rAnim!!.repeatMode = ValueAnimator.REVERSE
        rAnim!!.addUpdateListener {

            rotation = it.animatedValue as Float

            if (it.animatedValue as Float >= 359f) {

                scaleX = 0f
                scaleY = 0f

            }

//            scaleX = it.animatedValue as Float
//            scaleY = it.animatedValue as Float

        }
    }

    public fun startAnim() {

        vAnim!!.start()
        rAnim!!.start()

    }

    public fun stopAnim() {

        if (vAnim!!.isStarted) {

            vAnim!!.pause()
        }

        if (rAnim!!.isStarted) {

            rAnim!!.pause()
        }

//        vAnim!!.pause()
//        rAnim!!.pause()


    }



}