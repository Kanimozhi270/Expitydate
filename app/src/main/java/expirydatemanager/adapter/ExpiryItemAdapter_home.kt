package expirydatemanager.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.activity.AddItemActivity
import expirydatemanager.activity.Expiry_FullView
import expirydatemanager.pojo.ItemList
import nithra.tamil.calendar.expirydatemanager.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpiryItemAdapter_home(
    private val itemList: MutableList<ItemList.GetList>,
    private val contextFromFrag: Context, // Add 'private val' here
    private val item_type: String,
    private val onDeleteClick: (itemId: Int) -> Unit
) : RecyclerView.Adapter<ExpiryItemAdapter_home.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        // Safely get the 'item_name' and 'reminder_type' from the Map
        val itemName = item.itemName
        val reminderType = item.reminderType?.toString() ?: "No Reminder"
        val expiry_on = item.actionDate?.toString() ?: "No Expiry Date"

        val id = item.id
        println("ID after conversion: $id")

        holder.serialNumber.text = (position + 1).toString()

        // Set values to the ViewHolder
        holder.itemName.text = itemName
        holder.expiryDate.text = reminderType
        holder.expiry_on.text = expiry_on

        // Get the current date
        val currentDate = LocalDate.now()

        // Parse the expiry date (assuming the expiry date is in a format like "dd-MM-yyyy")
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Adjust the format based on your date string
        val expiryDate = try {
            LocalDate.parse(item.actionDate, formatter)
        } catch (e: Exception) {
            null
        }

        // Compare dates and show "Overdue" if the current date is greater than expiry date
        if (expiryDate != null && currentDate.isAfter(expiryDate)) {
            holder.overdueText.visibility = View.VISIBLE // Show overdue text
        } else {
            holder.overdueText.visibility = View.GONE // Hide overdue text
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(contextFromFrag, Expiry_FullView::class.java).apply {

              //  putExtra("item_data", item)
                putExtra("item_type", item_type)
                putExtra("isEditMode", "edit")
                putExtra("list_id", item.id)
                putExtra("category_id", item.categoryId)
                putExtra("category_name", item.categoryName)
                putExtra("item_id", item.itemId)
                putExtra("action_date", item.actionDate)
                putExtra("reminder_type", item.reminderType)
                putExtra("notify_time", item.notifyTime)
                putExtra("item_name", item.itemName)
                putExtra("remark", item.remark)

            }
            contextFromFrag.startActivity(intent)
        }


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
                "item_type" to item_type,
                "remark" to item.remark,
            )

            val intent = Intent(contextFromFrag, AddItemActivity::class.java).apply {
              //  putExtra("item_data", itemList[position].itemId)
                putExtra("item_data", itemMap)
                putExtra("isEditMode", "edit")
            }
            contextFromFrag.startActivity(intent)
        }

        holder.expiryDelete.setOnClickListener {
            item.id?.let { id -> onDeleteClick(id) }
        }


    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemName)
        val expiryDate: TextView = view.findViewById(R.id.reminderBefore)
        val expiry_on: TextView = view.findViewById(R.id.expityOn)
        val expiryEdit: LinearLayout = view.findViewById(R.id.expiry_edit)
        val expiryDelete: LinearLayout = view.findViewById(R.id.expiry_delete)
        val serialNumber: TextView = view.findViewById(R.id.serialNumber)
        val overdueText: TextView = view.findViewById(R.id.overdueText)
    }
}






