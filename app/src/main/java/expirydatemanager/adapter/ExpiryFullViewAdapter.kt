package expirydatemanager.adapter

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


class ExpiryFullViewAdapter(
    private val itemList: MutableList<ItemList.GetList>,
    private val launcher: ActivityResultLauncher<Intent>,
    private val context: Context,
    private val onDelete: (Int, String) -> Unit
) : RecyclerView.Adapter<ExpiryFullViewAdapter.ItemViewHolder>()
{

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.item_name)
        val expiryOn: TextView = itemView.findViewById(R.id.expiry_date)
        val reminderBefore: TextView = itemView.findViewById(R.id.reminder_before)
        val notifytime: TextView = itemView.findViewById(R.id.notify_time)
        val notes: TextView = itemView.findViewById(R.id.notes)
        val itemtype: TextView = itemView.findViewById(R.id.item_type)
        val category: TextView = itemView.findViewById(R.id.category)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_expiry_full_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        holder.itemNameTextView.text = item.itemName ?: "No Name"
        holder.expiryOn.text = "${item.actionDate ?: "N/A"}"
        holder.reminderBefore.text = "${item.reminderType ?: "N/A"}"
        holder.notifytime.text = "${item.notifyTime ?: "N/A"}"
        holder.notes.text = "${item.remark ?: "N/A"}"


    }

    override fun getItemCount(): Int = itemList.size




}