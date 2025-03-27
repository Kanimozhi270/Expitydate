package expirydatemanager.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.Adapter.ExpiryItemAdapter_home
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.pojo.ItemList
import expirydatemanager.retrofit.ExpiryRepository
import expirydatemanager.retrofit.ExpiryRetrofitInterface
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel

class RenewFragment_home : Fragment() {

    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    var viewModelFactory = ExpiryViewModelFactory(repository)
    lateinit var addItemViewModel: ExpiryViewModel
    private val itemList = mutableListOf<ItemList>()  // Store data as a List of Item objects
    private val itemListnew = mutableListOf<ItemList.GetList>()  // Store data as a List of Item objects
    private lateinit var recyclerView: RecyclerView
    lateinit var fragmentActivity: AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expiry_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewExpiry)
        val contentLayout = view.findViewById<LinearLayout>(R.id.contentLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //viewmodel intialize
        addItemViewModel = ViewModelProvider(this, viewModelFactory).get(ExpiryViewModel::class.java)

        val adapter = ExpiryItemAdapter_home(itemListnew, requireContext(), "renew item") { itemId ->
            showDeleteConfirmationDialog(itemId)
        }

        recyclerView.adapter = adapter

        if (ExpiryUtils.isNetworkAvailable(requireContext())) {
            ExpiryUtils.mProgress(requireActivity(), "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()
            val InputMap = HashMap<String,Any>()
            InputMap["action"] = "getlist"
            InputMap["user_id"] = "989015"
            InputMap["item_type"] = "1"
            InputMap["is_days"] = "0"

            addItemViewModel.fetchList1(InputMap)
           // addItemViewModel.deletelist(userId = 989015, 1, )
        } else {
            contentLayout.visibility = View.VISIBLE
        }

        // Observe itemList LiveData and update itemList with the raw data
        addItemViewModel.itemList1.observe(viewLifecycleOwner) { response ->
            println("addItemViewModel.itemList == $response")
            ExpiryUtils.mProgress.dismiss()

            // Clear the existing list and add new data
           // itemList.addAll(response)
            itemListnew.addAll(response.list!!)
            // Notify the adapter about data changes
            adapter.notifyDataSetChanged()
        }

        addItemViewModel.deletelistResponse.observe(viewLifecycleOwner) { response ->
            println("addItemViewModel.deletelistResponse == $response")
            ExpiryUtils.mProgress.dismiss()

            // Refresh list after delete
           // addItemViewModel.fetchList1(userId = 989015, item_id = 1, is_days = 0)
            adapter.notifyDataSetChanged()
        }


        return view
    }

    private fun showDeleteConfirmationDialog(itemId: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                ExpiryUtils.mProgress(requireActivity(), "Deleting item...", true).show()
              //  addItemViewModel.deletelist(userId = 989015, list_id = itemId)
            }
            .setNegativeButton("No", null)
            .show()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Now it's safe to call fragmentActivity
        if (context is AppCompatActivity) {
            fragmentActivity = context
        }
    }
}

