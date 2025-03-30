package expirydatemanager.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.activity.AddItemActivity
import expirydatemanager.activity.Expiry_FullView
import expirydatemanager.pojo.ItemList
import nithra.tamil.calendar.expirydatemanager.R

class ExpiryListAdapter(
    private val itemList: MutableList<ItemList.GetList>,
    private val launcher: ActivityResultLauncher<Intent>,
    private val context: Context,
    private val onDelete: (Int, String) -> Unit
) : RecyclerView.Adapter<ExpiryListAdapter.ItemViewHolder>()
{

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemName)
        val expiryOn: TextView = itemView.findViewById(R.id.expityOn)
        val reminderBefore: TextView = itemView.findViewById(R.id.reminderBefore)
        val expiryDelete: LinearLayout = itemView.findViewById(R.id.expiry_delete)
        val expiryEdit: LinearLayout = itemView.findViewById(R.id.expiry_edit)
        val serialNumber: TextView = itemView.findViewById(R.id.serialNumber)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_name_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.serialNumber.text = (position + 1).toString()

        holder.itemNameTextView.text = item.itemName ?: "No Name"
        holder.expiryOn.text = "${item.actionDate ?: "N/A"}"
        holder.reminderBefore.text = "${item.reminderType ?: "N/A"}"


        holder.expiryEdit.setOnClickListener {
            var item = itemList[position]
            val itemMap = hashMapOf(
                "id" to item.id.toString(),
                "category_id" to item.categoryId.toString(),
                "category_name" to item.categoryName.toString(),
                "custom_date" to (item.customDate ?: ""),
                "item_id" to item.itemId.toString(),
                "action_date" to item.actionDate,
                "reminder_type" to item.reminderType,
                "notify_time" to item.notifyTime,
                "item_name" to item.itemName,
                "item_type" to "1",
                "remark" to item.remark,
            )

            launcher.launch(
                Intent(context, AddItemActivity::class.java).apply {
                    putExtra("item_data", itemMap)
                    putExtra("isEditMode", "edit")
                })
        }

        holder.expiryDelete.setOnClickListener {
            var item = itemList[position]

            println("Get data for delete ==$item")
            showDeleteConfirmationDialog(
                item.id ?: 0, "item_type"
            )
        }

        holder.itemView.findViewById<View>(R.id.expiry_card).setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, Expiry_FullView::class.java)
            intent.putExtra("item_name", item.itemName)
            intent.putExtra("action_date", item.actionDate)
            intent.putExtra("reminder_type", item.reminderType)
            intent.putExtra("notify_time", item.notifyTime)
            intent.putExtra("remark", item.remark)
            intent.putExtra("category_name", item.categoryName)
            intent.putExtra("item_type", "Expiry Item") // or item.itemType if available
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int = itemList.size


    private fun showDeleteConfirmationDialog(itemId: Int, item_type: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { dialog, id ->
                onDelete(itemId, item_type)
            }.setNegativeButton("No", null)
        builder.create().show()
    }


}