package expirydatemanager.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.activity.AddItemActivity
import expirydatemanager.activity.Expiry_FullView
import expirydatemanager.pojo.ItemList
import nithra.tamil.calendar.expirydatemanager.R

class ExpiryListAdapter(private val itemList: MutableList<ItemList.GetList>) :
    RecyclerView.Adapter<ExpiryListAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemName)
        val expiryOn: TextView = itemView.findViewById(R.id.expityOn)
        val reminderBefore: TextView = itemView.findViewById(R.id.reminderBefore)
        val expiryDelete: LinearLayout = itemView.findViewById(R.id.expiry_delete)
        val expiryEdit: LinearLayout = itemView.findViewById(R.id.expiry_edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_name_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        holder.itemNameTextView.text = item.itemName ?: "No Name"
        holder.expiryOn.text = "${item.actionDate ?: "N/A"}"
        holder.reminderBefore.text = "Reminder: ${item.reminderType ?: "N/A"}"


        holder.expiryEdit.setOnClickListener {
            showEditDialog(item, holder.itemView.context)
        }

        holder.expiryDelete.setOnClickListener {
            showDeleteDialog(item, holder.itemView.context)
        }

        holder.itemView.findViewById<View>(R.id.expiry_card).setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, Expiry_FullView::class.java)
            intent.putExtra("item_id", item.id ?: 0)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = itemList.size

    private fun showEditDialog(item: ItemList.GetList, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Edit Item")
            .setMessage("Do you want to edit this item?")
            .setPositiveButton("Edit") { _, _ -> editItem(item, context) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(item: ItemList.GetList, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { _, _ -> deleteItem(item, context) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editItem(item: ItemList.GetList, context: Context) {
        val intent = Intent(context, AddItemActivity::class.java).apply {
            putExtra("id", item.id)
            putExtra("item_name", item.itemName)
            putExtra("action_date", item.actionDate)
            putExtra("reminder_type", item.reminderType)
            putExtra("notify_time", item.notifyTime)
            putExtra("remark", item.remark)
            putExtra("custom_date", item.customDate.toString())
            putExtra("item_id", item.itemId)
            putExtra("category_id", item.categoryId)
        }
        context.startActivity(intent)
    }

    private fun deleteItem(item: ItemList.GetList, context: Context) {
        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
        val position = itemList.indexOf(item)
        if (position != -1) {
            itemList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
