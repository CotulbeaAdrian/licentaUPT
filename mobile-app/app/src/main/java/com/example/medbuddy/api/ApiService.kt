package com.example.medbuddy.api

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

    @FormUrlEncoded
    @POST("/register")
    fun register(
        @Field("fullName") fullName: String,
        @Field("email") email: String,
        @Field("phoneNumber") phoneNumber: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Call<String>

    @FormUrlEncoded
    @POST("/updateProfile")
    fun updateProfile(
        @Field("id") id: String,
        @Field("fullName") fullName: String,
        @Field("phoneNumber") phoneNumber: String,
        @Field("age") age: String,
        @Field("weight") weight: String,
        @Field("gender") gender: String
    ): Call<String>

    @FormUrlEncoded
    @POST("/getMedicalRecordsAsPatient")
    fun getMedicalRecordsAsPatient(
        @Field("id") id: String
    ): Call<String>

    @FormUrlEncoded
    @POST("/getName")
    fun getName(
        @Field("id") id: String
    ): Call<String>

    @FormUrlEncoded
    @POST("/getMedicalRecordsAsDoctor")
    fun getMedicalRecordsAsDoctor(
        @Field("id") email: String
    ): Call<String>

    @POST("/logout")
    fun logout(): Call<ResponseBody>
}