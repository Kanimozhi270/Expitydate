package expirydatemanager.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

class RenewCategoryFragment : Fragment() {
    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    var viewModelFactory = ExpiryViewModelFactory(repository)
    lateinit var addItemViewModel: ExpiryViewModel

    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<ExpiryItem>()

    lateinit var fragmentActivity: AppCompatActivity
    private lateinit var itemType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sm_expiry_fragment_expiry_cat, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewExpiry)
        val contentLayout = view.findViewById<LinearLayout>(R.id.contentLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addItemViewModel =
            ViewModelProvider(this, viewModelFactory).get(ExpiryViewModel::class.java)


        val adapter = ExpiryItemAdapter_cat(itemList) { clickedItem ->
            println("click item type====${clickedItem.itemType}")
            val itemType = clickedItem.itemType.toDoubleOrNull()?.toInt() ?: 0

            val intent = Intent(requireContext(), ExpiryItemList::class.java)
            intent.putExtra("category_id", clickedItem.id)
            intent.putExtra("category_name", clickedItem.itemName)
            intent.putExtra("item_type", itemType.toString())

            startActivity(intent)

            println("catid===${clickedItem.id}")
            println("catname===${clickedItem.itemName}")
            println("itemType===${itemType}")

        }


        recyclerView.adapter = adapter


        if (ExpiryUtils.isNetworkAvailable(requireContext())) {
            ExpiryUtils.mProgress(requireActivity(), "ஏற்றுகிறது. காத்திருக்கவும் ", true)
            addItemViewModel.fetchCategories(userId = ExpiryUtils.userId, itemType = "2")

        } else {
            contentLayout.visibility = View.VISIBLE
        }

        addItemViewModel.renewCategories.observe(viewLifecycleOwner) { categoriesMap ->
            println("addItemViewModel.renewCategories == $categoriesMap")

            val categories = categoriesMap.values.toList().mapNotNull {
                val map = it as? Map<String, Any>
                val id = (map?.get("id") as? Double)?.toInt() ?: return@mapNotNull null
                val category = map["category"]?.toString() ?: return@mapNotNull null
                val itemType = map["item_type"]?.toString() ?: ""
                ExpiryItem(id, category, "", itemType)
            }

            ExpiryUtils.mProgress.dismiss()

            itemList.clear()
            itemList.addAll(categories)
            recyclerView.adapter?.notifyDataSetChanged()

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