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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class ExpiryItemAdapter_home(
    private val itemList: MutableList<ItemList.GetList>,
    private val contextFromFrag: Context,
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

        val itemName = item.itemName
        val reminderType = item.reminderType?.toString() ?: "No Reminder"
        val expiry_on = item.actionDate?.toString() ?: "No Expiry Date"
        val expiryDateStr = item.actionDate ?: ""
        if (expiryDateStr.isNotEmpty()) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val expiryDate = LocalDate.parse(expiryDateStr, formatter)
            val currentDate = LocalDate.now()

            val daysBetween = ChronoUnit.DAYS.between(currentDate, expiryDate)

            val reminderBefore = when {
                daysBetween > 1 -> "$daysBetween days \n Remaining"
                daysBetween == 1L -> "1 day \n Remaining"
                daysBetween == 0L -> "Today last"
                daysBetween < 0L -> "Expired before ${-daysBetween} days"
                else -> "No Expiry Date"
            }

            holder.expiryDate.text = reminderBefore
        } else {
            holder.expiryDate.text = "No Expiry Date"
        }


        holder.serialNumber.text = (position + 1).toString()
        holder.itemName.text = itemName
        //  holder.expiryDate.text = reminderType
        holder.expiry_on.text = expiry_on

        // Handle expiry status
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
        val expiryDate = try {
            LocalDate.parse(item.actionDate, formatter)
        } catch (e: Exception) {
            null
        }



        if (expiryDate != null) {
            val daysBetween =
                java.time.temporal.ChronoUnit.DAYS.between(currentDate, expiryDate).toInt()

            val statusText = when {
                daysBetween > 1 -> "$daysBetween days remaining"
                daysBetween == 1 -> "1 day remaining"
                daysBetween == 0 -> "Today last"
                daysBetween == -1 -> "Expired before 1 day"
                else -> "Expired before ${-daysBetween} days"
            }

            holder.overdueText.text = statusText
            holder.overdueText.visibility = View.VISIBLE

            // Optional color code
            holder.overdueText.setTextColor(
                if (daysBetween >= 0)
                    contextFromFrag.getColor(R.color.green)
                else
                    contextFromFrag.getColor(R.color.red)
            )
        } else {
            holder.overdueText.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(contextFromFrag, Expiry_FullView::class.java).apply {
                putExtra("item_type", item_type)
                putExtra("itemType", item_type)
                putExtra("isEditMode", "edit")
                putExtra("list_id", item.id.toString())
                putExtra("category_id", item.categoryId.toString())
                putExtra("category_name", item.categoryName)
                putExtra("item_id", item.itemId)
                putExtra("action_date", item.actionDate)
                putExtra("reminder_type", item.reminderType)
                putExtra("notify_time", item.notifyTime)
                putExtra("item_name", item.itemName)
                putExtra("remark", item.remark)
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
            val intent = Intent(contextFromFrag, AddItemActivity::class.java).apply {
                putExtra("item_data", itemMap)
                putExtra("isEditMode", "edit")
                putExtra("itemType", item_type)
            }
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
