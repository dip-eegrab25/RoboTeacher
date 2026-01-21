package com.ai.roboteacher

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator

class MyValueAnimator:ValueAnimator,ValueAnimator.AnimatorUpdateListener,Animator.AnimatorPauseListener {

    lateinit var animListener:AnimListener




    constructor(vararg d:Float) {

        setFloatValues(*d)
        duration = 700
        interpolator = LinearInterpolator()
        repeatCount = INFINITE
        repeatMode = REVERSE
        addUpdateListener(this)
        addPauseListener(this)


        //this.animListener = animListener

    }

    fun setMyAnimListener(animListener: AnimListener) {

        this.animListener = animListener


    }

    override fun onAnimationUpdate(animation: ValueAnimator) {

        animListener.onValueReceived(animation.animatedValue as Float)
    }

    override fun onAnimationPause(animation: Animator) {

//        animListener.onPause(isProcessing)
//        isProcessing = !isProcessing

    }

    override fun onAnimationResume(animation: Animator) {


    }

    interface AnimListener {

        fun onValueReceived(value:Float)
        //fun onPause(isReset:Boolean)


    }
}