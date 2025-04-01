package expirydatemanager.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.others.ExpiryItem
import nithra.tamil.calendar.expirydatemanager.R

class ExpiryItemAdapter_cat(
    private val itemList: List<ExpiryItem>,
    private val onItemClick: (ExpiryItem) -> Unit
) : RecyclerView.Adapter<ExpiryItemAdapter_cat.ItemViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_cat, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemName.text = item.itemName
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }


    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.tvItemName)
    }
}
