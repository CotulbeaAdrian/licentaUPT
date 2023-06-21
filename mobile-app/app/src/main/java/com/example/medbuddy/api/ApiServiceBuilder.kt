package com.example.medbuddy.api

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiServiceBuilder {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.115:8080")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
