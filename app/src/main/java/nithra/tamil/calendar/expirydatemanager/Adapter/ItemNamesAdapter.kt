package nithra.tamil.calendar.expirydatemanager.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.Activity.AddItemActivity
import nithra.tamil.calendar.expirydatemanager.Activity.ItemData
import nithra.tamil.calendar.expirydatemanager.Activity.ItemNamesList
import nithra.tamil.calendar.expirydatemanager.R

class ItemNamesAdapter(private val itemList: MutableList<ItemData>, private val db: SQLiteDatabase) :
    RecyclerView.Adapter<ItemNamesAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemName)
        val expiryOn: TextView = itemView.findViewById(R.id.expityOn)
        val reminderBefore: TextView = itemView.findViewById(R.id.reminderBefore)
        val expiryDelete: View = itemView.findViewById(R.id.expiry_delete)
        val expiryEdit: View = itemView.findViewById(R.id.expity_edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_name_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemNameTextView.text = item.itemName
        holder.expiryOn.text = "Expiry On: ${item.expiryDate}"
        holder.reminderBefore.text = "Reminder Before: ${item.reminderBefore} days"

        holder.expiryEdit.setOnClickListener {
            showEditDialog(item, holder.itemView.context)
        }

        holder.expiryDelete.setOnClickListener {
            showDeleteDialog(item, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int = itemList.size

    // Show dialog for Edit action
    private fun showEditDialog(item: ItemData, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Item")
        builder.setMessage("Do you want to edit this item?")
        builder.setPositiveButton("Edit") { _, _ ->
            editItem(item, context)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // Show dialog for Delete action
    private fun showDeleteDialog(item: ItemData, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Item")
        builder.setMessage("Are you sure you want to delete this item?")
        builder.setPositiveButton("Delete") { _, _ ->
            deleteItem(item, context)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // Edit Item
    private fun editItem(item: ItemData, context: Context) {
        val intent = Intent(context, AddItemActivity::class.java).apply {
            putExtra("id", item.id)
            putExtra("itemname", item.itemName)
            putExtra("expiry_date", item.expiryDate)
            putExtra("reminderbefore", item.reminderBefore)
        }
        context.startActivity(intent)
    }

    // Delete Item
    private fun deleteItem(item: ItemData, context: Context) {
        db.execSQL("DELETE FROM items WHERE id = ${item.id}")
        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()

        // Remove item from list and update RecyclerView
        val position = itemList.indexOf(item)
        if (position != -1) {
            itemList.removeAt(position)
            notifyItemRemoved(position)
        }

        // Check if list is empty and update visibility of tvEmptyMessage
        (context as? ItemNamesList)?.checkEmptyState()
    }
}
