package com.ai.roboteacher.Models

import com.google.gson.annotations.SerializedName

class SchoolData(@SerializedName("status"  ) var status  : String?           = null,
                 @SerializedName("message" ) var message : String?           = null,
                 @SerializedName("errors"  ) var errors  : ArrayList<String> = arrayListOf(),
                 @SerializedName("data"    ) var data    : ArrayList<Data>   = arrayListOf()) {

    data class Data (

        @SerializedName("school_id"            ) var schoolId           : Int?    = null,
        @SerializedName("school_name"          ) var schoolName         : String? = null,
        @SerializedName("school_address"       ) var schoolAddress      : String? = null,
        @SerializedName("city"                 ) var city               : String? = null,
        @SerializedName("state"                ) var state              : String? = null,
        @SerializedName("zip_code"             ) var zipCode            : String? = null,
        @SerializedName("contact_person"       ) var contactPerson      : String? = null,
        @SerializedName("contact_email"        ) var contactEmail       : String? = null,
        @SerializedName("contact_phone"        ) var contactPhone       : String? = null,
        @SerializedName("board"                ) var board              : String? = null,
        @SerializedName("date_of_registration" ) var dateOfRegistration : String? = null

    )


}