package expirydatemanager.activity

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import expirydatemanager.Adapter.ExpiryItemAdapter_editdelete
import expirydatemanager.fragment.ExpiryViewModelFactory
import expirydatemanager.others.ExpiryCustomDatePickerDialog
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.Notification.ExpiryNotificationReceiver
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.databinding.SmExpiryActivityAdditemBinding
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: SmExpiryActivityAdditemBinding
    private var hasPopulatedData = false
    var category_id = ""

    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    private val addItemViewModel: ExpiryViewModel by viewModels {
        ExpiryViewModelFactory(repository)
    }

    private lateinit var db: SQLiteDatabase
    private lateinit var adapter: ExpiryItemAdapter_editdelete
    private var selectedItemType = "1"
    private var displayDate = ""
    private var selectedDate = ""
    private var selectedReminder = "same day"
    private var dialog_type = ""
    private var categories = HashMap<String, Any>()
    var itemNamesList: Map<String, Any> = hashMapOf()
    var categoriesExpiryList: Map<String, Any> = hashMapOf()
    var categoriesRenewList: Map<String, Any> = hashMapOf()
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
        binding = SmExpiryActivityAdditemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.title = HtmlCompat.fromHtml(
            "Add Item", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setSupportActionBar(binding.appBar)
        supportActionBar!!.title = HtmlCompat.fromHtml(
            "Add Item", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Set default selection to "Expiry Item"

        changeColor(binding.btnExpiryItem, binding.expiryText, true)


        // addItemViewModel.fetchItemNames(ExpiryUtils.userId)
        itemData = intent.getSerializableExtra("item_data") as? HashMap<String, String>
        isEditMode = (intent.getStringExtra("isEditMode") as? String).toString()

        if (isEditMode == "edit") {
            selectedItemType = when (itemData?.get("item_type")) {
                "1", "expiry item" -> "1"
                "2", "renew item" -> "2"
                else -> "1"
            }
            loadCategoriesForSelectedType(selectedItemType)
        } else {
            selectedItemType = intent.getStringExtra("itemType") ?: "1"
            loadCategoriesForSelectedType("1")
        }

        // Observe item names from ViewModel
        addItemViewModel.itemNames.observe(this, androidx.lifecycle.Observer { itemNames ->
            println("itemNames == $itemNames")
            // addItemViewModel.fetchItemNames(989015)

            itemNamesList = itemNames

            dialog_type = "Item name"
//            adapter.notifyDataSetChanged()

        })


        // Observe categories from ViewModel
        addItemViewModel.expiryCategories.observe(this) { categories ->
            if (selectedItemType == "1") {
                categoriesExpiryList = categories
                ExpiryUtils.mProgress.dismiss()
                println("Expiry List: $categoriesExpiryList")
                if (categoriesExpiryList.isNotEmpty()) {
                    if (isEditMode == "edit" && !hasPopulatedData) {
                        hasPopulatedData = true
                        populateItemData()
                    }
                } else {
                    Toast.makeText(this, "No categories available.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        addItemViewModel.renewCategories.observe(this) { categories ->
            if (selectedItemType == "2") {
                categoriesRenewList = categories
                ExpiryUtils.mProgress.dismiss()
                println("Renew List: $categoriesRenewList")
                if (categoriesRenewList.isNotEmpty()) {
                    if (isEditMode == "edit" && !hasPopulatedData) {
                        hasPopulatedData = true
                        populateItemData()
                    }
                } else {
                    Toast.makeText(this, "No categories available.", Toast.LENGTH_SHORT).show()
                }
            }
        }





        if (isEditMode != "edit") {
            setupReminderButtons(binding.btnSameDay)
            setupTimeSpinners()
        } else {
            //  populateItemData()
        }


        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)
        db.execSQL("PRAGMA foreign_keys=ON;")




        binding.etItemName.setOnClickListener {
            if (ExpiryUtils.isNetworkAvailable(this)) {
                if (category_id == "") {
                    Toast.makeText(this, "Please Select a category", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val getItems =
                    itemNamesList["Items"] as? MutableList<Map<String, Any>> ?: mutableListOf()
                println("GetItems == $getItems")

                showSelectionDialog("Select Item Name", getItems) { selectedName ->
                    binding.etItemName.text = selectedName["item_name"] as String
                }
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }

        }
        println("selectttttrewmn  ====$selectedItemType")
        binding.btnExpiryItem.setOnClickListener {
            clearFields()
            changeColor(binding.btnExpiryItem, binding.expiryText, true)
            selectedItemType = "1"
            loadCategoriesForSelectedType("1")
        }

        binding.btnRenewItem.setOnClickListener {
            clearFields()
            changeColor(binding.btnRenewItem, binding.renewText, true)
            selectedItemType = "2"
            loadCategoriesForSelectedType("2")
        }

        binding.etExpiryDate.setOnClickListener {

            val datePicker = ExpiryCustomDatePickerDialog({ selectedManamagalDatePicker ->
                showDatePicker1(selectedManamagalDatePicker)
            }, binding.etExpiryDate.text.toString().trim())
            datePicker.show(supportFragmentManager, "datePicker")
        }


        binding.btnSameDay.setOnClickListener {
            selectedReminder = "same day"
            binding.customdatetext.visibility = View.GONE
            setupReminderButtons(binding.btnSameDay)
        }
        binding.btn2DaysBefore.setOnClickListener {
            selectedReminder = "2 days before"
            binding.customdatetext.visibility = View.GONE
            setupReminderButtons(binding.btn2DaysBefore)
        }
        binding.btn1WeekBefore.setOnClickListener {
            selectedReminder = "1 week before"
            binding.customdatetext.visibility = View.GONE
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
            println("Add/Update selectedItemType == $selectedItemType")

            val status = response["status"]?.toString() ?: ""
            if (status == "success") {
                if (isEditMode == "edit") {
                    Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Added Successfully!", Toast.LENGTH_SHORT).show()
                }
                val resultIntent = Intent()
                resultIntent.putExtra("item_added", true)
                resultIntent.putExtra("item_type", selectedItemType)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }


        binding.spCategories.setOnClickListener {
            if (ExpiryUtils.isNetworkAvailable(this)) {
                val categorySourceList: Map<String, Any> = when (selectedItemType) {
                    "1" -> categoriesExpiryList
                    "2" -> categoriesRenewList
                    else -> emptyMap()
                }

                println("categoriesExpiryList == $categoriesExpiryList")
                println("categoriesRenewList == $categoriesRenewList")

                val getCategoryList =
                    categorySourceList.values.toList().filterIsInstance<Map<String, Any>>()

                if (getCategoryList.isNotEmpty()) {
                    showSelectionDialog(
                        "Select Category",
                        getCategoryList.toMutableList()
                    ) { selectedCategory ->
                        println("Return Category Id ==${selectedCategory["id"]}")

                        binding.etItemName.setText("Select Item Name")
                        binding.spCategories.text = selectedCategory["category"] as String
                        category_id = selectedCategory["id"].toString()

                        val map = HashMap<String, String>()
                        map["action"] = "getItemName"
                        map["category_id"] = category_id
                        map["item_type"] = selectedItemType.toString()
                        map["user_id"] = ExpiryUtils.userId.toString()

                        println("GET ITEM NAMES == $map")

                        ExpiryUtils.mProgress(this, "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()
                        addItemViewModel.fetchItemNames(map)
                        addItemViewModel.itemNames.observe(this) { response ->
                            println("Get Item Response == $response")
                            itemNamesList = response
                            ExpiryUtils.mProgress.dismiss()
                        }
                    }
                } else {
                    Toast.makeText(this, "No categories available.", Toast.LENGTH_SHORT).show()
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
        println("saveItemToServer == notifyTime ${binding.etItemName.text.toString().trim()}")
        val itemId = getItemIdFromName(binding.etItemName.text.toString().trim())
        val categoryId = getCategoryIdFromName(binding.spCategories.text.toString().trim())


        println("saveItemToServer == itemID $itemId")
        println("saveItemToServer == categoryId $categoryId")
        println("saveItemToServer == notifyTime $notifyTime")
        println("saveItemToServer == remark $remark")
        println("saveItemToServer == remark $selectedItemType")
        println("saveItemToServer == selectedReminder $selectedReminder")


        println("saveItemToServer == formattedExpiryDate $formattedExpiryDate")
        println("saveItemToServer == customDate $customDate")
        println("saveItemToServer == id $editId")

        // Save the item to the server

        val listId =
            if (isEditMode == "edit") editId.takeIf { it.isNotEmpty() }?.toInt() ?: 0 else 0
        val editModeValue = if (isEditMode == "edit") isEditMode else ""

        addItemViewModel.addListToServer(
            id = itemId.toString(),
            categoryId = category_id.toString().toDouble().toInt(),
            itemType = selectedItemType.toString().toInt(),
            itemId = itemId,
            reminderType = getReminderType(selectedReminder),
            notifyTime = notifyTime,
            remark = remark,
            actionDate = formattedExpiryDate,
            listId = listId,  // ✅ Safe conversion
            customDate = customDate,
            editMode = editModeValue
        )

        println("saveItemToServer == id $listId")
        // Schedule the notification based on reminder type
        scheduleNotification(
            itemName = binding.etItemName.text.toString(),
            expiryDate = formattedExpiryDate,
            notifyTime = notifyTime,
            reminderType = selectedReminder,
            customDate = customDate
        )

        // Reset fields after saving the item
        // clearFields()
    }

    private fun validateInputs(): Boolean {
        val itemName = binding.etItemName.text.toString().trim()
        val category = binding.spCategories.text.toString().trim()
        val expiryDate = binding.etExpiryDate.text.toString().trim()
        val notifyTime = getFormattedNotifyTime()
        val note = binding.etNote.text.toString().trim()

        // Validate Item Type
        if (selectedItemType.isEmpty()) {
            showToast("Please select Item Type!")
            return false
        }

        // Validate Category
        if (category.isEmpty() || category == "Select Category") {
            showToast("Please select a Category!")
            return false
        }

        // Validate Item Name
        if (itemName.isEmpty() || itemName == "Select Item Name") {
            showToast("Please select Item Name!")
            return false
        }

        // Validate Expiry Date (Should be current date or in the future)
        if (expiryDate.isEmpty() || !isDateInFutureOrToday(expiryDate)) {
            showToast("Expiry Date should be today or a future date!")
            return false
        }

        // Validate Reminder Type
        if (selectedReminder.isEmpty()) {
            showToast("Please select a Reminder Type!")
            return false
        }

        // Validate notes
        if (note.isEmpty()) {
            showToast("Please Enter your Notes")
            return false
        }

        // Validate Notify Time selection
        if (notifyTime.contains("HH") || notifyTime.contains("MM")) {
            showToast("Please select a valid Notify Time!")
            return false
        }

        // ✅ If expiry date is today, notify time must be in the future
        if (isToday(expiryDate) && !isTimeInFutureOrNow(notifyTime)) {
            showToast("Notify Time must be in the future!")
            return false
        }

        return true // All validations passed
    }

    private fun isToday(dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val selectedDate = sdf.parse(dateStr)
            val today = sdf.parse(sdf.format(Date()))
            selectedDate == today
        } catch (e: Exception) {
            false
        }
    }


    private fun isTimeInFutureOrNow(notifyTime: String): Boolean {
        return try {
            val currentCalendar = Calendar.getInstance()
            val currentTime = currentCalendar.timeInMillis

            // Parse notify time (e.g., "12:00 AM")
            val timeParts = notifyTime.split(" ")
            val hourMinute = timeParts[0].split(":")
            var hour = hourMinute[0].toIntOrNull() ?: 0
            val minute = hourMinute.getOrNull(1)?.toIntOrNull() ?: 0
            val amPm = timeParts.getOrNull(1)?.uppercase(Locale.ENGLISH) ?: "AM"

            // Convert to 24-hour format
            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val selectedTime = calendar.timeInMillis
            selectedTime >= currentTime // Return true if the time is today or in the future
        } catch (e: Exception) {
            false
        }
    }


    private fun isDateInFutureOrToday(dateStr: String): Boolean {
        return try {
            val format =
                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH) // The format used for input date
            val selectedDate = format.parse(dateStr)
            val currentDate = Date() // Current system date

            // Compare only the date part (ignoring time) for today or future
            val calendarSelected = Calendar.getInstance()
            calendarSelected.time = selectedDate

            val calendarCurrent = Calendar.getInstance()
            calendarCurrent.time = currentDate

            // Check if the selected date is today or in the future
            calendarSelected[Calendar.YEAR] > calendarCurrent[Calendar.YEAR] || (calendarSelected[Calendar.YEAR] == calendarCurrent[Calendar.YEAR] && calendarSelected[Calendar.DAY_OF_YEAR] >= calendarCurrent[Calendar.DAY_OF_YEAR])
        } catch (e: Exception) {
            false
        }
    }


    private fun getFormattedNotifyTime(): String {
        val hour = binding.expirySpinnerHour.selectedItem.toString()
        val minute = binding.expirySpinnerMinute.selectedItem.toString()
        val amPm = binding.expirySpinnerAmPM.selectedItem.toString()
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
            "2 days before" -> 2
            "1 week before" -> 3
            else -> 0
        }
    }

    private fun getItemIdFromName(name: String): Int {
        println("getItemIdFromName == $name")
        println("itemNamesList == $itemNamesList")
        val items = itemNamesList["Items"] as? List<Map<String, Any>> ?: return 0

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
        val categories = categoriesExpiryList["Category"] as? List<Map<String, Any>> ?: return 0
        println("saveItemToServer  category == $categoriesExpiryList")

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
        binding.customdatetext.visibility = View.VISIBLE
        selectedReminder = displayDate
    }

    private fun clearFields() {
        // Clear the EditTexts
        binding.etItemName.text = "Select Item Name"
        binding.spCategories.text = "Select Category"
        binding.etExpiryDate.text.clear()
        binding.etNote.text.clear()
        binding.customdatetext.text = ""

        // Reset the spinners (notify time and reminder type)
        binding.expirySpinnerHour.setSelection(0)
        binding.expirySpinnerMinute.setSelection(0)
        binding.expirySpinnerAmPM.setSelection(0)

        // Reset the reminder buttons
        selectedReminder = "same day"
        setupReminderButtons(binding.btnSameDay)

        // Reset the selected item type
//        selectedItemType = ""

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

            selectedItemType = when (data["item_type"]) {
                "expiry item" -> "1"
                "renew item" -> "2"
                "1" -> "1"
                "2" -> "2"
                else -> "1"
            }

            when (selectedItemType) {
                "2" -> changeColor(binding.btnRenewItem, binding.renewText, true)
                "1" -> changeColor(binding.btnExpiryItem, binding.expiryText, true)
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
            binding.expirySpinnerHour.setSelection(hourIndex)
            binding.expirySpinnerMinute.setSelection(minuteIndex)
            binding.expirySpinnerAmPM.setSelection(amPmIndex)

//            binding.spCategories.text = data["category_name"].toString() ?: ""
            category_id = data["category_id"].toString() ?: "0"
            println("saveItemToServer  category id== $category_id")

            // Call getCategoryNameFromId to fetch the correct category name
            val categoryName = getCategoryNameFromId(category_id)
            binding.spCategories.text = categoryName
            if (ExpiryUtils.isNetworkAvailable(this)) {
                val map = HashMap<String, String>()
                map["action"] = "getItemName"
                map["category_id"] = category_id
                map["item_type"] = selectedItemType.toString()
                map["user_id"] = "" + ExpiryUtils.userId.toString()
                println("GET ITEM NAMES == $map")
                addItemViewModel.fetchItemNames(map)
                addItemViewModel.itemNames.observe(this) { response ->
                    println("Get Item Response == $response")


                    itemNamesList = response

                }
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getCategoryNameFromId(id: String): String {
        // Select the right list based on selectedItemType
        val categories: Map<String, Any> = when (selectedItemType) {
            "1" -> categoriesExpiryList
            "2" -> categoriesRenewList
            else -> emptyMap()
        }

        println("getCategoryNameFromId -> categoryId: $id")
        println("Selected Categories Map: $categories")
        println("Selected Categories Map: $selectedItemType")

        // Look for matching entry by comparing string/int/double keys
        val matchedCategory = categories.entries.find { entry ->
            val keyId = entry.key.toDoubleOrNull()?.toInt()
            val targetId = id.toIntOrNull()
            keyId != null && keyId == targetId
        }?.value as? Map<String, Any>

        return matchedCategory?.get("category") as? String ?: "Unknown Category"
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

            "1" -> {
                highlightSelectedButton(
                    binding.btnSameDay,
                    defaultColor,
                    selectedColor,
                    textDefaultColor,
                    textSelectedColor,
                    reminderButtons
                )
                selectedReminder = "same day"
            }

            "2" -> {
                highlightSelectedButton(
                    binding.btn2DaysBefore,
                    defaultColor,
                    selectedColor,
                    textDefaultColor,
                    textSelectedColor,
                    reminderButtons
                )
                selectedReminder = "2 days before"
            }

            "3" -> {
                highlightSelectedButton(
                    binding.btn1WeekBefore,
                    defaultColor,
                    selectedColor,
                    textDefaultColor,
                    textSelectedColor,
                    reminderButtons
                )
                selectedReminder = "1 week before"
            }

            "0" -> {
                highlightSelectedButton(
                    binding.customReminder,
                    defaultColor,
                    selectedColor,
                    textDefaultColor,
                    textSelectedColor,
                    reminderButtons
                )
                selectedReminder = "custom"
            }
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
        binding.expirySpinnerHour.adapter = hourAdapter

        // Set up Minute Spinner
        val minuteAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, minuteList)
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.expirySpinnerMinute.adapter = minuteAdapter

        // Set up AM/PM Spinner
        val amPmAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, amPmList)
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.expirySpinnerAmPM.adapter = amPmAdapter

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

    private fun loadCategoriesForSelectedType(selectedItemType: String) {
        if (selectedItemType == "1" && categoriesExpiryList.isNotEmpty()) {
            println("Using cached Expiry Categories")
            if (isEditMode == "edit" && !hasPopulatedData) {
                hasPopulatedData = true
                populateItemData()
            }
            return
        }

        if (selectedItemType == "2" && categoriesRenewList.isNotEmpty()) {
            println("Using cached Renew Categories")
            if (isEditMode == "edit" && !hasPopulatedData) {
                hasPopulatedData = true
                populateItemData()
            }
            return
        }

        // Only fetch if not cached
        ExpiryUtils.mProgress(this, "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()
        addItemViewModel.fetchCategories(ExpiryUtils.userId, selectedItemType)
        println("Fetching categories for itemType == $selectedItemType")
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
        val dialogView = inflater.inflate(R.layout.sm_expiry_dialog_custom_selection, null)
        builder.setView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
        val btnCustomAction = dialogView.findViewById<Button>(R.id.btnCustomAction)
        val selectText = dialogView.findViewById<TextView>(R.id.expiryselecttext)

        val dialog = builder.create()
        println("Show create dialog tableName == $title")
        selectText.text = if (title == "Select Category") {
            "Select Your Category Name"
        } else {
            if (items.isNotEmpty()) "Select Your Item Name" else "Please Add Your Item Name"
        }

        fun refreshItemList(itemType: String) {
            println("refreshItemList == $itemType")
            if (itemType == "item_type") {
                val map = HashMap<String, String>().apply {
                    put("action", "getItemName")
                    put("category_id", category_id.toString())
                    put("item_type", selectedItemType.toString())
                    put("user_id", "${ExpiryUtils.userId}")
                }

                addItemViewModel.fetchItemNames(map)
                addItemViewModel.itemNames.observeOnce(this@AddItemActivity) { updatedItemsMap ->
                    itemNamesList = updatedItemsMap
                    val updatedItems = updatedItemsMap["Items"] as? List<Map<String, Any>> ?: emptyList()
                    adapter.updateList(updatedItems.toMutableList())
                    recyclerView.adapter = adapter
                    etSearch.setText("")
                }
            } else {
                etSearch.setText("Select Your Category Name")
                addItemViewModel.fetchCategories(ExpiryUtils.userId, selectedItemType)
                if (selectedItemType == "1") {
                    addItemViewModel.expiryCategories.observeOnce(this@AddItemActivity) { categories ->
                        categoriesExpiryList = categories
                        val updatedCategories = categories.values
                            .filterIsInstance<Map<String, Any>>()
                            .toMutableList()
                        adapter.updateList(updatedCategories)
                        recyclerView.adapter = adapter
                        etSearch.setText("")

                        val map = HashMap<String, String>().apply {
                            put("action", "getItemName")
                            put("category_id", category_id.toString())
                            put("item_type", selectedItemType.toString())
                            put("user_id", "${ExpiryUtils.userId}")
                        }
                        addItemViewModel.fetchItemNames(map)
                        addItemViewModel.itemNames.observe(this) { response ->
                            println("Get Item Response == $response")
                            itemNamesList = response
                        }
                    }
                } else if (selectedItemType == "2") {
                    addItemViewModel.renewCategories.observeOnce(this@AddItemActivity) { categories ->
                        categoriesRenewList = categories
                        val updatedCategories = categories.values
                            .filterIsInstance<Map<String, Any>>()
                            .toMutableList()
                        adapter.updateList(updatedCategories)
                        recyclerView.adapter = adapter
                        etSearch.setText("")

                        val map = HashMap<String, String>().apply {
                            put("action", "getItemName")
                            put("category_id", category_id.toString())
                            put("item_type", selectedItemType.toString())
                            put("user_id", "${ExpiryUtils.userId}")
                        }
                        addItemViewModel.fetchItemNames(map)
                        addItemViewModel.itemNames.observe(this) { response ->
                            println("Get Item Response == $response")
                            itemNamesList = response
                        }
                    }
                }
            }
        }

        adapter = ExpiryItemAdapter_editdelete(this, items,
            onItemClick = { selectedItem ->
                println("SELECTED ITEM ID ==${selectedItem["id"]}")
                onItemSelected(selectedItem)
                dialog.dismiss()
            },
            onEdit = { itemName, itemId, itemType ->
                showEditDialog(itemName, itemId, itemType) {
                    println("EDIT CALLBACK CALLED FOR == $itemType ")
                    refreshItemList(itemType)
                    category_id = itemId.toString()
                    dialog.dismiss()
                }
            },
            onDelete = { itemId, itemType ->
                if (itemType == "item_type") {
                    deleteItem(itemId, itemType) {
                        refreshItemList(itemType)
                        dialog.dismiss()
                    }
                } else {
                    deleteCategory(itemId, itemType) {
                        refreshItemList(itemType)
                        dialog.dismiss()
                    }
                }
            })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ✅ Add Divider
        val divider = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.expiry_divider)?.let {
            divider.setDrawable(it)
        }
        recyclerView.addItemDecoration(divider)

        etSearch.visibility = if (items.size >= 10) View.VISIBLE else View.GONE

        etSearch.addTextChangedListener {
            adapter.filter(it.toString())
        }

        btnCustomAction.setOnClickListener {
            dialog_type = if (title == "Select Category") "categorys" else "Item name"
            showCreateDialog(dialog_type) { newItem ->
                if (dialog_type == "categorys") {
                    refreshItemList("category")
                } else {
                    refreshItemList("item_type")
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun showEditDialog(
        itemName: String, itemId: Int, itemType: String, onSuccess: () -> Unit
    ) {
        val categoryId = getCategoryIdFromName(binding.spCategories.text.toString())
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.sm_expiry_dialog_edit_item, null)
        builder.setView(dialogView)

        val etItemName = dialogView.findViewById<EditText>(R.id.etItemName)
        etItemName.setText(itemName)

        builder.setPositiveButton("Save") { _, _ ->
            val newItemName = etItemName.text.toString()
            if (newItemName.isNotEmpty()) {
                if (itemType == "item_type") {
                    val params = HashMap<String, String>().apply {
                        this["action"] = "addItemName"
                        this["user_id"] = "" + ExpiryUtils.userId
                        this["itemname"] = newItemName
                        this["item_id"] = "$itemId"
                        this["category_id"] = category_id
                        this["item_type"] = selectedItemType

                    }
                    println("EDITING PARAMS ==$params")
                    addItemViewModel.addItemToServer(params)

                    addItemViewModel.itemNameResponse1.removeObservers(this)
                    addItemViewModel.itemNameResponse1.observe(this) { response ->
                        val status = response["status"]?.toString() ?: ""

                        if (status == "success") {
                            binding.etItemName.text = newItemName
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
                        this["user_id"] = ExpiryUtils.userId
                        this["category"] = newItemName
                        this["item_type"] = selectedItemType
                        this["cat_id"] = "$itemId"
                    }
                    println(" Sending data ==$params")
                    addItemViewModel.addCategoryToServer(params)

                    addItemViewModel.categoryResponse.observe(this) { response ->
                        val status = response["status"]?.toString() ?: ""
                        if (status == "success") {
                            binding.spCategories.text = newItemName
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
        dialog.setContentView(R.layout.sm_expity_dialog_create_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etItemName = dialog.findViewById<EditText>(R.id.etItemName)
        val spinnerItemType = dialog.findViewById<android.widget.Spinner>(R.id.spinnerItemType)
        val btnCreate = dialog.findViewById<Button>(R.id.btnCreate)
        val selecttext = dialog.findViewById<TextView>(R.id.selecttext)


        println("Show create dialog tableName ==$tableName")

        if (tableName == "categorys") {
            etItemName.hint = "Enter Category Name"
            spinnerItemType.visibility = View.GONE
            selecttext.text = "Create Your Category Name"

        } else {
            etItemName.hint = "Enter Item Name"
            spinnerItemType.visibility = View.GONE
            selecttext.text = "Select Your Item Name"
        }

        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            selectedType = if (tableName == "categorys") {
                spinnerItemType.selectedItem?.toString() ?: ""
            } else {
                ""
            }

            if (name.isNotEmpty()) {
                if (tableName == "Item name") {

                    val params = HashMap<String, String>().apply {
                        this["action"] = "addItemName"
                        this["user_id"] = "" + ExpiryUtils.userId
                        this["itemname"] = name
                        this["item_id"] = ""
                        this["category_id"] = category_id
                        this["item_type"] = selectedItemType
                    }
                    println("Creating Params = = $params")
                    addItemViewModel.addItemToServer(params)
                    addItemViewModel.itemNameResponse1.observe(this@AddItemActivity) { response ->
                        val status = response["status"]?.toString() ?: ""
                        if (status == "success") {
                            Toast.makeText(this@AddItemActivity, "Item added!", Toast.LENGTH_SHORT)
                                .show()
                            onNewItemCreated(mapOf("item_name" to name))
                            addItemViewModel.itemNameResponse1.removeObservers(this@AddItemActivity)
                            binding.etItemName.text = name
                        } else {
                            Toast.makeText(
                                this@AddItemActivity, "Failed to add item!", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else if (tableName == "categorys") {
                    val selectedCategory = spinnerItemType.selectedItem?.toString() ?: ""
                    val selectedCategoryId =
                        (categoriesExpiryList["Category"] as? List<Map<String, Any>>)?.find { it["category"] == selectedCategory }
                            ?.get("id").toString()

                    val params = HashMap<String, Any>().apply {
                        this["action"] = "addCategory"
                        this["user_id"] = ExpiryUtils.userId
                        this["category"] = name
                        this["item_type"] = selectedItemType
                        this["cat_id"] = ""
                    }
                    binding.etItemName.text = "Select Item Name"
                    itemNamesList = hashMapOf()

                    addItemViewModel.addCategoryToServer(params)
                    addItemViewModel.categoryResponse.observe(this@AddItemActivity) { response ->
                        val status = response["status"]?.toString() ?: ""

                        println("After category added responses == $response")

                        if (status == "success") {
                            val categoryList = response["Category"] as? List<Map<String, Any>>
                            val lastCategory = categoryList?.lastOrNull()
                            val lastCategoryId =
                                lastCategory?.get("id")?.toString()?.replace(".0", "") ?: ""


                            println("Last added category_id = $lastCategoryId")
                            category_id = lastCategoryId
                            Toast.makeText(
                                this@AddItemActivity, "Category added!", Toast.LENGTH_SHORT
                            ).show()
                            binding.spCategories.text = name
                            onNewItemCreated(
                                mapOf(
                                    "category" to name, "category_id" to lastCategoryId
                                )
                            )

                            addItemViewModel.categoryResponse.removeObservers(this@AddItemActivity)
                        } else {
                            Toast.makeText(
                                this@AddItemActivity, "Failed to add category!", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

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
            "action" to "deleteItem", "user_id" to ExpiryUtils.userId, "item_id" to itemId
        )
        addItemViewModel.deleteitem(ExpiryUtils.userId, itemId, params)

        // Observe deletion result
        addItemViewModel.deleteitemResponse.observe(this) { response ->
            val status = response["status"]?.toString()
            if (status == "success") {
                binding.etItemName.text = "Select Item Name"
                Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_SHORT).show()
                onSuccess.invoke()
            } else {
                Toast.makeText(this, "Failed to delete!", Toast.LENGTH_SHORT).show()
            }

            // Prevent multiple triggers
            addItemViewModel.deleteitemResponse.removeObservers(this)
        }
    }


    private fun deleteCategory(itemId: Int, itemType: String, onSuccess: () -> Unit) {

        val params = hashMapOf<String, Any>(
            "action" to "deleteCategory", "user_id" to ExpiryUtils.userId, "cat_id" to itemId
        )
        addItemViewModel.deleteCategory(ExpiryUtils.userId, itemId, params)

        // Observe deletion result
        addItemViewModel.deletecatResponse.observe(this) { response ->
            val status = response["status"]?.toString()
            if (status == "success") {
                binding.spCategories.text = "Select Category"
                Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_SHORT).show()
                onSuccess.invoke()
            } else {
                Toast.makeText(this, "Failed to delete!", Toast.LENGTH_SHORT).show()
            }

            // Prevent multiple triggers
            addItemViewModel.deletecatResponse.removeObservers(this)
        }
    }

//AddItemActivity

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(
        itemName: String, expiryDate: String,      // yyyy-MM-dd from server
        notifyTime: String,      // HH:mm AM/PM
        reminderType: String,    // same day / 2 days before / 1 week before / custom
        customDate: String? = null
    ) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Convert expiryDate to dd_MM_yyyy for notification display
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
            putExtra("expiryDate", displayDate)
            putExtra("notifyTime", notifyTime)
            putExtra("itemId", editId)
        }

        println("itemIddd == $editId")

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            itemName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse time
        val timeParts = notifyTime.split(" ")
        val hourMinute = timeParts[0].split(":")
        var hour = hourMinute[0].toIntOrNull() ?: 0
        val minute = hourMinute.getOrNull(1)?.toIntOrNull() ?: 0
        val amPm = timeParts.getOrNull(1)?.uppercase(Locale.ENGLISH) ?: "AM"

        // Convert to 24-hour format
        if (amPm == "PM" && hour < 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0

        // Set calendar with expiry/custom date + time
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val calendar = Calendar.getInstance().apply {
            time = try {
                if (reminderType == "custom" && customDate != null) {
                    sdf.parse(customDate)!!
                } else {
                    sdf.parse(expiryDate)!!
                }
            } catch (e: Exception) {
                println("❌ Failed to parse date")
                return
            }

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Adjust for reminder type (if not custom)
        when (reminderType.lowercase(Locale.ENGLISH)) {
            "2 days before" -> calendar.add(Calendar.DAY_OF_MONTH, -2)
            "1 week before" -> calendar.add(Calendar.DAY_OF_MONTH, -7)
        }

        // Prevent past time
        val now = Calendar.getInstance()
        if (calendar.timeInMillis <= now.timeInMillis) {
            println("⚠️ Cannot schedule notification in the past: ${calendar.time}")
            return
        }


        println("✅ Notification scheduled for ${calendar.time} [Millis: ${calendar.timeInMillis}]")

        // Schedule the alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
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