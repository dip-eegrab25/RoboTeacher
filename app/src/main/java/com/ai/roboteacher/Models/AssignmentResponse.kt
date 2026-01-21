package com.ai.roboteacher.Models

import kotlinx.serialization.Serializable

data class AssignmentResponse(
    val status: String,
    val message: String,
    val errors: List<String>,
    val data: AssignmentData
):java.io.Serializable {

    data class AssignmentData(
        val id: Int,
        val school: School,
        val `class`: ClassInfo,
        val subject: Subject,
        val topic: Topic,
        val teacher: Teacher,
        val date: String,
        val start_time: String,
        val end_time: String,
        val otp: String,
        val status: Int,
        val is_completed: Int
    ): java.io.Serializable {


        data class School(
            val id: Int,
            val name: String
        ):java.io.Serializable

        data class ClassInfo(
            val id: Int,
            val name: String
        ):java.io.Serializable

        data class Subject(
            val id: Int,
            val name: String
        ):java.io.Serializable

        data class Topic(
            val id: Int,
            val name: String
        ):java.io.Serializable

        data class Teacher(
            val id: Int,
            val name: String
        ):java.io.Serializable

        override fun equals(other: Any?): Boolean {
            return this.id == (other as AssignmentResponse.AssignmentData.ClassInfo).id
        }
    }

}

