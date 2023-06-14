package com.example.medbuddy.api

import com.example.medbuddy.api.data.LoginRequest
import com.example.medbuddy.api.data.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<String>

    @POST("/logout")
    fun logout(): Call<ResponseBody>
}