package com.ai.roboteacher.miko

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ai.roboteacher.R

class RoboExpressionView:ConstraintLayout {

    enum class Expressions{

        NEUTRAL,NEUTRAL_TALKING,HAPPY
        ,SAD,SAD_TALKING,ANGRY
        ,HAPPY_TALKING,SLEEP
        ,CONFUSED,LOVE
        ,LOVE_TALKING,WINK,CONFUSED_TALKING,ANGRY_TALKING
    }

    var c:Context?=null
    var roboEyeLeft:ImageView?=null
    var roboEyeRight:ImageView?=null
    var roboEyeBrowLeft:ImageView?=null
    var roboEyeBrowRight:ImageView?=null
    var roboLips:ImageView?=null
    var isTalking:Boolean=false
    var isTranslated = false
    var isSleeping = true
    var isLipsRotated = false
    

    var expressionObserver:Observer<Expressions>?=null
    var mutableLiveExpression:MutableLiveData<Expressions>?=null

    constructor(context: Context) : super(context) {

        this.c = context
        init()


    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        this.c = context
        init()

    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        this.c = context
        init()

    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

        this.c = context
        init()

    }

    private fun init() {

        roboEyeBrowLeft = ImageView(c).apply {

            id = generateViewId()
            setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_robo_eyebrow_left))
        }

        roboEyeBrowRight = ImageView(c).apply {

            id = generateViewId()
            setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_robo_eyebrow_right))
        }

        roboEyeLeft = ImageView(c).apply {

            id = generateViewId()
            setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
        }

        roboEyeRight = ImageView(c).apply {

            id = generateViewId()
            setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
        }

        roboLips = ImageView(c).apply {

            id = generateViewId()
            setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))
        }

        addView(roboEyeBrowLeft)
        addView(roboEyeBrowRight)
        addView(roboEyeLeft)
        addView(roboEyeRight)
        addView(roboLips)

        applyConstraints()
        setupObservers()
        startAnims()


    }

    private fun applyConstraints() {

        val set = ConstraintSet()
        set.clone(this)

        //----------Eye Brow Left-----------------

        set.connect(roboEyeBrowLeft!!.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP)
        set.connect(roboEyeBrowLeft!!.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM)
        set.connect(roboEyeBrowLeft!!.id,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START)
        set.connect(roboEyeBrowLeft!!.id,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END)

        set.setHorizontalBias(roboEyeBrowLeft!!.id,0.30f)
        set.setVerticalBias(roboEyeBrowLeft!!.id,0.10f)



        //----------Eye Brow Right-----------------

        set.connect(roboEyeBrowRight!!.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP)
        set.connect(roboEyeBrowRight!!.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM)
        set.connect(roboEyeBrowRight!!.id,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START)
        set.connect(roboEyeBrowRight!!.id,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END)

        set.setHorizontalBias(roboEyeBrowRight!!.id,0.71f)
        set.setVerticalBias(roboEyeBrowRight!!.id,0.10f)




        //----------Eye Left-----------------

        set.connect(roboEyeLeft!!.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP)
        set.connect(roboEyeLeft!!.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM)
        set.connect(roboEyeLeft!!.id,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START)
        set.connect(roboEyeLeft!!.id,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END)

        set.constrainWidth(roboEyeLeft!!.id, ConstraintSet.MATCH_CONSTRAINT)
        set.constrainHeight(roboEyeLeft!!.id, ConstraintSet.MATCH_CONSTRAINT)

        set.constrainPercentWidth(roboEyeLeft!!.id,0.14f)
        set.setDimensionRatio(roboEyeLeft!!.id,"1:1")

        set.setHorizontalBias(roboEyeLeft!!.id,0.30f)
        set.setVerticalBias(roboEyeLeft!!.id,0.20f)


        //----------Eye Right-----------------

        set.connect(roboEyeRight!!.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP)
        set.connect(roboEyeRight!!.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM)
        set.connect(roboEyeRight!!.id,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START)
        set.connect(roboEyeRight!!.id,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END)

        set.constrainWidth(roboEyeRight!!.id, ConstraintSet.MATCH_CONSTRAINT)
        set.constrainHeight(roboEyeRight!!.id, ConstraintSet.MATCH_CONSTRAINT)

        set.constrainPercentWidth(roboEyeRight!!.id,0.14f)
        set.setDimensionRatio(roboEyeRight!!.id,"1:1")

        set.setHorizontalBias(roboEyeRight!!.id,0.70f)
        set.setVerticalBias(roboEyeRight!!.id,0.20f)



        //----------Lips-----------------

        set.connect(roboLips!!.id,ConstraintSet.START,roboEyeLeft!!.id,ConstraintSet.START)
        set.connect(roboLips!!.id,ConstraintSet.END,roboEyeRight!!.id,ConstraintSet.END)
        set.connect(roboLips!!.id,ConstraintSet.TOP,roboEyeLeft!!.id,ConstraintSet.BOTTOM)
        set.connect(roboLips!!.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM)


//        set.constrainPercentWidth(roboLips!!.id,0.16f)
//        set.setDimensionRatio(roboLips!!.id,"1:1")

        //set.setHorizontalBias(roboLips!!.id,0.70f)
        set.setVerticalBias(roboLips!!.id,0.40f)

        set.applyTo(this)

    }

    private fun setupObservers() {

        expressionObserver = object : Observer<Expressions>{
            override fun onChanged(value: Expressions) {

                when (value) {

                    Expressions.HAPPY->{

                        isTalking = false


                        if (isTranslated) {

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                            isTranslated = false

                        }

                        roboEyeBrowLeft!!.animate().rotation(-10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()


                        if (isSleeping) {

                            isSleeping = false

                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))

                        roboEyeLeft!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .start()

                        roboEyeRight!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }



                    }

                    Expressions.HAPPY_TALKING->{

                        isTalking = true


                        roboEyeBrowLeft!!.animate().rotation(-10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (!isTranslated) {

                            roboEyeBrowLeft!!.animate().translationYBy(30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(30f).start()
                            isTranslated = true

                        }

                        if (isSleeping) {

                            isSleeping = false


                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))


                        roboEyeLeft!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .start()

                        roboEyeRight!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }


                        startLipAnim()

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }



                    }

                    Expressions.NEUTRAL->{

                        isTalking = false


                        roboEyeBrowLeft!!.animate().rotation(0f).start()
                        roboEyeBrowRight!!.animate().rotation(0f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        if (isSleeping) {

                            isSleeping = false

                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))

                        roboEyeLeft!!.animate().scaleX(1f)
                            .scaleY(1f)
                            .start()

                        roboEyeRight!!.animate().scaleX(1f)
                            .scaleY(1f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        roboLips!!.animate().scaleX(1f).start()
                        roboLips!!.animate().scaleY(0.89116f).start()

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }



                    }



                    Expressions.NEUTRAL_TALKING->{

                        isTalking = false


                        roboEyeBrowLeft!!.animate().rotation(0f).start()
                        roboEyeBrowRight!!.animate().rotation(0f).start()


                        if (isSleeping) {

                            isSleeping = false

                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))

                        roboEyeLeft!!.animate().scaleX(1f)
                            .scaleY(1f)
                            .start()

                        roboEyeRight!!.animate().scaleX(1f)
                            .scaleY(1f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_neutral_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        roboLips!!.scaleX = 1f
                        roboLips!!.scaleY = 1f

                        isTalking = true

                        startLipAnim()

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }


                    }

                    Expressions.ANGRY->{

                        isTalking = false
                        isLeft = false
                        isRight = false

                        roboEyeBrowLeft!!.animate().rotation(10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (!isTranslated) {

                            roboEyeBrowLeft!!.animate().translationYBy(30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(30f).start()

                            isTranslated = true


                        }

                        if (isSleeping) {

                            isSleeping = false

                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))


                        roboEyeLeft!!.animate().scaleX(0.8f)
                            .scaleY(1f)
                            .start()

                        roboEyeRight!!.animate().scaleX(0.8f)
                            .scaleY(1f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_angry_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        roboLips!!.scaleX = 1f
                        roboLips!!.scaleY = 1f

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }


                    }

                    Expressions.ANGRY_TALKING->{

                        isTalking = true


                        roboEyeBrowLeft!!.animate().rotation(10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (!isTranslated) {

                            roboEyeBrowLeft!!.animate().translationYBy(30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(30f).start()

                            isTranslated = true
                        }



                        if (isSleeping) {

                            isSleeping = false

                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))

                        roboEyeLeft!!.animate().scaleX(0.8f)
                            .scaleY(1f)
                            .start()

                        roboEyeRight!!.animate().scaleX(0.8f)
                            .scaleY(1f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_angry_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        roboLips!!.scaleX = 1f
                        roboLips!!.scaleY = 1f

                        startLipAnim()

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }


                    }

                    Expressions.SAD->{

                        isTalking = false


                        roboEyeBrowLeft!!.animate().rotation(-20f).start()
                        roboEyeBrowRight!!.animate().rotation(20f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        if (isSleeping) {

                            isSleeping = false

//                            roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
//                            roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))

                        roboEyeLeft!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .rotation(-10f)
                            .start()

                        roboEyeRight!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .rotation(10f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_angry_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        roboLips!!.scaleX = 1f
                        roboLips!!.scaleY = 0.5f

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }

                    }

                    Expressions.SAD_TALKING->{

                        isTalking = true


                        roboEyeBrowLeft!!.animate().rotation(-20f).start()
                        roboEyeBrowRight!!.animate().rotation(20f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        if (isSleeping) {

                            isSleeping = false

//                            roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
//                            roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))

                        roboEyeLeft!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .rotation(-10f)
                            .start()

                        roboEyeRight!!.animate().scaleX(0.6776f)
                            .scaleY(0.4776f)
                            .rotation(10f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_angry_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        roboLips!!.scaleX = 1f
                        roboLips!!.scaleY = 0.5f

                        startLipAnim()

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }

                    }

                    Expressions.SLEEP->{

                        isTalking = false
                        isLeft = false
                        isRight = false

                        roboEyeBrowLeft!!.animate().rotation(0f).start()
                        roboEyeBrowRight!!.animate().rotation(0f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }


                        isSleeping = true

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left_sleep))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left_sleep))

                        (roboEyeLeft!!.drawable as AnimatedVectorDrawable).start()
                        (roboEyeRight!!.drawable as AnimatedVectorDrawable).start()



//                        roboEyeLeft!!.animate().scaleX(0.2f)
//                            .scaleY(0.01114f)
//                            .rotation(0f)
//                            .start()
//
//                        roboEyeRight!!.animate().scaleX(0.2f)
//                            .scaleY(0.01114f)
//                            .rotation(0f)
//                            .start()



//                        isLeft = false
//                        isRight = false

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))

                        if (isLipsRotated) {

                            roboLips!!.animate().rotation(0f).start()
                            isLipsRotated = false

                        }

                        roboLips!!.scaleX = 0.5f
                        roboLips!!.scaleY = 0.3f

                    }

                    Expressions.CONFUSED->{

                        isTalking = false


                        roboEyeBrowLeft!!.animate().rotation(10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        if (isSleeping) {

                            isSleeping = false

                            //roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                            //roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_confused_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_confused_right))

                        roboEyeRight!!.animate()
                            .scaleY(0.6776f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_neutral_selector))

                        if (!isLipsRotated) {

                            roboLips!!.animate().rotation(-20f).start()
                            isLipsRotated = true

                        }

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }

                    }

                    Expressions.CONFUSED_TALKING->{

                        isTalking = false


                        roboEyeBrowLeft!!.animate().rotation(10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        if (isSleeping) {

                            isSleeping = false

                            //roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                            //roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        }

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_confused_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_confused_right))

                        roboEyeRight!!.animate()
                            .scaleY(0.6776f)
                            .start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_neutral_selector))

                        if (!isLipsRotated) {

                            roboLips!!.animate().rotation(-20f).start()
                            isLipsRotated = true

                        }

                        //roboLips!!.animate().rotation(-20f).start()



                        isTalking = true

                        startLipAnim()

                        if (!isLeft && !isRight) {

                            isLeft = true
                            isRight = true
                            startAnims()
                        }
                    }

                    Expressions.LOVE->{

                        isTalking = false

                        roboEyeBrowLeft!!.animate().rotation(-10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        isLeft = false
                        isRight = false

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.heart_svgrepo_com))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.heart_svgrepo_com))

                        roboEyeLeft!!.animate().scaleX(0.7f).scaleY(0.7f).start()
                        roboEyeRight!!.animate().scaleX(0.7f).scaleY(0.7f).start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))

                        roboLips!!.animate().rotation(0f).start()

                    }

                    Expressions.LOVE_TALKING->{

                        isTalking = true

                        roboEyeBrowLeft!!.animate().rotation(-10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        isLeft = false
                        isRight = false

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.heart_svgrepo_com))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.heart_svgrepo_com))

                        roboEyeLeft!!.animate().scaleX(0.7f).scaleY(0.7f).start()
                        roboEyeRight!!.animate().scaleX(0.7f).scaleY(0.7f).start()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))

                        roboLips!!.animate().rotation(0f).start()

                        startLipAnim()

                    }

                    Expressions.WINK->{

                        isTalking = true

                        roboEyeBrowLeft!!.animate().rotation(-10f).start()
                        roboEyeBrowRight!!.animate().rotation(-10f).start()

                        if (isTranslated) {

                            isTranslated = false

                            roboEyeBrowLeft!!.animate().translationYBy(-30f).start()
                            roboEyeBrowRight!!.animate().translationYBy(-30f).start()
                        }

                        isLeft = false
                        isRight = false

                        roboEyeLeft!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))
                        roboEyeRight!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_animated_robo_eye_left))

                        roboEyeLeft!!.animate().scaleX(1f).scaleY(1f).start()
                        roboEyeRight!!.animate().scaleX(0.3f).scaleY(0.3f).start()

