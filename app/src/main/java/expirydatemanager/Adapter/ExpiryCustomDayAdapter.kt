package nithra.tamil.calendar.expirydatemanager.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.R

class ExpiryCustomDayAdapter(
    private val days: List<String>,
    var context: Context,
    var defaultSelectedDay: String?,
    private val onDaySelected: (String) -> Unit
) : RecyclerView.Adapter<ExpiryCustomDayAdapter.DayViewHolder>() {

    private var selectedPosition = -1

    init {
        // Set selected position to defaultSelectedDay if provided
        defaultSelectedDay?.let {
            selectedPosition = days.indexOf(it)
        }
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDay: TextView = itemView.findViewById(R.id.txtDay)
        val txtDateLay: CardView = itemView.findViewById(R.id.txtDateLay)
        val txtDateLay1: CardView = itemView.findViewById(R.id.txtDateLay1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_aanmeega_date_item, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, @SuppressLint("RecyclerView") position: Int) {

        if (days[position] == "") {
            holder.itemView.isEnabled = false
            holder.txtDay.visibility = View.INVISIBLE
            holder.txtDateLay.visibility = View.INVISIBLE
            holder.txtDateLay1.visibility = View.INVISIBLE
        } else {
            println("position == $position")
            println("days position == ${days[position]}")
            val daysPOs = days[position]
            if (daysPOs.toInt() <= 9) {
                holder.txtDay.text = "0" + days[position]
            } else {
                holder.txtDay.text = days[position]
            }
            holder.itemView.isEnabled = true
            holder.txtDay.visibility = View.VISIBLE
            holder.txtDateLay.visibility = View.VISIBLE
            holder.txtDateLay1.visibility = View.VISIBLE
        }


        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onDaySelected(days[position])
        }
        if (selectedPosition == position) {
            holder.txtDateLay.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.exp_brown)
            )
            holder.txtDay.setTextColor(Color.WHITE)
        } else {
            holder.txtDateLay.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.white)
            )
            holder.txtDay.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount(): Int = days.size

    // Function to set the selected date programmatically
    fun setSelectedDate(day: String) {
        selectedPosition = days.indexOf(day)
        notifyDataSetChanged()
    }

}
