package nithra.tamil.calendar.expirydatemanager.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.Others.Item
import nithra.tamil.calendar.expirydatemanager.R

class ItemAdapter_home(private val itemList: List<Item>) : RecyclerView.Adapter<ItemAdapter_home.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemName.text = item.itemName
        holder.expiryDate.text = item.expiryDate
    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.tvItemName)
        val expiryDate: TextView = view.findViewById(R.id.tvExpiryDate)
    }
}
