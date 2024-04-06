package com.example.taskapp.dto



data class Tarea(
    var id: Long,
    var nombre: String? = null,
    var descripcion: String? = null,
    var archivo: String? = null,
    val fecha_creacion: String? = null,
    val fecha_limite: String? = null
)

