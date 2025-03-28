package expirydatemanager.activity

import android.app.Activity
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.Adapter.ExpiryItemAdapter_editdelete
import expirydatemanager.fragment.ExpiryViewModelFactory
import expirydatemanager.others.ExpiryCustomDatePickerDialog
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.Notification.ExpiryNotificationReceiver
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityAdditemBinding
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdditemBinding

    // private val addItemViewModel: ExpiryViewModel by viewModels()
    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    private val addItemViewModel: ExpiryViewModel by viewModels {
        ExpiryViewModelFactory(repository)
    }

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
    var selectedType = ""

    //private val expiryDateViewModel: ExpiryViewModel by viewModels()
    var editId = ""
    var isEditMode = ""

    // Variables to store selected time values
    private var selectedHour: String = "HH"
    private var selectedMinute: String = "MM"
    private var selectedAmPm: String = "AM"
    private var itemData: HashMap<String, String>? = null  // Declare globally

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
        selectedItemType = (intent.getStringExtra("itemType") as? String).toString()
        changeColor(binding.btnExpiryItem, binding.expiryText, true)
        loadCategoriesForSelectedType()
        addItemViewModel.fetchItemNames(989015)
        itemData = intent.getSerializableExtra("item_data") as? HashMap<String, String>
        isEditMode = (intent.getStringExtra("isEditMode") as? String).toString()

        // Observe item names from ViewModel
        addItemViewModel.itemNames.observe(this, androidx.lifecycle.Observer { itemNames ->
            println("itemNames == $itemNames")
            // addItemViewModel.fetchItemNames(989015)

            itemNamesList = itemNames

            dialog_type = "Item name"
//            adapter.notifyDataSetChanged()

        })


        // Observe categories from ViewModel
        addItemViewModel.categories.observe(this, androidx.lifecycle.Observer { categories ->
            categoriesList = categories
            println("catItem == $categoriesList")
            dialog_type = "Category name"
            // If empty, don't show the dialog
            if (categoriesList.isEmpty()) return@Observer
            if (categoriesList.isNotEmpty() && itemData != null) {

            }
        })
        if (isEditMode == "edit") {
            populateItemData()
        } else {
            setupReminderButtons(binding.btnSameDay)
            setupTimeSpinners()
        }


        //  setupReminderButtons(binding.btnSameDay)
        //  setupTimeSpinners()

        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)
        db.execSQL("PRAGMA foreign_keys=ON;")




        binding.etItemName.setOnClickListener {
            if (ExpiryUtils.isNetworkAvailable(this)) {
                val GetItems =
                    itemNamesList["Items"] as? MutableList<Map<String, Any>> ?: mutableListOf()
                println("GetItems == $GetItems")

                showSelectionDialog("Select Item Name", GetItems) { selectedName ->
                    binding.etItemName.setText(selectedName["item_name"] as String)
                }
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
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

            val datePicker = ExpiryCustomDatePickerDialog({ selectedManamagalDatePicker ->
                showDatePicker1(selectedManamagalDatePicker)
            }, binding.etExpiryDate.text.toString().trim())
            datePicker.show(supportFragmentManager, "datePicker")
        }


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
            val datePicker = ExpiryCustomDatePickerDialog({ selectedManamagalDatePicker ->
                showCustomReminderDate(selectedManamagalDatePicker)
            }, binding.customdatetext.text.toString().trim())

            datePicker.show(supportFragmentManager, "datePicker")
            setupReminderButtons(binding.customReminder)

        }
        addItemViewModel.itemNameResponse.observe(this) { response ->
            println("Add/Update Item Response == $response")

            val status = response["status"]?.toString() ?: ""
            if (status == "success") {
                if (isEditMode == "edit") {
                    Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Added Successfully!", Toast.LENGTH_SHORT).show()
                }
                val resultIntent = Intent()
                resultIntent.putExtra("item_added", true)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }


        binding.spCategories.setOnClickListener {
            if (ExpiryUtils.isNetworkAvailable(this)) {
                // loadCategoriesForSelectedType()
                if (categoriesList["Category"] != null) {
                    val GetcategoryList =
                        categoriesList["Category"] as MutableList<Map<String, Any>>
                    showSelectionDialog("Select Category", GetcategoryList) { selectedCategory ->
                        binding.spCategories.text = selectedCategory["category"] as String
                    }
                }

            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }

        }


        binding.btnAddItem.setOnClickListener {
            if (ExpiryUtils.isNetworkAvailable(this)) {
                saveItemToServer()
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }

        //setupTimeSpinners()
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


        println("saveItemToServer == itemID $itemId")
        println("saveItemToServer == categoryId $categoryId")
        println("saveItemToServer == notifyTime $notifyTime")
        println("saveItemToServer == remark $remark")
        if (selectedItemType == "expiry item") {
            println("saveItemToServer == 1 $selectedItemType")
        } else {
            println("saveItemToServer == 2 $selectedItemType")
        }

        println("saveItemToServer == formattedExpiryDate $formattedExpiryDate")
        println("saveItemToServer == customDate $customDate")
        println("saveItemToServer == id $editId")

        // Save the item to the server
        addItemViewModel.addListToServer(
            id = editId,
            categoryId = categoryId,
            itemType = if (selectedItemType == "expiry item") 1 else 2,
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
        println("saveItemToServer == $itemNamesList")

        return items.find { it["item_name"] == name }?.get("id")?.let {
            when (it) {
                is Double -> it.toInt()  // Convert Double to Int
                is Int -> it             // Already an Int
                is String -> it.toDoubleOrNull()?.toInt() ?: 0  // Handle string numbers
                else -> 0
            }
        } ?: 0
    }


    private fun getCategoryIdFromName(name: String): Int {
        val categories = categoriesList["Category"] as? List<Map<String, Any>> ?: return 0
        println("saveItemToServer  category == $categoriesList")

        return categories.find { it["category"] == name } // Change "item_name" to "category"
            ?.get("id")?.let {
                when (it) {
                    is Double -> it.toInt()  // Convert Double to Int
                    is Int -> it             // Already an Int
                    is String -> it.toDoubleOrNull()?.toInt() ?: 0  // Handle string numbers
                    else -> 0
                }
            } ?: 0
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

    private fun populateItemData() {
        itemData?.let { data ->

            println("check data === $data")
            binding.etItemName.setText(data["item_name"] ?: "Select Item Name")
            binding.etExpiryDate.setText(formatDate(data["action_date"].toString()) ?: "")
            binding.etNote.setText(data["remark"] ?: "")
            editId = data["id"] ?: ""

            selectedItemType =
                if (data["item_type"] == "expiry item") "expiry item" else "renew item"
            if (selectedItemType == "renew item") {
                changeColor(binding.btnRenewItem, binding.renewText, true)
            } else {
                changeColor(binding.btnExpiryItem, binding.expiryText, true)
            }

            setupReminderButtons1(data["reminder_type"].toString() ?: "0")

            // **Ensure Time Spinners Are Initialized Before Setting Selection**
            setupTimeSpinners()

            // **Extract Notify Time & Handle Potential Nulls**
            val notifyTime = data["notify_time"] ?: "12:00 AM"
            val timeParts = notifyTime.split(" ")
            val time = timeParts.getOrNull(0)?.split(":") ?: listOf("12", "00") // Default 12:00
            val amPm = timeParts.getOrNull(1) ?: "AM"

            val hourStr = time.getOrNull(0)?.padStart(2, '0') ?: "HH"
            val minuteStr = time.getOrNull(1)?.padStart(2, '0') ?: "MM"

            val hourList = listOf("HH") + (1..12).map { it.toString().padStart(2, '0') }
            val minuteList = listOf("MM") + (0..59).map { it.toString().padStart(2, '0') }

            val hourIndex = hourList.indexOf(hourStr).takeIf { it >= 0 } ?: 0
            val minuteIndex = minuteList.indexOf(minuteStr).takeIf { it >= 0 } ?: 0
            val amPmIndex = if (amPm.uppercase() == "PM") 1 else 0

            // **Set Spinner Selections**
            binding.JathagamSpinnerHour.setSelection(hourIndex)
            binding.JathagamSpinnerMinute.setSelection(minuteIndex)
            binding.JathagamSpinnerAmPM.setSelection(amPmIndex)

            binding.spCategories.text = data["category_name"].toString() ?: ""
        }
    }


    fun formatDate(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(inputDate)  // Parse input string to Date
            outputFormat.format(date!!)  // Convert Date to desired format
        } catch (e: Exception) {
            inputDate // Return the original date if there's an error
        }
    }

    private fun setupReminderButtons1(reminderType: String) {
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
        when (reminderType) {
            "0" -> highlightSelectedButton(
                binding.btnSameDay,
                defaultColor,
                selectedColor,
                textDefaultColor,
                textSelectedColor,
                reminderButtons
            )

            "1" -> highlightSelectedButton(
                binding.btnSameDay,
                defaultColor,
                selectedColor,
                textDefaultColor,
                textSelectedColor,
                reminderButtons
            )

            "2" -> highlightSelectedButton(
                binding.btn2DaysBefore,
                defaultColor,
                selectedColor,
                textDefaultColor,
                textSelectedColor,
                reminderButtons
            )

            "3" -> highlightSelectedButton(
                binding.btn1WeekBefore,
                defaultColor,
                selectedColor,
                textDefaultColor,
                textSelectedColor,
                reminderButtons
            )

            "custom" -> highlightSelectedButton(
                binding.customReminder,
                defaultColor,
                selectedColor,
                textDefaultColor,
                textSelectedColor,
                reminderButtons
            )
        }
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


        fun refreshItemList(itemType: String) {
            println("refreshItemList == $itemType")
            if (itemType == "item_type") {
                addItemViewModel.fetchItemNames(989015)
                // Don't rely on items.clear() directly here, use a fresh copy
                addItemViewModel.itemNames.observeOnce(this@AddItemActivity) { updatedItemsMap ->
                    itemNamesList = updatedItemsMap

                    val updatedItems =
                        updatedItemsMap["Items"] as? List<Map<String, Any>> ?: emptyList()

                    // Update the adapter data properly
                    adapter.updateList(updatedItems.toMutableList()) // <-- use this
                    recyclerView.adapter =
                        adapter // <- Force rebind in case observer skipped notify
                    etSearch.setText("") // Reset search box to show full list

                }
            } else {
                addItemViewModel.fetchCategories(989015, selectedType)
                addItemViewModel.categories.observeOnce(this@AddItemActivity) { categories ->
                    categoriesList = categories
                    val updatedCategories =
                        categories["Category"] as? List<Map<String, Any>> ?: emptyList()
                    adapter.updateList(updatedCategories.toMutableList())
                    recyclerView.adapter =
                        adapter // <- Force rebind in case observer skipped notify
                    etSearch.setText("") // Reset search box to show full list

                }
            }
        }




        adapter = ExpiryItemAdapter_editdelete(this, items, onItemClick = { selectedItem ->
            onItemSelected(selectedItem)
            dialog.dismiss()
        }, onEdit = { itemName, itemId, itemType ->
            showEditDialog(itemName, itemId, itemType) {
                refreshItemList(itemType)
            }

        }, onDelete = { itemId, itemType ->
            deleteItem(itemId, itemType) {
                refreshItemList(itemType)
            }
        })


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Search functionality
        etSearch.addTextChangedListener {
            adapter.filter(it.toString()) // Filter list based on search input
        }

        // Button for adding new item
        btnCustomAction.setOnClickListener {
            if (title == "Select Category") {
                dialog_type = "categorys"
            } else {
                dialog_type = "Item name"
            }

            showCreateDialog(dialog_type) { newItem ->
                if (dialog_type == "Item name") {
                    refreshItemList("item_type")
                } else {
                    refreshCategoryList(selectedItemType)
                }
            }
            dialog.dismiss()
        }


        dialog.show()
    }

    fun refreshCategoryList(itemType: String) {
        val type = if (itemType == "expiry item") "1" else "2"
        addItemViewModel.fetchCategories(989015, itemType)

        addItemViewModel.categories.observe(this) { categoryMap ->
            categoriesList = categoryMap
            println("🔥 Categories updated == $categoriesList")
        }
    }


    private fun showEditDialog(
        itemName: String,
        itemId: Int,
        itemType: String,
        onSuccess: () -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_item, null)
        builder.setView(dialogView)

        val etItemName = dialogView.findViewById<EditText>(R.id.etItemName)
        etItemName.setText(itemName)

        builder.setPositiveButton("Save") { _, _ ->
            val newItemName = etItemName.text.toString()
            if (newItemName.isNotEmpty()) {
                if (itemType == "item_type") {
                    val params = HashMap<String, String>().apply {
                        this["action"] = "addItemName"
                        this["user_id"] = "989015"
                        this["itemname"] = newItemName
                        this["item_id"] = "$itemId"
                    }

                    addItemViewModel.addItemToServer(params)

                    addItemViewModel.itemNameResponse1.removeObservers(this)
                    addItemViewModel.itemNameResponse1.observe(this) { response ->
                        val status = response["status"]?.toString() ?: ""
                        if (status == "success") {
                            Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess.invoke()
                        } else {
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                        addItemViewModel.itemNameResponse1.removeObservers(this)
                    }

                } else {
                    val params = HashMap<String, Any>().apply {
                        this["action"] = "addCategory"
                        this["user_id"] = "989015"
                        this["category"] = newItemName
                        this["item_type"] =
                            if (selectedType == "expiry item") "1" else "2"
                        this["cat_id"] = "$itemId"
                    }
                    println(" Sending data ==$params")
                    addItemViewModel.addCategoryToServer(params)

                    addItemViewModel.categoryResponse.observe(this) { response ->
                        val status = response["status"]?.toString() ?: ""
                        if (status == "success") {
                            Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess.invoke() // 🔁 Trigger refresh callback
                        } else {
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                        addItemViewModel.categoryResponse.removeObservers(this)
                    }
                }

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
        println("Show create dialog tableName ==$tableName")
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
        addItemViewModel.categoryResponse.observe(this@AddItemActivity) { response ->
            val status = response["status"]?.toString() ?: ""
            if (status == "success") {
                Toast.makeText(this@AddItemActivity, "Category added!", Toast.LENGTH_SHORT).show()
                refreshCategoryList(selectedType)  // ⬅️ Call refresh here
                addItemViewModel.categoryResponse.removeObservers(this@AddItemActivity)
            } else {
                Toast.makeText(this@AddItemActivity, "Failed to add category!", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            selectedType =
                if (tableName == "categorys") spinnerItemType.selectedItem.toString() else ""
            println("item nameeee===== $name")
            println("item nameeeett ===== $tableName")

            if (name.isNotEmpty()) {
                if (tableName == "Item name") {
                    val params = HashMap<String, String>().apply {
                        this["action"] = "addItemName"
                        this["user_id"] = "989015"
                        this["itemname"] = etItemName.text.toString()
                        this["item_id"] = ""
                    }

                    addItemViewModel.addItemToServer(params)

                    addItemViewModel.itemNameResponse1.observe(this@AddItemActivity) { response ->
                        val status = response["status"]?.toString() ?: ""
                        if (status == "success") {
                            Toast.makeText(this@AddItemActivity, "Item added!", Toast.LENGTH_SHORT)
                                .show()

                            // ✅ Notify dialog and refresh item list
                            onNewItemCreated(mapOf("item_name" to etItemName.text.toString()))
                            addItemViewModel.itemNameResponse1.removeObservers(this@AddItemActivity)
                        } else {
                            Toast.makeText(
                                this@AddItemActivity,
                                "Failed to add item!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else if (tableName == "categorys") {
                    println("cat nameeee===== $name")

                    val params = HashMap<String, Any>().apply {
                        this["action"] = "addCategory"
                        this["user_id"] = "989015"
                        this["category"] = etItemName.text.toString().trim()
                        this["item_type"] =
                            if (spinnerItemType.selectedItem.toString() == "expiry item") "1" else "2"
                        this["cat_id"] = ""
                    }
                    println("cat nameeee===== $params")
                    addItemViewModel.addCategoryToServer(params)


                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun deleteItem(itemId: Int, itemType: String, onSuccess: () -> Unit) {
        val params = hashMapOf<String, Any>(
            "action" to "deleteItem", "user_id" to 989015, "item_id" to itemId
        )
        addItemViewModel.deleteitem(989015, itemId, params)

        // Observe deletion result
        addItemViewModel.deleteitemResponse.observe(this) { response ->
            val status = response["status"]?.toString()
            if (status == "success") {
                Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_SHORT).show()
                onSuccess.invoke()
            } else {
                Toast.makeText(this, "Failed to delete!", Toast.LENGTH_SHORT).show()
            }

            // Prevent multiple triggers
            addItemViewModel.deleteitemResponse.removeObservers(this)
        }
    }

    private fun scheduleNotification(
        itemName: String,
        expiryDate: String,      // yyyy-MM-dd from server
        notifyTime: String,      // HH:mm AM/PM
        reminderType: String,    // same day / 2 days before / 1 week before / custom
        customDate: String? = null
    ) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Convert expiryDate to dd_MM_yyyy for notification
        val displayDate = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(expiryDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            expiryDate // fallback
        }

        val intent = Intent(this, ExpiryNotificationReceiver::class.java).apply {
            putExtra("itemName", itemName)
            putExtra("notificationId", itemName.hashCode())
            putExtra("reminderType", reminderType)
            putExtra("customDate", customDate)
            putExtra("expiryDate", displayDate) // sending as dd_MM_yyyy
            putExtra("notifyTime", notifyTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            itemName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Convert time to 24-hour
        val timeParts = notifyTime.split(" ")
        val hourMinute = timeParts[0].split(":")
        var hour = hourMinute[0].toInt()
        val minute = hourMinute[1].toInt()
        val amPm = timeParts[1]

        if (amPm == "PM" && hour != 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0

        // Parse expiryDate for calendar
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val baseDate = sdf.parse(expiryDate)
        val calendar = Calendar.getInstance().apply {
            time = baseDate!!
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Adjust for reminder type
        when (reminderType) {
            "2 days before" -> calendar.add(Calendar.DAY_OF_MONTH, -2)
            "1 week before" -> calendar.add(Calendar.DAY_OF_MONTH, -7)
            "custom" -> {
                customDate?.let {
                    val customFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val customParsed = customFormat.parse(it)
                    calendar.time = customParsed!!
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                }
            }
        }

        // Set alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }


}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}
