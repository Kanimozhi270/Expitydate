package nithra.tamil.calendar.expirydatemanager.Activity

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import nithra.tamil.calendar.Others.ItemAdapter_editdelete
import nithra.tamil.calendar.expirydatemanager.Notification.NotificationReceiver
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityAdditemBinding
import java.util.*

class AddItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdditemBinding
    private lateinit var db: SQLiteDatabase
    private lateinit var adapter: ItemAdapter_editdelete

    private var selectedItemType = ""
    private var selectedReminder = "same day"
    private var categories = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdditemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.title = HtmlCompat.fromHtml(
            "<b>Add Item", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setSupportActionBar(binding.appBar)
        supportActionBar!!.title = HtmlCompat.fromHtml(
            "<b>Add Item", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)
        db.execSQL("PRAGMA foreign_keys=ON;")
        createTablesIfNotExist()

        val itemNames = fetchDataFromTable("itemnames", "itemname")
        categories = fetchDataFromTable("categorys", "categoryname", selectedItemType)

        binding.etItemName.setOnClickListener {
            showSelectionDialog("Select Item Name", itemNames) { selectedName ->
                binding.etItemName.text = selectedName
            }
        }
        println("selectttttrewmn  ====$selectedItemType")
        binding.btnExpiryItem.setOnClickListener {
            selectedItemType = "Expiry Item"
            println("selecttttt====$selectedItemType")
        }

        binding.btnRenewItem.setOnClickListener {
            selectedItemType = "Renew Item"
            println("selectttttrew====$selectedItemType")
        }

        binding.etExpiryDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                    binding.etExpiryDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.etNotifyTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.etNotifyTime.setText(selectedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePickerDialog.show()
        }

        binding.btnSameDay.setOnClickListener {
            selectedReminder = "same day"
        }
        binding.btn2DaysBefore.setOnClickListener {
            selectedReminder = "2 days before"
        }
        binding.btn1WeekBefore.setOnClickListener {
            selectedReminder = "1 week before"
        }
        binding.customReminder.setOnClickListener {
            // Open dialog to pick custom reminder if needed
        }



        binding.spCategories.setOnClickListener {
            showSelectionDialog("Select Category", categories) { selectedCategory ->
                binding.spCategories.text = selectedCategory
            }
        }

        binding.btnAddItem.setOnClickListener {
            saveItem()
        }
    }



    // Create tables if they do not exist
    private fun createTablesIfNotExist() {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "itemname TEXT NOT NULL, " +
                    "itemtype TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "expiry_date TEXT NOT NULL, " +
                    "reminderbefore TEXT NOT NULL, " +
                    "notify_time TEXT, " +
                    "note TEXT)"
        )
        db.execSQL("CREATE TABLE IF NOT EXISTS itemnames (id INTEGER PRIMARY KEY AUTOINCREMENT, itemname TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS categorys (id INTEGER PRIMARY KEY AUTOINCREMENT, categoryname TEXT NOT NULL, itemtype TEXT NOT NULL)")
    }


    private fun fetchDataFromTable(tableName: String, columnName: String, filterType: String? = null): List<String> {
        val dataList = mutableListOf<String>()
        val query = if (filterType != null) {
            "SELECT $columnName FROM $tableName WHERE itemtype = ?"
        } else {
            "SELECT $columnName FROM $tableName"
        }
        val cursor = if (filterType != null) {
            db.rawQuery(query, arrayOf(filterType))
        } else {
            db.rawQuery(query, null)
        }
        if (cursor.moveToFirst()) {
            do {
                dataList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return dataList
    }


    private fun showSelectionDialog(title: String, items: List<String>, onItemSelected: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_selection, null)
        builder.setView(dialogView)

        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val btnCustomAction = dialogView.findViewById<Button>(R.id.btnCustomAction)

        val dialog = builder.create()
        val tableName = if (title.contains("Category", true)) "categorys" else "itemnames"
        val columnName = if (tableName == "categorys") "categoryname" else "itemname"

        val itemList = fetchDataFromTable(tableName, columnName).toMutableList()  // Fetch initial data

        adapter = ItemAdapter_editdelete(this, itemList,
            onEdit = { item ->
                showCreateDialog(tableName, item) { updatedItems ->
                    adapter.updateItems(updatedItems)
                }
            },
            onDelete = { item ->
                deleteItem(tableName, item)
                val updatedItems = fetchDataFromTable(tableName, columnName)
                adapter.updateItems(updatedItems)
            },
            onItemClick = { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        etSearch.addTextChangedListener {
            adapter.filter(it.toString())
        }

        btnCustomAction.setOnClickListener {
            dialog.dismiss()
            showCreateDialog(tableName) { updatedItems ->
                adapter.updateItems(updatedItems)  // Refresh RecyclerView with new data
            }
        }

        dialog.show()
    }


    // Delete item from database
    private fun deleteItem(tableName: String, item: String) {
        val column = if (tableName == "itemnames") "itemname" else "categoryname"
        val result = db.delete(tableName, "$column=?", arrayOf(item))
        if (result > 0) {
            Toast.makeText(this, "Deleted $item", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("DatabaseError", "Failed to delete $item from $tableName")
        }
    }

    private fun showCreateDialog(tableName: String, existingItem: String? = null, onItemsUpdated: ((List<String>) -> Unit)? = null) {
        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dialog.setContentView(R.layout.dialog_create_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etItemName = dialog.findViewById<EditText>(R.id.etItemName)
        val btnCreate = dialog.findViewById<Button>(R.id.btnCreate)

        if (existingItem != null) {
            etItemName.setText(existingItem)
            btnCreate.text = "Update"
        }

        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            if (name.isNotEmpty()) {
                val contentValues = ContentValues().apply {
                    if (tableName == "itemnames") put("itemname", name)
                    else {
                        put("categoryname", name)
                        put("itemtype", selectedItemType)
                    }
                }
                val result = if (existingItem != null) {
                    val column = if (tableName == "itemnames") "itemname" else "categoryname"
                    db.update(tableName, contentValues, "$column=?", arrayOf(existingItem))
                } else {
                    db.insert(tableName, null, contentValues)
                }

                if (result != -1L) {
                    val updatedItems = fetchDataFromTable(tableName, if (tableName == "itemnames") "itemname" else "categoryname")
                    onItemsUpdated?.invoke(updatedItems)  // Send updated data back to showSelectionDialog
                    dialog.dismiss()
                } else Toast.makeText(this, "Failed to save!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    // Save item with all details

    private fun saveItem() {
        val itemName = binding.etItemName.text.toString().trim()
        val category = binding.spCategories.text.toString().trim()
        val expiryDate = binding.etExpiryDate.text.toString().trim()
        val notifyTime = binding.etNotifyTime.text.toString().trim()
        val reminderBefore = selectedReminder.trim()
        val note = binding.etNote.text.toString().trim()

        if (itemName.isNotEmpty() && category.isNotEmpty() && expiryDate.isNotEmpty() && notifyTime.isNotEmpty()) {
            val contentValues = ContentValues().apply {
                put("itemname", itemName)
                put("itemtype", selectedItemType)
                put("category", category)
                put("expiry_date", expiryDate)
                put("reminderbefore", reminderBefore)
                put("notify_time", notifyTime)
                put("note", note)
            }
            val result = db.insert("items", null, contentValues)
            if (result != -1L) {
                Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show()
                scheduleNotification(itemName, expiryDate, notifyTime) // Schedule notification
                finish()
            } else {
                Toast.makeText(this, "Failed to add item!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNotification(itemName: String, expiryDate: String, notifyTime: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("itemName", itemName)
            putExtra("notificationId", itemName.hashCode()) // Unique ID
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, itemName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Convert expiryDate and notifyTime to Calendar format
        val dateParts = expiryDate.split("-") // Expected format: dd-MM-yyyy
        val timeParts = notifyTime.split(":") // Expected format: HH:mm

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
            set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-based
            set(Calendar.YEAR, dateParts[2].toInt())
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
        }

        // Set Alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

}
