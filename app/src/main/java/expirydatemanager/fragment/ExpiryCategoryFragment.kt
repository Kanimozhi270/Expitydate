package expirydatemanager.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.Adapter.ExpiryItemAdapter_cat
import expirydatemanager.activity.ExpiryItemList
import expirydatemanager.others.ExpiryItem
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel


class ExpiryCategoryFragment : Fragment() {

    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    var viewModelFactory = ExpiryViewModelFactory(repository)
    lateinit var addItemViewModel: ExpiryViewModel

    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<ExpiryItem>()

    lateinit var fragmentActivity: AppCompatActivity
    private lateinit var itemType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expiry_cat, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewExpiry)
        val contentLayout = view.findViewById<LinearLayout>(R.id.contentLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addItemViewModel =
            ViewModelProvider(this, viewModelFactory).get(ExpiryViewModel::class.java)

        val adapter = ExpiryItemAdapter_cat(itemList) { clickedItem ->
            val intent = Intent(requireContext(), ExpiryItemList::class.java)
            intent.putExtra("category_id", clickedItem.id)
            intent.putExtra("category_name", clickedItem.itemName)
            intent.putExtra("item_type", clickedItem.itemType)

            startActivity(intent)

            println("catid===${clickedItem.id}")
            println("catname===${clickedItem.itemName}")
            println("itemType===${clickedItem.itemType}")

        }

        recyclerView.adapter = adapter


        if (ExpiryUtils.isNetworkAvailable(requireContext())) {
            ExpiryUtils.mProgress(requireActivity(), "ஏற்றுகிறது. காத்திருக்கவும் ", true)
            addItemViewModel.fetchCategories(userId = 989015, itemType = "expiry item")

        } else {
            contentLayout.visibility = View.VISIBLE

        }

        addItemViewModel.categories.observe(viewLifecycleOwner) { response ->
            println("addItemViewModel.categories == $response")

            val categories = (response["Category"] as? List<Map<String, Any>>)?.map {
                val id = (it["id"] as? Double)?.toInt() ?: 0
                val category = it["category"]?.toString() ?: ""
                itemType = it["item_type"]?.toString() ?: ""
                ExpiryItem(id, category, "", itemType)
            } ?: emptyList()

            ExpiryUtils.mProgress.dismiss()

            itemList.clear()
            itemList.addAll(categories)
            adapter.notifyDataSetChanged()

            // ✅ Show empty view if list is empty
            if (itemList.isEmpty()) {
                contentLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                contentLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
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