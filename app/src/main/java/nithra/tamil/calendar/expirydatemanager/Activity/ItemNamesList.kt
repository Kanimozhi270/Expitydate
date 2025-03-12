package nithra.tamil.calendar.expirydatemanager.Activity

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import nithra.tamil.calendar.expirydatemanager.Adapter.ItemNamesAdapter
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityItemNamesListBinding

data class ItemData(
    val id: Int,
    val itemName: String,
    val expiryDate: String,
    val reminderBefore: String
)

class ItemNamesList : AppCompatActivity() {

    private lateinit var binding: ActivityItemNamesListBinding
    private lateinit var db: SQLiteDatabase
    private lateinit var itemNamesAdapter: ItemNamesAdapter
    private lateinit var itemNames: MutableList<ItemData>

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

        // Create or open database
        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)

        // Fetch item data from the database
        itemNames = fetchItemNames().toMutableList()

        // Set up RecyclerView
        itemNamesAdapter = ItemNamesAdapter(itemNames, db)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ItemNamesList)
            adapter = itemNamesAdapter
        }

        // Check if list is empty
        checkEmptyState()
    }

    // Method to fetch item names from the database
    private fun fetchItemNames(): List<ItemData> {
        val itemList = mutableListOf<ItemData>()
        val cursor: Cursor = db.rawQuery("SELECT id, itemname, expiry_date, reminderbefore FROM items", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val itemName = cursor.getString(1)
                val expiryDate = cursor.getString(2)
                val reminderBefore = cursor.getString(3)
                itemList.add(ItemData(id, itemName, expiryDate, reminderBefore))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return itemList
    }

    // Show/hide tvEmptyMessage based on data availability
    fun checkEmptyState() {
        if (itemNames.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}

