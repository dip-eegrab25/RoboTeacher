package com.ai.roboteacher

import android.text.Spanned

data class DataSet(
    var id: Long,
    var spannableStringBuilder: MySpannableStringBuilder,
    var isProcess: Boolean=false,
    var ansList:ArrayList<MySpannableStringBuilder?>? = null,
    var isQuestion:Boolean = false,
    var isImage:Boolean = false,
    var isPage:Boolean = false,
    var quesNo:Int = 0,
    var isAnim:Boolean = false,
    var renderedMarkDown:Spanned?=null,
    var isError:Boolean = false

)
