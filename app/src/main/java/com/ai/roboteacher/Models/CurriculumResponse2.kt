package com.ai.roboteacher.Models

import com.google.gson.annotations.SerializedName

data class CurriculumResponse2 (

    @SerializedName("status"  ) var status  : String?           = null,
    @SerializedName("message" ) var message : String?           = null,
    @SerializedName("errors"  ) var errors  : ArrayList<String> = arrayListOf(),
    @SerializedName("data"    ) var data    : Data?             = Data()){

    data class Data (

        @SerializedName("class"          ) var `class`         : String?         = null,
        @SerializedName("class_name"     ) var className     : String?         = null,
        @SerializedName("subject"        ) var subject       : String?         = null,
        @SerializedName("total_units"    ) var totalUnits    : Int?            = null,
        @SerializedName("total_chapters" ) var totalChapters : Int?            = null,
        @SerializedName("list"           ) var list          : ArrayList<List> = arrayListOf()

    )

    data class List (

        @SerializedName("chapter" ) var chapter : Int? = null,
        @SerializedName("unit" ) var unit : Int? = null

    )
}