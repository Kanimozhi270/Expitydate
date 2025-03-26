package nithra.tamil.calendar.expirydatemanager.activity

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.others.CustomDatePickerDialog
import nithra.tamil.calendar.expirydatemanager.Adapter.ExpiryItemAdapter_editdelete
import nithra.tamil.calendar.expirydatemanager.Notification.NotificationReceiver
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityAdditemBinding
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryDateViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdditemBinding
    private val addItemViewModel: ExpiryDateViewModel by viewModels()
    private lateinit var db: SQLiteDatabase
    private lateinit var adapter: ExpiryItemAdapter_editdelete
    private var selectedItemType = ""
    private var displayDate = ""
    private var selectedDate = ""
    private var selectedReminder = "same day"
    private var dialog_type = ""
    private var categories = HashMap<String, Any>()
    var itemNamesList: Map<String, Any> = hashMapOf()
    var categoriesList: Map<String, Any> = hashMapOf()
    private val expiryDateViewModel: ExpiryDateViewModel by viewModels()

    // Variables to store selected time values
    private var selectedHour: String = "HH"
    private var selectedMinute: String = "MM"
    private var selectedAmPm: String = "AM"

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

        // Set default selection to "Expiry Item"
        selectedItemType = "expiry item"
        changeColor(binding.btnExpiryItem, binding.expiryText, true)
        loadCategoriesForSelectedType()


        addItemViewModel.fetchItemNames(989015)
        // Observe item names from ViewModel
        addItemViewModel.itemNames.observe(this, androidx.lifecycle.Observer { itemNames ->
            println("itemNames == $itemNames")
            addItemViewModel.fetchItemNames(989015)

            itemNamesList = itemNames

            dialog_type = "Item name"

        })


        // Observe categories from ViewModel
        addItemViewModel.categories.observe(this, androidx.lifecycle.Observer { categories ->
            categoriesList = categories
            println("catItem == $categoriesList")
            dialog_type = "Category name"
            // If empty, don't show the dialog
            if (categoriesList.isEmpty()) return@Observer

        })

        setupReminderButtons(binding.btnSameDay)

        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)
        db.execSQL("PRAGMA foreign_keys=ON;")


        val hourList = listOf("HH") + (1..12).map { it.toString().padStart(2, '0') }
        val minuteList = listOf("MM") + (0..59).map { it.toString().padStart(2, '0') }
        val amPmList = listOf("AM", "PM") // AM/PM options

// Hour spinner
        val hourAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hourList)
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.JathagamSpinnerHour.adapter = hourAdapter

