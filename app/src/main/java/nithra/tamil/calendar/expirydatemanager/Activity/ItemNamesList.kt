package nithra.tamil.calendar.expirydatemanager.Activity

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import nithra.tamil.calendar.expirydatemanager.Adapter.ItemNamesAdapter
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityItemNamesListBinding
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryDateViewModel



class ItemNamesList : AppCompatActivity() {

    private lateinit var binding: ActivityItemNamesListBinding
    private lateinit var itemNamesAdapter: ItemNamesAdapter
    private val addItemViewModel: ExpiryDateViewModel by viewModels()
    var categoriesList: Map<String, Any> = hashMapOf()

    var itemNames: List<HashMap<String, Any>> = listOf()
    private var dialog_type = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemNamesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.title = HtmlCompat.fromHtml(
            "<b>All Items List", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setSupportActionBar(binding.appBar)
        supportActionBar!!.title = HtmlCompat.fromHtml(
            "<b>All Items List", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //get data from server
        addItemViewModel.fetchList(989015)
        // Observe item names from ViewModel
        addItemViewModel.itemList.observe(this, androidx.lifecycle.Observer { item ->


            itemNames  = item["list"] as List<HashMap<String, Any>>
            println("itemNames == $itemNames")


        })
        // Set up RecyclerView
        itemNamesAdapter = ItemNamesAdapter(itemNames)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ItemNamesList)
            adapter = itemNamesAdapter
        }


        // Observe categories from ViewModel
        addItemViewModel.categories.observe(this, androidx.lifecycle.Observer { categories ->
            categoriesList = categories
            println("catItem == $categoriesList")
            dialog_type = "Category name"
            // If empty, don't show the dialog
            if (categoriesList.isEmpty()) return@Observer

        })



    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
        }


        return super.onOptionsItemSelected(menuItem)
    }


}

