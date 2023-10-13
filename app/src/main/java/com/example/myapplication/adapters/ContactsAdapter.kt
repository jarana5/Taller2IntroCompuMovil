import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ContactoAdapter(private val contactos: List<Pair<Int, String>>) :
	RecyclerView.Adapter<ContactoAdapter.ViewHolder>() {
	
	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val imageViewContacto: ImageView = itemView.findViewById(R.id.imageViewContacto)
		val nameViewContacto: TextView = itemView.findViewById(R.id.nameTextContacto)
		val indexViewContacto: TextView = itemView.findViewById(R.id.indexViewContacto)
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.contact, parent, false)
		return ViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val (indice, nombre) = contactos[position]
		holder.nameViewContacto.text = nombre
		holder.indexViewContacto.text = indice.toString()
		holder.imageViewContacto.setImageResource(R.drawable.contacts) // Cambia la imagen predeterminada según tus necesidades
	}
	
	override fun getItemCount(): Int {
		return contactos.size
	}
}
