package com.ai.roboteacher.RetrofitBuilder

import android.content.Context
import com.ai.roboteacher.Api.Api
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstanceBuilder {

     const val BASEURL = "http://192.168.1.41:8012"

     var STUDY = "/study"
     const val GENERIC_TASK = "/generic-tasks"
     const val GENERAL_PURPOSE_ASSISTANT = "/general-purpose-assistant/"
     var TEACHING_ASSISTANT = "/teaching-assistant/"
     var QUESTION_GENERATOR = "/question-generator/"
     var shutDown = "/shutdown"
     var modelURL = "http://192.168.1.41/panda/aimodel/whisper-base.tflite"




    private var retrofitBuilder:RetrofitInstanceBuilder?=null
    private var retrofit: Retrofit?=null

     fun getRetrofitInstance(c:Context):RetrofitInstanceBuilder {

        if (retrofitBuilder == null) {

            val gson = GsonBuilder()
                .setLenient()
                .create()

            val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASEURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        }

         return this


    }

     fun getApi():Api? {

        retrofit?.let {

            return retrofit!!.create(Api::class.java)
        }

        return null




    }
}