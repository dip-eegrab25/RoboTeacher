package com.example.myapplication

import com.google.gson.annotations.SerializedName


data class CurriculumResponse (

  @SerializedName("status"  ) var status  : String?           = null,
  @SerializedName("message" ) var message : String?           = null,
  @SerializedName("errors"  ) var errors  : ArrayList<String> = arrayListOf(),
  @SerializedName("data"    ) var data    : ArrayList<Data>   = arrayListOf()

)