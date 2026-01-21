package com.ai.roboteacher.Models

import com.google.gson.annotations.SerializedName

data class ClassResponse(
    val status: String,
    val message: String,
    val errors: List<String>,
    val data: List<ClassItem>?
) {


    data class ClassItem(
        val id: Int,
        val name: String,
        @SerializedName("school_id") val schoolId: Int = 0,
        @SerializedName("class_id") val classId: Int = 0


    ) {

        override fun equals(other: Any?): Boolean {

            if (other is ClassItem) {

                if (name.equals((other as ClassItem).name)) {

                    return true

                }

                return false


            } else if (other is AssignmentResponse.AssignmentData) {

                if (id == (other as AssignmentResponse.AssignmentData).subject.id) {

                    return true

                } else if (id == (other as AssignmentResponse.AssignmentData).`class`.id) {

                    return true
                }

                return false


            } else {

                return false
            }




        }
    }
}

