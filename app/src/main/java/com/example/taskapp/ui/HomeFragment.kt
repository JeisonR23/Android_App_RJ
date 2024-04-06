package com.example.taskapp.ui

import TaskAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskapp.R
import com.example.taskapp.databinding.FragmentHomeBinding
import com.example.taskapp.di.AppModule
import com.example.taskapp.dto.Tarea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileNotFoundException

class HomeFragment : Fragment(), TaskAdapter.OnItemClicked {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAdapter
    private var listaTareas: List<Tarea> = emptyList()
    private var tarea = Tarea(-1, "", "", "")
    private var isEditing = false

    private lateinit var selectedFileUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bdtareas.layoutManager = LinearLayoutManager(requireContext())
        setupRecyclerView()
        obtenerTarea()
        binding.btnAddUpdate.setOnClickListener {
            val isValid = validarCampos()
            if (isValid) {
                if (!isEditing) {
                    agregarTarea(
                        binding.etNombre.text.toString(),
                        binding.etDescripcion.text.toString(),
                        binding.etArchivo.text.toString()
                    )
                } else {
                    actualizarTareas(
                        binding.etNombre.text.toString(),
                        binding.etDescripcion.text.toString(),
                        binding.etArchivo.text.toString()
                    )
                }
            } else {
                Toast.makeText(requireContext(), "Se deben llenar los campos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.btnSeleccionarArchivo.setOnClickListener {
            abrirSelectorArchivo()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(requireContext(), listaTareas)
        adapter.setOnClick(this@HomeFragment)
        binding.bdtareas.adapter = adapter
    }

    private fun validarCampos(): Boolean {
        return !(binding.etNombre.text.isNullOrEmpty() || binding.etDescripcion.text.isNullOrEmpty())
    }

    private fun obtenerTarea() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = AppModule.api.getTask()
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        listaTareas = response.body() ?: emptyList()
                        setupRecyclerView()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "ERROR CONSULTAR TODOS",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "ERROR CONSULTAR TODOS: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun agregarTarea(nombre: String, descripcion: String, archivoNombre: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(selectedFileUri)
                    ?: throw FileNotFoundException("No se pudo abrir el archivo")

                val tempFile = File.createTempFile("temp", ".jpg", requireContext().cacheDir)
                tempFile.deleteOnExit()
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                val requestBody = RequestBody.create(null, tempFile)
                val archivoParte =
                    MultipartBody.Part.createFormData("archivo", tempFile.name, requestBody)

                // Llamar a la función de la API para agregar la tarea con el archivo
                val response = AppModule.api.agregarTarea(
                    archivoParte,
                    nombre.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    descripcion.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )

                // Manejar la respuesta
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        val tareaCreada = response.body()
                        tareaCreada?.let {
                            Toast.makeText(
                                requireContext(),
                                "TAREA AGREGADA CON EXITO",
                                Toast.LENGTH_SHORT
                            ).show()
                            obtenerTarea()
                            limpiarCampos()
                            limpiarObjeto()
                        } ?: kotlin.run {
                            Toast.makeText(
                                requireContext(),
                                "Error al recibir la tarea creada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "ERROR AL AGREGAR LA TAREA",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    println(e)
                    Toast.makeText(
                        requireContext(),
                        "ERROR AL AGREGAR LA TAREA: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun actualizarTareas(nombre: String, descripcion: String, archivoNombre: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtener el InputStream del archivo seleccionado
                val inputStream = requireContext().contentResolver.openInputStream(selectedFileUri)
                    ?: throw FileNotFoundException("No se pudo abrir el archivo")

                // Crear un archivo temporal para almacenar el archivo seleccionado
                val tempFile = File.createTempFile("temp", ".jpg", requireContext().cacheDir)
                tempFile.deleteOnExit()

                // Copiar el contenido del InputStream al archivo temporal
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                // Crear la parte multipart para el archivo
                val requestBody = RequestBody.create(null, tempFile)
                val archivoParte =
                    MultipartBody.Part.createFormData("archivo", tempFile.name, requestBody)

                // Llamar a la función de la API para actualizar la tarea con el archivo
                val response = AppModule.api.actualizarTarea(
                    tarea.id,
                    archivoParte,
                    nombre.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    descripcion.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                )

                // Manejar la respuesta
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        val tareaActualizada = response.body()
                        tareaActualizada?.let {
                            Toast.makeText(
                                requireContext(),
                                "TAREA ACTUALIZADA CON EXITO",
                                Toast.LENGTH_SHORT
                            ).show()
                            obtenerTarea()
                            limpiarCampos()
                            limpiarObjeto()
                            binding.btnAddUpdate.text = "Agregar Tarea"
                            binding.btnAddUpdate.backgroundTintList =
                                resources.getColorStateList(R.color.lavender)
                            isEditing = false
                        } ?: kotlin.run {
                            Toast.makeText(
                                requireContext(),
                                "Error al recibir la tarea actualizada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "ERROR AL ACTUALIZAR LA TAREA",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    println(e)
                    Toast.makeText(
                        requireContext(),
                        "ERROR AL ACTUALIZAR LA TAREA: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun limpiarCampos() {
        binding.etNombre.setText("")
        binding.etDescripcion.setText("")
        binding.etArchivo.setText("")
    }

    private fun limpiarObjeto() {
        tarea.apply {
            id = -1
            nombre = ""
            descripcion = ""
            archivo = ""
        }
    }

    private fun abrirSelectorArchivo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedFileUri = uri
                    val fileName = obtenerNombreArchivo(uri)
                    binding.etArchivo.setText(fileName)
                }
            }
        }

    private fun obtenerNombreArchivo(uri: Uri): String {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
        return "Archivo sin nombre"
    }

    override fun editarTarea(tarea: Tarea) {
        binding.etNombre.setText(tarea.nombre)
        binding.etDescripcion.setText(tarea.descripcion)
        binding.etArchivo.setText(tarea.archivo)
        binding.btnAddUpdate.text = "Actualizar Tarea"
        binding.btnAddUpdate.backgroundTintList = resources.getColorStateList(R.color.lavender)
        this.tarea = tarea
        isEditing = true
    }

    override fun borrarTarea(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val call = AppModule.api.borrarTarea(id)
            requireActivity().runOnUiThread {
                if (call.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "TAREA ELIMINADA CON EXITO",
                        Toast.LENGTH_SHORT
                    ).show()
                    obtenerTarea()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "ERROR AL ELIMINAR LA TAREA",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
