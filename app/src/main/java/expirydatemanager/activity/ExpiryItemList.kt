package expirydatemanager.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
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

    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }

    private val addItemViewModel: ExpiryViewModel by viewModels {
        ExpiryViewModelFactory(repository)
    }

    private var itemListNew: MutableList<ItemList.GetList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemNamesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set toolbar title
        binding.appBar.title = HtmlCompat.fromHtml(
            "<b>All Items List", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setSupportActionBar(binding.appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get category ID from intent
        val categoryId = intent.getIntExtra("category_id", -1)

        // Fetch data from server
        if (ExpiryUtils.isNetworkAvailable(this)) {
            ExpiryUtils.mProgress(this, "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()

            val inputMap = HashMap<String, Any>().apply {
                put("action", "getlist")
                put("user_id", "989015")
                put("item_id", "0")
                put("is_days", "0")
                if (categoryId != -1) {
                    put("category_id", categoryId.toString())
                }
            }

            addItemViewModel.fetchList1(inputMap)
        } else {
            Toast.makeText(this, "இணையதள இணைப்பு இல்லை", Toast.LENGTH_SHORT).show()
        }

        // Set up RecyclerView
        itemNamesAdapter = ExpiryListAdapter(itemListNew)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpiryItemList)
            adapter = itemNamesAdapter
        }

        // Observe the list response
        addItemViewModel.itemList1.observe(this) { response ->
            ExpiryUtils.mProgress.dismiss()

            if (response.status == "success" && !response.list.isNullOrEmpty()) {
                itemListNew.clear()
                itemListNew.addAll(response.list)
                itemNamesAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "தகவல் இல்லை", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(menuItem)
    }
}
