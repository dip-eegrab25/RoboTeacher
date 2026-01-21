package com.ai.roboteacher

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorStateListDrawable
import android.graphics.drawable.Drawable
import android.hardware.input.InputManager
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat

class ChatEditTextViewGeneral:AppCompatEditText {

    private lateinit var c:Context
    private var rightDrawable:Drawable?=null
    private var leftDrawableBounds:Rect = Rect()
    private var drawableBounds:Rect = Rect()
    private var drawableClickListener:OnDrawableClickListener?=null
    private var isStop = false
    var isLeftSelected:Boolean = false
    var isRightSelected:Boolean = false

    fun setOnDrawableClickListener(drawableClickListener: OnDrawableClickListener) {

        this.drawableClickListener = drawableClickListener

    }

    private constructor(c: Context) : super(c) {

        this.c = c


    }

    constructor(c: Context, a: AttributeSet) : super(c,a) {

        this.c = c

        Handler().post {

            setDefaultState()

//            setRightDrawable(ContextCompat.getDrawable(c,R.drawable.chat_start_stop_toggle))
            //setDrawables(ContextCompat.getDrawable(c,R.drawable.chat_start_stop_toggle))

        }

        //init()

    }

    override fun setCompoundDrawables(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        super.setCompoundDrawables(left, top, right, bottom)

//        right?.let {
//            if (it.bounds.isEmpty) {
//                it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
//            }
//
//            rightDrawable = it
//        }


    }

//    override fun getCompoundDrawables(): Array<Drawable> {
//        return super.getCompoundDrawables()
//    }

    constructor(c: Context, a: AttributeSet, defStyle:Int) : super(c,a,defStyle) {

        this.c = c

        //init()

    }

    var x = 0
    var y = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {


        when(event?.action) {

            MotionEvent.ACTION_DOWN->{

                x = event.x.toInt()
                y = event.y.toInt()

                if (rightDrawable!=null && leftDrawable!=null) {

                    drawableBounds.right = width
                    drawableBounds.left = width- rightDrawable?.bounds?.width()!!
                    drawableBounds.top = 0
                    drawableBounds.bottom = height

                    leftDrawableBounds.left = 0
                    leftDrawableBounds.top = 0
                    leftDrawableBounds.right = leftDrawable?.bounds?.width()!!
                    leftDrawableBounds.bottom = height


                } else {

                    Log.d(ChatEditTextView::class.java.name, "Null")
                }

                return true

            }

            MotionEvent.ACTION_UP->{


//                if (isSelected) {

//                    if (drawableBounds.contains(x,y)) {
//
//                        isSelected = !isSelected
//
//                        drawableClickListener?.onClick(DrawablePosition.RIGHT)
//
//                        return true
//
//                    } else if (leftDrawableBounds.contains(x,y)) {
//
//                        isSelected = !isSelected
//
//                        drawableClickListener?.onClick(DrawablePosition.LEFT)
//
//                        return true
//
//                    } else {
//
//                        drawableClickListener?.onClick(DrawablePosition.OTHER)
//                        return super.onTouchEvent(event)
//
//
//                    }
                //}


                if (drawableBounds.contains(x,y)) {

                    Log.d(ChatEditTextView::class.java.name, "onTouchEventRight: ")

                    if (!isLeftSelected) {

                        Log.d(ChatEditTextView::class.java.name, "onTouchEventRight(no left): ")

                        if (isSelected) {

                            isSelected = false
                            drawableClickListener?.onClick(DrawablePosition.RIGHT)
                            setDefaultState()



                        } else {

                            isSelected = true
                            isRightSelected = true
                            isLeftSelected = false
                            leftDrawable = ContextCompat.getDrawable(c, R.drawable.mic_no_selector)
                            leftDrawable?.setBounds(
                                0,
                                0,
                                leftDrawable!!.intrinsicWidth,
                                leftDrawable!!.intrinsicHeight
                            )
                            setCompoundDrawables(leftDrawable, null, rightDrawable, null)
                            drawableClickListener?.onClick(DrawablePosition.RIGHT)

                        }
                    }



                    return true

                } else if (leftDrawableBounds.contains(x,y)) {

                    if (!isRightSelected) {


                        if (isSelected) {

                            isSelected = false
                            drawableClickListener?.onClick(DrawablePosition.LEFT)
                            setDefaultState()


                        } else {

                            isSelected = true
                            isLeftSelected = true
                            isRightSelected = false
                            rightDrawable =
                                ContextCompat.getDrawable(c, R.drawable.send_drawable_no_selector)
                            rightDrawable?.setBounds(
                                0,
                                0,
                                rightDrawable!!.intrinsicWidth,
                                rightDrawable!!.intrinsicHeight
                            )
                            setCompoundDrawables(leftDrawable, null, rightDrawable, null)
                            drawableClickListener?.onClick(DrawablePosition.LEFT)

                        }
                    }



                } else {

                    drawableClickListener?.onClick(DrawablePosition.OTHER)

//                    requestFocus()
//                    showKeyboardFocus(true)

                    //return super.onTouchEvent(event)
                }



                //click here
            }
        }

        return false

    }

    var leftDrawable:Drawable?=null

    fun setDrawables(drawable: Drawable?) {

        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        setCompoundDrawables(drawable, null, drawable, null)
        rightDrawable = drawable
        leftDrawable = drawable



    }

//    fun setRightDrawable(drawable: Drawable?) {
//        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
//        setCompoundDrawables(null, null, drawable, null)
//        rightDrawable = drawable
//    }
//
//    fun setLeftDrawable(drawable: Drawable?) {
//        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
//        setCompoundDrawables(null, null, drawable, null)
//        rightDrawable = drawable
//    }

    private fun showKeyboardFocus(visible:Boolean) {

        val imm:InputMethodManager = c.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (visible) {

            imm.showSoftInput(this,InputMethodManager.SHOW_IMPLICIT)

            //imm = null

        }


    }

    fun setDefaultState() {

        isLeftSelected = false
        isRightSelected = false

        leftDrawable = ContextCompat.getDrawable(c,R.drawable.chat_start_stop_toggle)
        rightDrawable = ContextCompat.getDrawable(c,R.drawable.send_drawable_selector)

        leftDrawable?.setBounds(0, 0, leftDrawable!!.intrinsicWidth, leftDrawable!!.intrinsicHeight)
        rightDrawable?.setBounds(0, 0, rightDrawable!!.intrinsicWidth, rightDrawable!!.intrinsicHeight)
        setCompoundDrawables(leftDrawable, null, rightDrawable, null)


    }

//    fun setState(drawablePosition: DrawablePosition) {
//
//        if (drawablePosition == DrawablePosition.RIGHT) {
//
//
//        }
//
//        isLeftSelected = false
//        isRightSelected = false
//
//        leftDrawable = ContextCompat.getDrawable(c,R.drawable.chat_start_stop_toggle)
//        rightDrawable = ContextCompat.getDrawable(c,R.drawable.send_drawable_selector)
//
//        leftDrawable?.setBounds(0, 0, leftDrawable!!.intrinsicWidth, leftDrawable!!.intrinsicHeight)
//        rightDrawable?.setBounds(0, 0, rightDrawable!!.intrinsicWidth, rightDrawable!!.intrinsicHeight)
//        setCompoundDrawables(leftDrawable, null, rightDrawable, null)
//
//
//    }

    interface OnDrawableClickListener {

        fun onClick(position:DrawablePosition)
    }

    enum class DrawablePosition{

        RIGHT,LEFT,OTHER
    }
}