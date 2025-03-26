package nithra.tamil.calendar.expirydatemanager.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.others.Item
import nithra.tamil.calendar.expirydatemanager.others.Utils
import nithra.tamil.calendar.expirydatemanager.Adapter.ExpiryItemAdapter_cat
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryDateViewModel

class RenewCategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()
    private val addItemViewModel: ExpiryDateViewModel by viewModels()
    lateinit var fragmentActivity: AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_renew_cat, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewRenew)
        val contentLayout = view.findViewById<LinearLayout>(R.id.contentLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ExpiryItemAdapter_cat(itemList)
        recyclerView.adapter = adapter

        if (!Utils.isNetworkAvailable(requireContext())){
            Utils.mProgress(requireActivity(), "ஏற்றுகிறது. காத்திருக்கவும் ", true)
            addItemViewModel.fetchCategories(userId = 989015, itemType = "renew item")
        }else{
            contentLayout.visibility=View.VISIBLE

        }


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

            Utils.mProgress.dismiss()
            itemList.clear()
            itemList.addAll(categories)
            adapter.notifyDataSetChanged()
        }

        return view
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Now it's safe to call fragmentActivity
        if (context is AppCompatActivity) {
            fragmentActivity = context
        }
    }
}

