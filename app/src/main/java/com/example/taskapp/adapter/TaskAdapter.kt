import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.taskapp.R
import com.example.taskapp.dto.Tarea

class TaskAdapter(
    var context: Context,
    var tareas: List<Tarea>
) : RecyclerView.Adapter<TaskAdapter.UsuarioViewHolder>() {

    private var onClick: OnItemClicked? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return UsuarioViewHolder(vista)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val tarea = tareas[position]

        holder.Nombre.text = tarea.nombre
        holder.Decripcion.text = tarea.descripcion
        holder.Ruta_Archivo.text = tarea.archivo.toString()
        holder.Fecha_Creacion.text = tarea.fecha_creacion

        holder.btnEditar.setOnClickListener {
            onClick?.editarTarea(tarea)
        }

        holder.btnBorrar.setOnClickListener {
            onClick?.borrarTarea(tarea.id)
        }
    }

    override fun getItemCount(): Int {
        return tareas.size
    }

    inner class UsuarioViewHolder(itemView: View) : ViewHolder(itemView) {
        val Nombre: TextView = itemView.findViewById(R.id.Nombre)
        val Decripcion: TextView = itemView.findViewById(R.id.Descripcion)
        val Ruta_Archivo: TextView = itemView.findViewById(R.id.Archivo)
        val Fecha_Creacion: TextView = itemView.findViewById(R.id.FechaCreacion)
        val btnEditar: Button = itemView.findViewById(R.id.btnEditar)
        val btnBorrar: Button = itemView.findViewById(R.id.btnBorrar)
    }

    interface OnItemClicked {
        fun editarTarea(usuario: Tarea)
        fun borrarTarea(idUsuario: Long)
    }

    fun setOnClick(onClick: OnItemClicked?) {
        this.onClick = onClick
    }

}