// Minute spinner
        val minuteAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, minuteList)
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.JathagamSpinnerMinute.adapter = minuteAdapter

        // Create an ArrayAdapter
        val amPmAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, amPmList)
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.JathagamSpinnerAmPM.adapter = amPmAdapter

        binding.etItemName.setOnClickListener {
            val GetItems =
                itemNamesList["Items"] as? MutableList<Map<String, Any>> ?: mutableListOf()
            println("GetItems == $GetItems")

            println("GetItems == $GetItems")
            showSelectionDialog("Select Item Name", GetItems) { selectedName ->
                binding.etItemName.setText(selectedName["item_name"] as String)
            }
        }
        println("selectttttrewmn  ====$selectedItemType")
        binding.btnExpiryItem.setOnClickListener {
            selectedItemType = "expiry item"
            changeColor(binding.btnExpiryItem, binding.expiryText, true)
            loadCategoriesForSelectedType()
        }
        binding.btnRenewItem.setOnClickListener {
            selectedItemType = "renew item"
            changeColor(binding.btnRenewItem, binding.renewText, true)
            loadCategoriesForSelectedType()
        }

        binding.etExpiryDate.setOnClickListener {
            /*  val calendar = Calendar.getInstance()
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
              datePickerDialog.show()*/

            val datePicker = CustomDatePickerDialog({ selectedManamagalDatePicker ->
                showDatePicker1(selectedManamagalDatePicker)
            }, binding.etExpiryDate.text.toString().trim())
            datePicker.show(supportFragmentManager, "datePicker")
        }

        /*     binding.etNotifyTime.setOnClickListener {
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
             }*/

        binding.btnSameDay.setOnClickListener {
            selectedReminder = "same day"
            setupReminderButtons(binding.btnSameDay)
        }
        binding.btn2DaysBefore.setOnClickListener {
            selectedReminder = "2 days before"
            setupReminderButtons(binding.btn2DaysBefore)
        }
        binding.btn1WeekBefore.setOnClickListener {
            selectedReminder = "1 week before"
            setupReminderButtons(binding.btn1WeekBefore)
        }

        binding.customReminder.setOnClickListener {
            val datePicker = CustomDatePickerDialog({ selectedManamagalDatePicker ->
                showCustomReminderDate(selectedManamagalDatePicker)
            }, binding.customdatetext.text.toString().trim())

            datePicker.show(supportFragmentManager, "datePicker")
            setupReminderButtons(binding.customReminder)

        }


        binding.spCategories.setOnClickListener {
            // loadCategoriesForSelectedType()
            val GetcategoryList = categoriesList["Category"] as MutableList<Map<String, Any>>
            showSelectionDialog("Select Category", GetcategoryList) { selectedCategory ->
                binding.spCategories.text = selectedCategory["category"] as String
            }
        }


        binding.btnAddItem.setOnClickListener {
            saveItemToServer()
        }

        setupTimeSpinners()
        // loadCategoriesForSelectedType()
    }

    private fun saveItemToServer() {
        if (!validateInputs()) return

        val notifyTime = getFormattedNotifyTime()
        val remark = binding.etNote.text.toString().trim()
        val formattedExpiryDate =
            convertDateToServerFormat(binding.etExpiryDate.text.toString().trim())
        val customDate = if (getReminderType(selectedReminder) == 0) formattedExpiryDate else ""

        val itemId = getItemIdFromName(binding.etItemName.text.toString().trim())
        val categoryId = getCategoryIdFromName(binding.spCategories.text.toString().trim())

        // Save the item to the server
        addItemViewModel.addListToServer(
            categoryId = categoryId,
            itemType = if (selectedItemType == "Expiry Item") 1 else 2,
            itemId = itemId,
            reminderType = getReminderType(selectedReminder),
            notifyTime = notifyTime,
            remark = remark,
            actionDate = formattedExpiryDate,
            listId = 0,
            customDate = customDate
        )

        // Schedule the notification based on reminder type
        scheduleNotification(
            itemName = binding.etItemName.text.toString(),
            expiryDate = formattedExpiryDate,
            notifyTime = notifyTime,
            reminderType = selectedReminder,
            customDate = customDate
        )

        // Reset fields after saving the item
        clearFields()
    }


    private fun validateInputs(): Boolean {
        val itemName = binding.etItemName.text.toString().trim()
        val category = binding.spCategories.text.toString().trim()
        val expiryDate = binding.etExpiryDate.text.toString().trim()
        val notifyTime = getFormattedNotifyTime()

        return when {
            itemName.isEmpty() -> showToast("Please enter Item Name!")
            category.isEmpty() -> showToast("Please select a Category!")
            expiryDate.isEmpty() -> showToast("Please select an Expiry Date!")
            notifyTime.contains("HH") || notifyTime.contains("MM") -> showToast("Please select Notify Time!")
            selectedReminder == "custom" && binding.customdatetext.text.toString().trim()
                .isEmpty() -> showToast("Please select a Custom Reminder Date!")

            else -> true
        }
    }

    private fun getFormattedNotifyTime(): String {
        val hour = binding.JathagamSpinnerHour.selectedItem.toString()
        val minute = binding.JathagamSpinnerMinute.selectedItem.toString()
        val amPm = binding.JathagamSpinnerAmPM.selectedItem.toString()
        return "$hour:$minute $amPm"
    }

    private fun convertDateToServerFormat(localDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val parsedDate = inputFormat.parse(localDate)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            ""
        }
    }

    private fun getReminderType(reminder: String): Int {
        return when (reminder.lowercase()) {
            "same day" -> 1
            "1 day before" -> 2
            "1 week before" -> 3
            else -> 0
        }
    }

    private fun getItemIdFromName(name: String): Int {
        val items = itemNamesList["Items"] as? List<Map<String, Any>> ?: return 0
        return items.find { it["item_name"] == name }?.get("id")?.toString()?.toIntOrNull() ?: 0
    }

    private fun getCategoryIdFromName(name: String): Int {
        val categories = categoriesList["Category"] as? List<Map<String, Any>> ?: return 0
        return categories.find { it["category"] == name }?.get("id")?.toString()?.toIntOrNull() ?: 0
    }

    private fun showToast(message: String): Boolean {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        return false
    }


    private fun showCustomReminderDate(selectedDatePickerDate: String) {
        val tamilLocale = Locale("ta", "IN")

        val inputFormat = SimpleDateFormat("dd MMMM yyyy", tamilLocale)
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val outputFormatServer = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        val date = inputFormat.parse(selectedDatePickerDate)
        val displayDate = date?.let { outputFormat.format(it) } ?: ""
        val selectedDate = date?.let { outputFormatServer.format(it) } ?: ""

        binding.customdatetext.text = displayDate // Display formatted date
        selectedReminder = displayDate
    }

    private fun clearFields() {
        // Clear the EditTexts
        binding.etItemName.text = "Select Item Name"
        binding.spCategories.text = "Select Catrgory"
        binding.etExpiryDate.text.clear()
        binding.etNote.text.clear()
        binding.customdatetext.text = ""

        // Reset the spinners (notify time and reminder type)
        binding.JathagamSpinnerHour.setSelection(0)
        binding.JathagamSpinnerMinute.setSelection(0)
        binding.JathagamSpinnerAmPM.setSelection(0)

        // Reset the reminder buttons
        selectedReminder = "same day"
        setupReminderButtons(binding.btnSameDay)

        // Reset the selected item type
        selectedItemType = ""

        // Optionally reset other selections if needed
    }


    private fun setupReminderButtons(btnSameDay: Button) {
        val defaultColor = ContextCompat.getColor(this, R.color.lightgray) // Default color
        val selectedColor = ContextCompat.getColor(this, R.color.btncolor) // Selected color

        val textDefaultColor = ContextCompat.getColor(this, android.R.color.black)
        val textSelectedColor = ContextCompat.getColor(this, android.R.color.white)

        val reminderButtons = listOf(
            binding.btnSameDay,
            binding.btn2DaysBefore,
            binding.btn1WeekBefore,
            binding.customReminder
        )
        highlightSelectedButton(
            btnSameDay,
            defaultColor,
            selectedColor,
            textDefaultColor,
            textSelectedColor,
            reminderButtons
        )

    }


    fun highlightSelectedButton(
        selectedButton: Button,
        defaultColor: Int,
        selectedColor: Int,
        textDefaultColor: Int,
        textSelectedColor: Int,
        reminderButtons: List<Button>
    ) {
        // Reset all buttons to default color
        reminderButtons.forEach {
            it.setBackgroundColor(defaultColor)
            it.setTextColor(textDefaultColor)
        }

        // Set the selected button's color
        selectedButton.setBackgroundColor(selectedColor)
        selectedButton.setTextColor(textSelectedColor)
    }


    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun setupTimeSpinners() {
        val hourList = listOf("HH") + (1..12).map { it.toString().padStart(2, '0') }
        val minuteList = listOf("MM") + (0..59).map { it.toString().padStart(2, '0') }
        val amPmList = listOf("AM", "PM")

        // Set up Hour Spinner
        val hourAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hourList)
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.JathagamSpinnerHour.adapter = hourAdapter

        // Set up Minute Spinner
        val minuteAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, minuteList)
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.JathagamSpinnerMinute.adapter = minuteAdapter

        // Set up AM/PM Spinner
        val amPmAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, amPmList)
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.JathagamSpinnerAmPM.adapter = amPmAdapter

    }

    private fun showDatePicker1(selectedDatePickerDate: String) {

        val tamilLocale = Locale("ta", "IN")

        val inputFormat = SimpleDateFormat("dd MMMM yyyy", tamilLocale)
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val outputFormatServer = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        val date = inputFormat.parse(selectedDatePickerDate)
        displayDate = date?.let { outputFormat.format(it) } ?: ""

        val date1 = inputFormat.parse(selectedDatePickerDate)
        selectedDate = date1?.let { outputFormatServer.format(it) } ?: ""

        binding.etExpiryDate.setText(displayDate)

    }

    private fun loadCategoriesForSelectedType() {
        addItemViewModel.fetchCategories(989015, selectedItemType)
    }


    private fun changeColor(card: CardView, text: TextView, show: Boolean) {
        with(binding) {
            setCardColor(btnExpiryItem, R.color.exp_white1)
            setTextColor(expiryText, R.color.exp_trans)
            setCardColor(btnRenewItem, R.color.exp_white1)
            setTextColor(renewText, R.color.exp_trans)
        }
        if (show) {
            setCardColor(card, R.color.btncolor)
            setTextColor(text, R.color.exp_white1)

        }

    }

    private fun setCardColor(card: CardView, color: Int) {
        card.setCardBackgroundColor(
            ContextCompat.getColor(
                this@AddItemActivity, color
            )
        )
    }

    private fun setTextColor(text: TextView, color: Int) {

        text.setTextColor(
            ContextCompat.getColor(
                this@AddItemActivity, color
            )
        )
    }

    private fun showSelectionDialog(
        title: String,
        items: MutableList<Map<String, Any>>,
        onItemSelected: (Map<String, Any>) -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_selection, null)
        builder.setView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val btnCustomAction = dialogView.findViewById<Button>(R.id.btnCustomAction)

        val dialog = builder.create()

        // Initialize adapter
        val adapter = ExpiryItemAdapter_editdelete(this, items, onItemClick = { selectedItem ->
            onItemSelected(selectedItem)
            dialog.dismiss()
        }, onEdit = { itemName, itemId ->
            showEditDialog(itemName, itemId) // Show the edit dialog
        }, onDelete = { itemId ->
            deleteItem(itemId) // Delete the item
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Search functionality
        etSearch.addTextChangedListener {
            adapter.filter(it.toString()) // Filter list based on search input
        }

        // Button for adding new item
        btnCustomAction.setOnClickListener {
            // Open the create dialog when the button is clicked
            showCreateDialog(dialog_type) { newItem ->
                // Add the new item to the list and update the adapter
                items.add(newItem)
                adapter.notifyDataSetChanged()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditDialog(itemName: String, itemId: Int) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_item, null)
        builder.setView(dialogView)

        val etItemName = dialogView.findViewById<EditText>(R.id.etItemName)
        etItemName.setText(itemName)

        builder.setPositiveButton("Save") { dialog, which ->
            val newItemName = etItemName.text.toString()
            if (newItemName.isNotEmpty()) {
                // Call the ViewModel's addItemToServer method with the item ID to update
                println("itemId for item ==$itemId")
                expiryDateViewModel.addItemToServer(newItemName, itemId)
            }
        }
        builder.setNegativeButton("Cancel", null)

        builder.create().show()
    }

    private fun showCreateDialog(tableName: String, onNewItemCreated: (Map<String, Any>) -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etItemName = dialog.findViewById<EditText>(R.id.etItemName)
        val spinnerItemType = dialog.findViewById<android.widget.Spinner>(R.id.spinnerItemType)
        val btnCreate = dialog.findViewById<Button>(R.id.btnCreate)

        if (tableName == "categorys") {
            etItemName.hint = "Enter Category Name"
            spinnerItemType.visibility = View.VISIBLE

            val itemTypes = arrayOf("expiry item", "renew item")
            val adapter =
                android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, itemTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerItemType.adapter = adapter
        } else {
            etItemName.hint = "Enter Item Name"
            spinnerItemType.visibility = View.GONE
        }
        println("check doalogg")

        btnCreate.setOnClickListener {
            println("check doalogg New")
            val name = etItemName.text.toString().trim()
            val selectedType =
                if (tableName == "categorys") spinnerItemType.selectedItem.toString() else ""
            println("item nameeee===== $name")
            println("item nameeeett ===== $tableName")

            if (name.isNotEmpty()) {
                if (tableName == "Item name") {
                    println("item nameeee===== $name")
                    expiryDateViewModel.addItemToServer(name, 0)
                } else if (tableName == "categorys") {
                    println("cat nameeee===== $name")
                    expiryDateViewModel.addCategoryToServer(name, selectedType)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun deleteItem(itemId: Int) {
        expiryDateViewModel.deleteitem(itemId.toString())
    }

    private fun scheduleNotification(
        itemName: String,
        expiryDate: String,
        notifyTime: String,
        reminderType: String,
        customDate: String? = null
    ) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("itemName", itemName)
            putExtra("notificationId", itemName.hashCode()) // Unique ID for the notification
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            itemName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Split the expiryDate into day, month, and year
        val dateParts = expiryDate.split("-") // Expected format: dd-MM-yyyy
        val timeParts = notifyTime.split(" ") // Expected format: HH:mm AM/PM

        val hourMinuteParts = timeParts[0].split(":") // Splitting HH:mm
        var hour = hourMinuteParts[0].toInt()
        val minute = hourMinuteParts[1].toInt()
        val amPm = timeParts[1] // AM or PM

        // Convert 12-hour format to 24-hour format
        if (amPm == "PM" && hour != 12) {
            hour += 12
        } else if (amPm == "AM" && hour == 12) {
            hour = 0
        }

        // Set up the calendar with expiry date and notify time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
            set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-based
            set(Calendar.YEAR, dateParts[2].toInt())
            set(Calendar.HOUR_OF_DAY, hour) // Corrected hour format
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Adjust the notification date based on reminder type
        when (reminderType) {
            "same day" -> { /* No change */
            }

            "2 days before" -> calendar.add(Calendar.DAY_OF_MONTH, -2)
            "1 week before" -> calendar.add(Calendar.DAY_OF_MONTH, -7)
            "custom" -> {
                customDate?.let {
                    val customParts = it.split("-")
                    calendar.set(Calendar.DAY_OF_MONTH, customParts[0].toInt())
                    calendar.set(Calendar.MONTH, customParts[1].toInt() - 1)
                    calendar.set(Calendar.YEAR, customParts[2].toInt())
                }
            }
        }

        // Schedule the Alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }


}
