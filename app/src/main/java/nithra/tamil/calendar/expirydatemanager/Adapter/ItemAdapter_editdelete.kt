package nithra.tamil.calendar.expirydatemanager.Adapter

import android.app.AlertDialog
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
    var items: MutableList<Map<String, Any>>,
    private val onItemClick: (Map<String, Any>) -> Unit,
    private val onEdit: (String, Int) -> Unit,  // Edit listener with item name and ID
    private val onDelete: (Int) -> Unit         // Delete listener with item ID
) : RecyclerView.Adapter<ItemAdapter_editdelete.ItemViewHolder>() {

    private var filteredItems: MutableList<Map<String, Any>> = items

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter {
                (it["category"]?.toString()?.contains(query, true) == true ||
                        it["item_name"]?.toString()?.contains(query, true) == true)
            }.toMutableList()
        }
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
        holder.tvItemName.text = item["item_name"]?.toString() ?: ""

        // Handle edit button click
        holder.btnEdit.setOnClickListener {
            val itemName = item["item_name"]?.toString() ?: ""
            val itemId = item["item_id"] as? Int ?: 0
            onEdit(itemName, itemId) // Call edit listener with item name and item ID
        }

        // Handle delete button click
        holder.btnDelete.setOnClickListener {
            val itemId = item["item_id"] as? Int ?: 0
            showDeleteConfirmationDialog(itemId)  // Show delete confirmation dialog
        }

        // Handle item click
        holder.tvItemName.setOnClickListener {
            onItemClick(item)
        }
    }

    private fun showDeleteConfirmationDialog(itemId: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { dialog, id ->
                onDelete(itemId)  // Call delete listener with item ID
            }
            .setNegativeButton("No", null)
        builder.create().show()
    }
}



