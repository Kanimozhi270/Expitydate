package expirydatemanager.Adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.R


class ExpiryItemAdapter_editdelete(
    private val context: Context,
    var items: MutableList<Map<String, Any>>,
    private val onItemClick: (Map<String, Any>) -> Unit,
    private val onEdit: (String, Int, String) -> Unit,  // Edit listener with item name and ID
    private val onDelete: (Int, String) -> Unit         // Delete listener with item ID
) : RecyclerView.Adapter<ExpiryItemAdapter_editdelete.ItemViewHolder>() {

    private var filteredItems: MutableList<Map<String, Any>> = items

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter {
                (it["category"]?.toString()
                    ?.contains(query, true) == true || it["item_name"]?.toString()
                    ?.contains(query, true) == true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateList(newItems: MutableList<Map<String, Any>>) {
        this.items.clear()
        this.items.addAll(newItems)
        this.filteredItems = newItems.toMutableList() // 🔥 update this too!
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

        println("filteredItems ==$item")
        if (item.containsKey("item_name")) {
            holder.tvItemName.text = filteredItems[position]["item_name"] as String? ?: ""

        } else if (item.containsKey("category")) {
            holder.tvItemName.text = filteredItems[position]["category"] as String? ?: ""
        }
        // Handle edit button click
        holder.btnEdit.setOnClickListener {
            var itemName = ""
            var itemType = ""
            var itemId = 0
            if (item.containsKey("item_name")) {
                itemName = item["item_name"]?.toString() ?: ""
                itemType = "item_type" // ✅ this is correct
            } else if (item.containsKey("category")) {
                itemName = item["category"]?.toString() ?: ""
                itemType = "categorys" // ✅ should match how you check in refresh
            }

            itemId = (item["id"] as? Double)?.toInt() ?: 0
            onEdit(itemName, itemId, itemType) // Call edit listener with item name and item ID
        }

        // Handle delete button click
        holder.btnDelete.setOnClickListener {
            var itemType = ""
            if (item.containsKey("item_name")) {
                itemType = "item_type"

            } else if (item.containsKey("category")) {
                itemType = "cateogry_type"
            }
            val itemId = (item["id"] as? Double)?.toInt() ?: 0
            showDeleteConfirmationDialog(itemId, itemType)  // Show delete confirmation dialog
        }

        // Handle item click
        holder.tvItemName.setOnClickListener {
            onItemClick(item)
        }
    }

    private fun showDeleteConfirmationDialog(itemId: Int, itemType: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { dialog, id ->
                onDelete(itemId, itemType)
            }.setNegativeButton("No", null)
        builder.create().show()
    }
}


