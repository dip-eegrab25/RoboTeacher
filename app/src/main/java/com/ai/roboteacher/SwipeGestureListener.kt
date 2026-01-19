package com.ai.roboteacher

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeGestureListener(private val onSwipe:(direction:Direction
                                                ,velocityX:Float
        ,velocityY:Float)->Unit,private val c:Context): GestureDetector.SimpleOnGestureListener() {

            var velocityThresHold = 100
    var swipeThresHold = 100
    val screenWidthThreshold = Utils.getScreenWidth(c)-Utils.getScreenWidth(c)*0.40f


    override fun onDown(e: MotionEvent): Boolean {
        return super.onDown(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        onSwipe(Direction.TAP_UP,0f,0f)
        return true
    }



//    override fun onSingleTapUp(e: MotionEvent): Boolean {
//
//        onSwipe(Direction.TAP_UP,0f,0f)
//        return true
//    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        if (e1 == null || e2 == null) {

            return false
        }

        if (e1.x>screenWidthThreshold) {

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (abs(diffX)> abs(diffY)) {

                if (abs(diffX)>swipeThresHold && abs(diffX)>velocityThresHold) {

                    if (e1.x>e2.x) {

                        onSwipe(Direction.LEFT,velocityX, velocityY)
                        return true

                    } else if (e2.x>e1.x) {

                        onSwipe(Direction.RIGHT,velocityX, velocityY)
                        return true

                    }

                    return false






                } else {

                    return false

                }
            }


        }



        return false


    }
}