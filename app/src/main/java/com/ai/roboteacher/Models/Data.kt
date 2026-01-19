package com.example.myapplication

import com.google.gson.annotations.SerializedName


data class Data (

  @SerializedName("id"         ) var id        : Int? = null,
  @SerializedName("unit"       ) var unit      : String? = "",
  @SerializedName("chapter"    ) var chapter   : String? = "",
  @SerializedName("subject_id" ) var subjectId : Int? = null,
  @SerializedName("class_id"   ) var classId   : Int? = null

)