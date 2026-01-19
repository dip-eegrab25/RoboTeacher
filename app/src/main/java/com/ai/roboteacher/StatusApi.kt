package com.ai.roboteacher

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

    interface StatusApi {
        @GET("status")
        suspend fun getStatus(): Response<ResponseBody>
    }

