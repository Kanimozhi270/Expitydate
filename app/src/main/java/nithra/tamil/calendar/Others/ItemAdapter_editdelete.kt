package nithra.tamil.calendar.Others

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.R

class ItemAdapter_editdelete(
    private val context: Context,
    private var items: MutableList<String>,
    private val onEdit: (String) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ItemAdapter_editdelete.ItemViewHolder>() {

    private var filteredItems = items.toMutableList()

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items.toMutableList()
        } else {
            items.filter { it.contains(query, true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }


    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_row_editdelete, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = filteredItems.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.tvItemName.text = item
        holder.tvItemName.setOnClickListener { onItemClick(item) }
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }
}
