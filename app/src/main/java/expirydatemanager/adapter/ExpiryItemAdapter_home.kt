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
import java.time.Period

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
        val expiry_on = item.actionDate?.toString() ?: "No Expiry Date"
        holder.serialNumber.text = (position + 1).toString()
        holder.itemName.text = itemName
        holder.expiry_on.text = expiry_on

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDate = LocalDate.now()
        val expiryDate = try {
            LocalDate.parse(item.actionDate, formatter)
        } catch (e: Exception) {
            null
        }

        if (expiryDate != null) {
            val isFuture = expiryDate.isAfter(currentDate) || expiryDate.isEqual(currentDate)
            val period = Period.between(currentDate, expiryDate)
            val absPeriod = if (isFuture) period else Period.between(expiryDate, currentDate)

            val months = absPeriod.months
            val days = absPeriod.days

            val statusText = if (isFuture) {
                when {
                    months > 0 && days > 0 -> "$months month${if (months > 1) "s" else ""} \n $days day${if (days > 1) "s" else ""} remaining"
                    months > 0 -> "$months month${if (months > 1) "s" else ""} remaining"
                    days > 1 -> "$days days \n remaining"
                    days == 1 -> "1 day remaining"
                    days == 0 -> "Today last"
                    else -> ""
                }
            } else {
                when {
                    months > 0 && days > 0 -> "Expired before \n $months month${if (months > 1) "s" else ""} $days day${if (days > 1) "s" else ""}"
                    months > 0 -> "Expired before \n $months month${if (months > 1) "s" else ""}"
                    days == 1 -> "Expired before \n 1 day"
                    else -> "Expired before \n $days days"
                }
            }

            holder.expiryDate.text = statusText

            val totalDaysRemaining = expiryDate.toEpochDay() - currentDate.toEpochDay()
            holder.expiryDate.setTextColor(
                if (totalDaysRemaining > 5)
                    contextFromFrag.getColor(R.color.green)
                else
                    contextFromFrag.getColor(R.color.red)
            )
        } else {
            holder.expiryDate.text = "No Expiry Date"
            holder.expiryDate.setTextColor(contextFromFrag.getColor(R.color.black)) // default color
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
    }
}
