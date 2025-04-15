package expirydatemanager.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import expirydatemanager.adapter.ExpiryFullViewAdapter
import expirydatemanager.fragment.ExpiryViewModelFactory
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.pojo.ItemList
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityExpiryFullViewBinding
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class Expiry_FullView : AppCompatActivity() {

    private lateinit var binding: ActivityExpiryFullViewBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    var categoriesList: Map<String, Any> = hashMapOf()
    private val apiService = ExpiryRetrofitInstance.instance

    private val addItemViewModel: ExpiryViewModel by viewModels {
        ExpiryViewModelFactory(repository)
    }
    private var itemListNew: MutableList<ItemList.GetList> = mutableListOf()

    private lateinit var itemNamesAdapter: ExpiryFullViewAdapter
    var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiryFullViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.title = HtmlCompat.fromHtml(
            "<b>Add Item", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setSupportActionBar(binding.appBar)
        supportActionBar!!.title = HtmlCompat.fromHtml(
            "<b>Add Item", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        val itemName = intent.getStringExtra("item_name") ?: "N/A"
        val expiryDate = intent.getStringExtra("action_date") ?: "N/A"
        val actionDateStr = intent.getStringExtra("action_date") ?: ""
        val reminderBefore = if (actionDateStr.isNotEmpty()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val actionDate = LocalDate.parse(actionDateStr, formatter)
                val currentDate = LocalDate.now()
                val daysBetween = ChronoUnit.DAYS.between(currentDate, actionDate)

                when {
                    daysBetween > 1 -> "$daysBetween days remaining"
                    daysBetween == 1L -> "1 day remaining"
                    daysBetween == 0L -> "Today last"
                    daysBetween < 0L -> "Expired before ${-daysBetween} days"
                    else -> "N/A"
                }
            } catch (e: Exception) {
                "Invalid Date"
            }
        } else {
            "No Expiry Date"
        }

        val notifyTime = intent.getStringExtra("notify_time") ?: "N/A"
        val note = intent.getStringExtra("remark") ?: "N/A"
        val intentCategory = intent.getStringExtra("category_id") ?: "N/A"
        val itemType = intent.getStringExtra("item_type") ?: "N/A"
        val itemId = intent.getStringExtra("item_id") ?: "N/A"
        val catItemType = if (itemType == "expiry item") "1" else "2"

        binding.reminderBefore.text = reminderBefore



        addItemViewModel.fetchCategories(ExpiryUtils.userId, catItemType)

        if (catItemType == "1") {
            addItemViewModel.expiryCategories.observe(this@Expiry_FullView) { categories ->
                categoriesList = categories
                category = getCategoryNameFromId(intentCategory, categories)
                binding.category.text = category
            }
        } else {
            addItemViewModel.renewCategories.observe(this@Expiry_FullView) { categories ->
                categoriesList = categories
                category = getCategoryNameFromId(intentCategory, categories)
                binding.category.text = category
            }
        }



        println("itemidddd====$itemId")

        binding.okButton.setOnClickListener {
            finish()
        }

        println("🔍 Item ID received in FullView = $itemId")

        // Fetch data from server
        if (ExpiryUtils.isNetworkAvailable(this)) {
            ExpiryUtils.mProgress(this, "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()
            var cate_id = "" + intent.getStringExtra("category_id")
            val inputMap = HashMap<String, Any>().apply {
                put("action", "getlist")
                put("user_id", ExpiryUtils.userId)
                put("category_id", cate_id)
                put("item_type", itemType)
                put("item_id", itemId)
                put("is_days", "0")
            }
            println("inputMap ==$inputMap")
            addItemViewModel.fetchList1(inputMap)
        } else {
            Toast.makeText(this, "இணையதள இணைப்பு இல்லை", Toast.LENGTH_SHORT).show()
        }

        //observe the list response
        addItemViewModel.itemList1.observe(this) { response ->
            ExpiryUtils.mProgress.dismiss()

            itemListNew.clear()
            if (!response.list.isNullOrEmpty()) {
                itemListNew.addAll(response.list)
            }
            /*
                        if (itemListNew.isEmpty()) {
                            binding.contentLayout.visibility = View.VISIBLE
                            binding.recyclerViewExpiry.visibility = View.GONE
                        } else {
                            binding.contentLayout.visibility = View.GONE
                            binding.recyclerViewExpiry.visibility = View.VISIBLE
                        }*/
            itemNamesAdapter.notifyDataSetChanged()
        }

        binding.itemName.text = itemName
        binding.expiryDate.text = formatDate(expiryDate)
        //  binding.reminderBefore.text = "$reminderBefore" + "Days"
        binding.notifyTime.text = "$notifyTime"
        binding.notes.text = "$note"
        binding.category.text = "" + category
        // binding.category.text = getCategoryIdFromName(binding.category.text.toString().trim()).toString()
        binding.itemType.text = "$itemType"
    }


    private fun getCategoryNameFromId(id: String, categoriesMap: Map<String, Any>): String {
        println("getCategoryNameFromId -> categoryId: $id")
        println("Categories Map: $categoriesMap")

        val category = categoriesMap.entries.find { entry ->
            val keyId = entry.key.toDoubleOrNull()?.toInt()
            val targetId = id.toIntOrNull()
            keyId != null && keyId == targetId
        }?.value as? Map<String, Any>

        return category?.get("category") as? String ?: "Unknown Category"
    }


    private fun formatDate(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(inputDate)
            date?.let { outputFormat.format(it) } ?: inputDate
        } catch (e: Exception) {
            inputDate
        }
    }


    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(menuItem)


    }

}