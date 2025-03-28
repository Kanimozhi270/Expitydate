package expirydatemanager.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var recyclerView: RecyclerView
    private val itemList =
        mutableListOf<ItemList.GetList>()  // Store data as a List of Item objects
    var adapter: ExpiryItemAdapter_home? = null
    lateinit var fragmentActivity: AppCompatActivity
    var progressDialog: Dialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expiry_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewExpiry)
        val contentLayout = view.findViewById<LinearLayout>(R.id.contentLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addItemViewModel =
            ViewModelProvider(this, viewModelFactory).get(ExpiryViewModel::class.java)

        if (adapter == null) {
            adapter = ExpiryItemAdapter_home(itemList, requireContext(), "expiry item") { itemId ->
                showDeleteConfirmationDialog(itemId)
            }
            recyclerView.adapter = adapter
        }
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val inputMap = hashMapOf<String, Any>(
                        "action" to "getlist",
                        "user_id" to "989015",
                        "item_type" to "2",
                        "is_days" to "0"
                    )
                    addItemViewModel.fetchList1(inputMap)
                }
            }

        addItemViewModel.deleteitemResponse.observe(viewLifecycleOwner) { response ->
            println("addItemViewModel.deleteitemResponse == $response")
            progressDialog!!.dismiss()  // Hide progress dialog here

            // Remove item locally from list (optional optimization)
            val deletedId = response["list_id"]?.toString()?.toIntOrNull() ?: -1
            itemList.removeAll { it.id == deletedId }
            adapter?.notifyDataSetChanged()

            // OR: Re-fetch the full list if needed
            val inputMap = HashMap<String, Any>()
            inputMap["action"] = "getlist"
            inputMap["user_id"] = "989015"
            inputMap["item_type"] = "2"
            inputMap["is_days"] = "0"
            addItemViewModel.fetchList1(inputMap)
        }



        if (ExpiryUtils.isNetworkAvailable(requireContext())) {
            ExpiryUtils.mProgress(requireActivity(), "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getlist"
            InputMap["user_id"] = "989015"
            InputMap["item_type"] = "2"
            InputMap["is_days"] = "0"

            addItemViewModel.fetchList1(InputMap)
            // addItemViewModel.deletelist(userId = 989015, 2, )
        } else {
            contentLayout.visibility = View.VISIBLE
        }

        // Observe itemList LiveData and update itemList with the raw data
        addItemViewModel.itemList1.observe(viewLifecycleOwner) { response ->
            ExpiryUtils.mProgress.dismiss()

            itemList.clear()
            itemList.addAll(response.list ?: emptyList())
            adapter?.notifyDataSetChanged()
        }

        addItemViewModel.itemNameResponse.observe(viewLifecycleOwner) { response ->
            ExpiryUtils.mProgress.dismiss()
            println(" itemNameResponse is called =$response")
            val inputMap = HashMap<String, Any>().apply {
                this["action"] = "getlist"
                this["user_id"] = 989015
                this["item_type"] = "2"
                this["is_days"] = "0"
            }
            addItemViewModel.fetchList1(inputMap)
        }




        addItemViewModel.deletelistResponse.observe(viewLifecycleOwner) { response ->
            progressDialog?.dismiss()

            val status = response["status"]?.toString()
            if (status == "success") {
                Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show()

                // 🔄 Refresh the list after successful delete
                val inputMap = hashMapOf<String, Any>(
                    "action" to "getlist",
                    "user_id" to "989015",
                    "item_type" to "2",
                    "is_days" to "0"
                )
                addItemViewModel.fetchList1(inputMap)
            } else {
                Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    fun refreshList() {
        val inputMap = hashMapOf<String, Any>(
            "action" to "getlist",
            "user_id" to "989015",
            "item_type" to "2",
            "is_days" to "0"
        )
        addItemViewModel.fetchList1(inputMap)
    }


    private fun showDeleteConfirmationDialog(itemId: Int) {

        val InputMap = HashMap<String, Any>()
        InputMap["action"] = "getlist"
        InputMap["user_id"] = "989015"
        InputMap["item_type"] = "1"

        progressDialog = ExpiryUtils.mProgress(requireActivity(), "Deleting item...", true)

        AlertDialog.Builder(requireContext()).setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->

                val params = HashMap<String, Any>().apply {
                    this["action"] = "deleteList"
                    this["user_id"] = 989015
                    this["list_id"] = itemId
                }
                progressDialog!!.show()
                addItemViewModel.deletelist(userId = 989015, list_id = itemId, params)
            }

            .setNegativeButton("No", null).show()

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Now it's safe to call fragmentActivity
        if (context is AppCompatActivity) {
            fragmentActivity = context
        }
    }
}

