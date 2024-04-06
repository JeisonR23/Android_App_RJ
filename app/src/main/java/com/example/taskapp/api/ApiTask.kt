package com.example.taskapp.api

import com.example.taskapp.dto.Tarea
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiTask{
    @GET("tarea/get")
    suspend fun getTask(): Response<List<Tarea>>


    @GET("tarea/find/{id}")
    suspend fun obtenerTarea(
        @Path("id") id: Long
    ): Response<Tarea>

    @Multipart
    @POST("tarea/post")
    suspend fun agregarTarea(
        @Part archivo: MultipartBody.Part,
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody
    ): Response<Tarea>


    @Multipart
    @PUT("tarea/update/{id}")
    suspend fun actualizarTarea(
        @Path("id") id: Long,
        @Part archivo: MultipartBody.Part,
        @Part ("nombre") nombre: RequestBody,
        @Part ("descripcion") descripcion: RequestBody
    ): Response<Tarea>

    @DELETE("tarea/delete/{id}")
    suspend fun borrarTarea(
        @Path("id") id: Long
    ): Response<Tarea>
}



