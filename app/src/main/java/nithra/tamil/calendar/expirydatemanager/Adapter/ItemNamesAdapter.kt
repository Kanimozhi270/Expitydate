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
import nithra.tamil.calendar.expirydatemanager.Activity.Expiry_FullView
import nithra.tamil.calendar.expirydatemanager.R

class ItemNamesAdapter(private val itemList: List<HashMap<String, Any>>) :
    RecyclerView.Adapter<ItemNamesAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemName)
        val expiryOn: TextView = itemView.findViewById(R.id.expityOn)
        val reminderBefore: TextView = itemView.findViewById(R.id.reminderBefore)
        val expiryDelete: View = itemView.findViewById(R.id.expiry_delete)
        val expiryEdit: View = itemView.findViewById(R.id.expity_edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        println("size of item list == ${itemList.size}")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_name_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemNameTextView.text = item["item_name"].toString()
        holder.expiryOn.text = "Expiry On: ${item}"

        // Convert reminderBefore text to numeric values
        /*        val reminderDays = when (item.reminderBefore.lowercase()) {
                    "same day" -> "1"
                    "2 days before" -> "2"
                    "1 week before" -> "7"
                    else -> item.reminderBefore // If custom, show the exact text
                }

                holder.reminderBefore.text = "$reminderDays"*/

        holder.expiryEdit.setOnClickListener {
            showEditDialog(item, holder.itemView.context)
        }

        holder.expiryDelete.setOnClickListener {
            showDeleteDialog(item, holder.itemView.context)
        }

        // Navigate to FullViewActivity when expiry_card is clicked
        holder.itemView.findViewById<View>(R.id.expiry_card).setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, Expiry_FullView::class.java)
            intent.putExtra("item_id", item["id"].toString()) // Pass the item ID
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = itemList.size



    // Show dialog for Edit action
    private fun showEditDialog(item: HashMap<String, Any>, context: Context) {
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
    private fun showDeleteDialog(item: HashMap<String, Any>, context: Context) {
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
    private fun editItem(item: HashMap<String, Any>, context: Context) {
        val intent = Intent(context, AddItemActivity::class.java).apply {
            /*  putExtra("id", item.id)
              putExtra("itemname", item.itemName)
              putExtra("expiry_date", item.expiryDate)
              putExtra("reminderbefore", item.reminderBefore)*/
        }
        context.startActivity(intent)
    }

    // Delete Item
    private fun deleteItem(item: HashMap<String, Any>, context: Context) {

        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()

        // Remove item from list and update RecyclerView
        val position = itemList.indexOf(item)
        if (position != -1) {
            //itemList.removeAt(position)
            notifyItemRemoved(position)
        }

    }
}
