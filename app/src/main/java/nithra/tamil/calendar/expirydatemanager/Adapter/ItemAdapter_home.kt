package nithra.tamil.calendar.expirydatemanager.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.R

class ItemAdapter_home(private val itemList: List<Map<String, Any>>) :
    RecyclerView.Adapter<ItemAdapter_home.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        // Safely get the 'item_name' and 'reminder_type' from the Map
        val itemName = item["item_name"]?.toString() ?: "No Name"
        val reminderType = item["reminder_type"]?.toString() ?: "No Reminder"
        val expiry_on = item["action_date"]?.toString() ?: "No Expiry Date"
        val id = (item["id"])
        println("ID after conversionnn: $id")
        // Set the values to the TextViews in the ViewHolder
        holder.itemName.text = itemName
        holder.expiryDate.text = reminderType
        holder.expiry_on.text = expiry_on
    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemName)
        val expiryDate: TextView = view.findViewById(R.id.reminderBefore)
        val expiry_on: TextView = view.findViewById(R.id.expityOn)
    }
}