//                        isRight = true
//
//                        startWinkAnim()

                        roboLips!!.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.miko_lips_happy_selector))

                        roboLips!!.animate().rotation(0f).start()

                        startLipAnim()


                    }

                    else->{


                    }
                }



            }

        }

        mutableLiveExpression = MutableLiveData()

        mutableLiveExpression!!.observeForever(expressionObserver!!)
    }

    var isLeft:Boolean = true
    var isRight:Boolean = true

    private fun startAnims() {

        CoroutineScope(Dispatchers.Main).launch {

            while(isLeft) {

                (roboEyeRight!!.drawable as AnimatedVectorDrawable).start()

                (roboEyeLeft!!.drawable as AnimatedVectorDrawable).start()

                delay(200)

                (roboEyeRight!!.drawable as AnimatedVectorDrawable).stop()

                (roboEyeLeft!!.drawable as AnimatedVectorDrawable).stop()

                delay(4000)

            }

        }

//        CoroutineScope(Dispatchers.Main).launch {
//
//            while(isRight) {
//
//                (roboEyeRight!!.drawable as AnimatedVectorDrawable).start()
//
//                delay(300)
//
//                (roboEyeRight!!.drawable as AnimatedVectorDrawable).stop()
//
//                delay(4000)
//
//            }
//
//        }

        //(roboEyeLeft!!.drawable as AnimatedVectorDrawable).start()
        //(roboEyeRight!!.drawable as AnimatedVectorDrawable).start()
    }

    private fun startLipAnim() {

        roboLips!!.post {

            CoroutineScope(Dispatchers.Main).launch {

                var fArr = floatArrayOf(0.12996f, 0.34669f, 0.588799f,0.76554f ,0.89986f, 1f, 0.89986f,0.76554f, 0.588799f,0.34669f, 0.12996f)

                while (isTalking) {

                    for (f in fArr) {

                        if (!isTalking) {

                            break
                        }

                        if (f>=1f) {

                            roboLips!!.isSelected = true
                            roboLips!!.scaleX = 1f


                        } else {

                            roboLips!!.isSelected = false

                        }

                        roboLips!!.scaleY = f

                        if (f<=0.7) {

                            roboLips!!.scaleX = 0.6776f
                            roboLips!!.isSelected = false
//                            roboLips!!.setImageDrawable(ContextCompat.getDrawable(c
//                                ,R.drawable.miko_lips_happy_selector))

                        } else {

//                            roboLips!!.scaleX = 1f
//                            roboLips!!.isSelected = true

//                            smileImg.setImageDrawable(ContextCompat.getDrawable(this@MainActivity
//                                ,R.drawable.miko_lips_angry_selector))



                        }

                        delay(300)
                    }
                }
            }
        }
    }

    public fun setExpression(value:Expressions) {

        mutableLiveExpression!!.value = value

    }
}