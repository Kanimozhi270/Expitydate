package expirydatemanager.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import expirydatemanager.Adapter.ExpiryListAdapter
import expirydatemanager.fragment.ExpiryViewModelFactory
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.pojo.ItemList
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityItemNamesListBinding
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel

class ExpiryItemList : AppCompatActivity() {

    private lateinit var binding: ActivityItemNamesListBinding
    private lateinit var itemNamesAdapter: ExpiryListAdapter
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }

    private val addItemViewModel: ExpiryViewModel by viewModels {
        ExpiryViewModelFactory(repository)
    }

    private var itemListNew: MutableList<ItemList.GetList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemNamesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get category ID from intent
        val categoryId = intent.getIntExtra("category_id", -1)
        val categoryName = intent.getStringExtra("category_name") ?: "All Items"
        val itemType = intent.getStringExtra("item_type") ?: "expiry item"

        // Set toolbar title
        binding.appBar.title = HtmlCompat.fromHtml(
            "<b>$categoryName", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setSupportActionBar(binding.appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val itemAdded = result.data?.getBooleanExtra("item_added", false) == true
                    if (itemAdded) {
                        refreshList(
                            categoryId, categoryName, itemType
                        ) // 🔄 When coming back from edit/add
                    } else {
                        // Optional: handle other result cases here if needed
                        refreshList(categoryId, categoryName, itemType)
                    }
                }
            }


        // Fetch data from server
        if (ExpiryUtils.isNetworkAvailable(this)) {
            ExpiryUtils.mProgress(this, "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()

            val inputMap = HashMap<String, Any>().apply {
                put("action", "getlist")
                put("user_id", ExpiryUtils.userId)
                put("category_id", categoryId)
                put("item_type", itemType)
                put("is_days", "0")/*if (categoryId != -1) {
                    put("category_id", categoryId.toString())
                }*/
            }

            addItemViewModel.fetchList1(inputMap)
        } else {
            Toast.makeText(this, "இணையதள இணைப்பு இல்லை", Toast.LENGTH_SHORT).show()
        }

        // Set up RecyclerView
        itemNamesAdapter = ExpiryListAdapter(
            itemListNew, launcher, this, onDelete = { itemId, itemType ->
                if (itemType == "item_type") {
                    deleteItem(itemId, itemType) {
                        itemListNew.removeAll { it.id == itemId }
                        itemNamesAdapter.notifyDataSetChanged()
                    }
                }
            }

        )
        binding.recyclerViewExpiry.apply {
            layoutManager = LinearLayoutManager(this@ExpiryItemList)
            adapter = itemNamesAdapter
        }

        // Observe the list response
        addItemViewModel.itemList1.observe(this) { response ->
            ExpiryUtils.mProgress.dismiss()

            itemListNew.clear()
            if (!response.list.isNullOrEmpty()) {
                itemListNew.addAll(response.list)
            }



            if (itemListNew.isEmpty()) {
                binding.contentLayout.visibility = View.VISIBLE
                binding.recyclerViewExpiry.visibility = View.GONE
            } else {
                binding.contentLayout.visibility = View.GONE
                binding.recyclerViewExpiry.visibility = View.VISIBLE
            }
            itemNamesAdapter.notifyDataSetChanged()
        }

    }


    private fun deleteItem(itemId: Int, itemType: String, onSuccess: () -> Unit) {
        val params = hashMapOf<String, Any>(
            "action" to "deleteList", "user_id" to ExpiryUtils.userId, "list_id" to itemId
        )
        addItemViewModel.deletelist(ExpiryUtils.userId, itemId, params)

        addItemViewModel.deletelistResponse.observeOnce(this) { response ->
            val status = response["status"]?.toString()
            if (status == "success") {
                Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_SHORT).show()
                onSuccess.invoke()

                // 💡 Update UI visibility here
                if (itemListNew.isEmpty()) {
                    binding.contentLayout.visibility = View.VISIBLE
                    binding.recyclerViewExpiry.visibility = View.GONE
                } else {
                    binding.contentLayout.visibility = View.GONE
                    binding.recyclerViewExpiry.visibility = View.VISIBLE
                }

            } else {
                Toast.makeText(this, "Failed to delete!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun refreshList(categoryId: Int, categoryName: String, itemType: String) {
        val inputMap = HashMap<String, Any>().apply {
            put("action", "getlist")
            put("user_id", ExpiryUtils.userId)
            put("category_id", categoryId)
            put("item_type", itemType)
            put("is_days", "0")
        }

        addItemViewModel.fetchList1(inputMap)
    }


    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(menuItem)
    }
}