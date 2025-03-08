package nithra.tamil.calendar.expirydatemanager

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityItemNamesListBinding

class ItemNamesList : AppCompatActivity() {

    private lateinit var binding: ActivityItemNamesListBinding
    private lateinit var db: SQLiteDatabase
    private lateinit var itemNamesAdapter: ItemNamesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemNamesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create or open database
        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)

        // Fetch item names from the database
        val itemNames = fetchItemNames()

        // Set up RecyclerView
        itemNamesAdapter = ItemNamesAdapter(itemNames)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ItemNamesList)
            adapter = itemNamesAdapter
        }
    }

    // Method to fetch item names from the database
    private fun fetchItemNames(): List<String> {
        val itemList = mutableListOf<String>()
        val cursor: Cursor = db.rawQuery("SELECT itemname, expiry_date, reminderbefore FROM items", null)
        if (cursor.moveToFirst()) {
            do {
                itemList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return itemList
    }
}
