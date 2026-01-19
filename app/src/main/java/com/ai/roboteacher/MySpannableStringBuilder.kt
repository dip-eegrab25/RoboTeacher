package com.ai.roboteacher

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan

class MySpannableStringBuilder:SpannableStringBuilder {

    constructor(data:String):super(data) {

        setSpan(ForegroundColorSpan(Color.WHITE),0,data.length, SPAN_EXCLUSIVE_EXCLUSIVE)


    }

    init {

        setSpan(ForegroundColorSpan(Color.WHITE),0,length, SPAN_EXCLUSIVE_EXCLUSIVE)
    }


}