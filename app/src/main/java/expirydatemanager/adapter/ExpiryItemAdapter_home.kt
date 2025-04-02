package expirydatemanager.Adapter

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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExpiryItemAdapter_home(
    private val itemList: MutableList<ItemList.GetList>,
    private val contextFromFrag: Context, // Add 'private val' here
    private val item_type: String,
    private val addItemLauncher: ActivityResultLauncher<Intent>,
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
        val formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy") // Adjust the format based on your date string
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
            println("categoryy idd===${item.categoryId}")

            val intent = Intent(contextFromFrag, Expiry_FullView::class.java).apply {

                //  putExtra("item_data", item)
                putExtra("item_type", item_type)
                putExtra("itemType", item_type)
                putExtra("isEditMode", "edit")
                putExtra("list_id", item.id.toString())
                putExtra("category_id", item.categoryId)
                putExtra("category_name", item.categoryName)
                putExtra("item_id", item.itemId)
                putExtra("action_date", item.actionDate)
                putExtra("reminder_type", item.reminderType)
                putExtra("notify_time", item.notifyTime)
                putExtra("item_name", item.itemName)
                putExtra("remark", item.remark)

                println("list_iddddd == ${item.id}")
                println("cat idddd== ${item.categoryName}")
                println("cat nameee == ${item.categoryId}")
            }
            addItemLauncher.launch(intent)
        }


        holder.expiryEdit.setOnClickListener {
            val itemMap = hashMapOf(
                "id" to item.id?.toString().orEmpty(),
                "category_id" to item.categoryId?.toString().orEmpty(),
                "category_name" to item.categoryName.orEmpty(),
                "action_date" to item.actionDate.orEmpty(),
                "reminder_type" to item.reminderType.orEmpty(),
                "notify_time" to item.notifyTime.orEmpty(),
                "item_name" to item.itemName.orEmpty(),
                "item_type" to item_type,
                "remark" to item.remark.orEmpty()
            )
            // Create the Intent
            val intent = Intent(contextFromFrag, AddItemActivity::class.java).apply {
                putExtra("item_data", itemMap)
                putExtra("isEditMode", "edit")
                putExtra("itemType", item_type)
            }

            // Use the passed ActivityResultLauncher to launch AddItemActivity
            addItemLauncher.launch(intent)
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






