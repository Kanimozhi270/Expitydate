package nithra.tamil.calendar.expirydatemanager.activity

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityExpiryFullViewBinding

class Expiry_FullView : AppCompatActivity() {
    private lateinit var db: SQLiteDatabase
    private lateinit var binding: ActivityExpiryFullViewBinding

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

        // Initialize database
        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)

        // Get item ID from intent
        val itemId = intent.getIntExtra("item_id", -1)

        if (itemId != -1) {
            loadItemDetails(itemId)
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(menuItem)


    }

    private fun loadItemDetails(itemId: Int) {
        val cursor = db.rawQuery("SELECT * FROM items WHERE id = ?", arrayOf(itemId.toString()))
        if (cursor.moveToFirst()) {
            val itemName = cursor.getString(cursor.getColumnIndexOrThrow("itemname"))
            val expiryDate = cursor.getString(cursor.getColumnIndexOrThrow("expiry_date"))
            val reminderBefore = cursor.getString(cursor.getColumnIndexOrThrow("reminderbefore"))
            val notifyTime = cursor.getString(cursor.getColumnIndexOrThrow("notify_time"))
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val itemType = cursor.getString(cursor.getColumnIndexOrThrow("itemtype"))

            binding.itemName.text = itemName
            binding.expiryDate.text = "Expiry Date: $expiryDate"
            binding.reminderBefore.text = "Reminder Before: $reminderBefore"
            binding.notifyTime.text = "Notify Time: $notifyTime"
            binding.notes.text = "Notes: $note"
            binding.category.text = "Category: $category"
            binding.itemType.text = "Item Type: $itemType"
        }
        cursor.close()
    }
}