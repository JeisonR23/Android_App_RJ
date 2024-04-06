package com.example.taskapp.di

import com.example.taskapp.api.ApiTask
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {
     const val BASE_URL= "http://172.20.1.193:3000/tarea/"
     const val BASE_URL_CASA= "http://192.168.1.7:3000/tarea/"
    val api: ApiTask by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(ApiTask::class.java)
    }
}