package nithra.tamil.calendar.expirydatemanager.fragment

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.Others.Item
import nithra.tamil.calendar.expirydatemanager.Adapter.ItemAdapter_cat
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryDateViewModel

class RenewCategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()
    private val addItemViewModel: ExpiryDateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_renew_cat, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewRenew)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ItemAdapter_cat(itemList)
        recyclerView.adapter = adapter

        addItemViewModel.fetchCategories(userId = 989015, itemType = "renew item")

        addItemViewModel.categories.observe(viewLifecycleOwner) { response ->
            println("response=====${response}")
            val categories = (response["Category"] as? List<Map<String, Any>>)?.map {
                // Safely get the 'id' and convert to Int (handle float or decimal cases)
                val id = (it["id"] as? Double)?.toInt() ?: 0  // Convert Double to Int safely, default to 0 if not present

                // Safely get 'category' and 'item_type' and convert to String
                val category = it["category"]?.toString() ?: ""
                val itemType = it["item_type"]?.toString() ?: ""

                // Now map to your Item class
                Item(id, category, itemType)
            } ?: emptyList()

            itemList.clear()
            itemList.addAll(categories)
            adapter.notifyDataSetChanged()
        }

        return view
    }
}

