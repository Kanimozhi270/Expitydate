package com.nithra.aanmeega_service.fragment

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import nithra.tamil.calendar.expirydatemanager.Adapter.CustomDayAdapter
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityAanmeegaDatePickerDialogBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomDatePickerDialog(
    private val onDateSelected: (String) -> Unit,
    private var initialDate: String? = null // <-- Added this parameter
) : DialogFragment() {

    private lateinit var binding: ActivityAanmeegaDatePickerDialogBinding
    private val calendar = Calendar.getInstance()
    private var selectedDay: String? = null
    private var selectedMonth: Int? = null
    private var selectedYear: Int? = null
    var lastvalue = ""
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = ActivityAanmeegaDatePickerDialogBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(
            requireContext(),
            android.R.style.Theme_DeviceDefault_Dialog_MinWidth
        )
            .setView(binding.root)
            .create()

        dialog.setCanceledOnTouchOutside(false) // Prevent outside touch dismissal
        setupUI()
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false) // Prevent dismiss on back press
    }

    private fun setupUI() {

        // Initialize Weekdays (If needed as dynamic)
        val weekdays = getWeekdayList()

        val layoutWeekdays = binding.layoutWeekdays
        layoutWeekdays.removeAllViews() // Clear existing views if any

        weekdays.forEach { day ->
            val textView = TextView(context).apply {
                text = day
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                /*typeface = ResourcesCompat.getFont(
                    context, R.font.lexend_medium
                )*/
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Equal weight for all days
                ).apply {
                    gravity = Gravity.CENTER
                }
                gravity = Gravity.CENTER
            }
            layoutWeekdays.addView(textView)
        }


        if (!initialDate.isNullOrEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("ta", "IN")) // Tamil locale
                val date = dateFormat.parse(initialDate)
                if (date != null) {
                    calendar.time = date
                    selectedDay = calendar.get(Calendar.DAY_OF_MONTH).toString()
                    selectedMonth = calendar.get(Calendar.MONTH)
                    selectedYear = calendar.get(Calendar.YEAR)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Handle parsing error if needed
            }
        } else {
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH).toString()
            selectedMonth = calendar.get(Calendar.MONTH)
            selectedYear = calendar.get(Calendar.YEAR)
        }

        updateCalendar()

        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnSetDate.setOnClickListener {
            val selectedDate =
                "${calendar.get(Calendar.DAY_OF_MONTH)} ${getMonthName(calendar.get(Calendar.MONTH))} ${
                    calendar.get(Calendar.YEAR)
                }"
            onDateSelected(selectedDate)
            dismiss()
        }
        // Populate month spinner
        val monthAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, getMonthList())
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(calendar.get(Calendar.MONTH)) // Set default selection

        // Get the year list once
        val yearList = getYearList()

// Populate year spinner
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, yearList)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter

// Set default selection without reloading the list
        val currentYearIndex = yearList.indexOf(calendar.get(Calendar.YEAR).toString())
        if (currentYearIndex >= 0) {
            binding.spinnerYear.setSelection(currentYearIndex)
        }
        // Month selection listener
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                calendar.set(Calendar.MONTH, position)
                maintainSelectedDay()
                updateCalendar()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Year selection listener
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedYear = parent?.getItemAtPosition(position).toString().toInt()
                calendar.set(Calendar.YEAR, selectedYear)
                maintainSelectedDay()
                updateCalendar()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun maintainSelectedDay() {
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        println("current == $currentMonth")
        println("current year == $currentYear")
        println("current selected == $selectedMonth")
        println("current selected year == $selectedYear")
        println("current selected year == $lastvalue")

        // Maintain selectedDay only if it's from the currently displayed month and year
        if (currentMonth == selectedMonth && currentYear == selectedYear) {
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (!lastvalue.equals("")){
                selectedDay = lastvalue
            }
            selectedDay?.let {
                val day = it.toInt()
                if (day <= daysInMonth) {
                    println("days == $day")
                    println("days montha == $daysInMonth")
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                } else {
                    println("days selectedDay == $selectedDay")
                    selectedDay = null // If day doesn't exist, clear selection
                }
            }
        } else {
            println("days selectedDay 1== $selectedDay")
            if (selectedDay != null){
                lastvalue = "" + selectedDay
            }
            selectedDay = null // Clear selection if month/year changes
        }
    }

    private fun getWeekdayList(): List<String> {
        return listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }

    private fun updateCalendar() {
        val daysList = getDaysInMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

        val adapter = CustomDayAdapter(daysList, requireContext(), selectedDay) { day ->
            selectedDay = day
            calendar.set(Calendar.DAY_OF_MONTH, day.toInt())
            selectedMonth = calendar.get(Calendar.MONTH)
            selectedYear = calendar.get(Calendar.YEAR)
        }

        binding.recyclerViewDays.layoutManager =
            StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerViewDays.adapter = adapter

        // Set default date selection if available
        selectedDay?.let { day ->
            adapter.setSelectedDate(day)
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): List<String> {

        val daysList = mutableListOf<String>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2, etc.
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Add empty strings for days before the first day of the month
        for (i in 1 until firstDayOfWeek) {
            daysList.add("") // Empty slots
        }
        // Add actual dates
        for (day in 1..daysInMonth) {
            daysList.add(day.toString())
        }
        return daysList
    }


    private fun getMonthList(): Array<String> {
        return arrayOf(
            "ஜனவரி",
            "பிப்ரவரி",
            "மார்ச்",
            "ஏப்ரல்",
            "மே",
            "ஜூன்",
            "ஜூலை",
            "ஆகஸ்ட்",
            "செப்டம்பர்",
            "அக்டோபர்",
            "நவம்பர்",
            "டிசம்பர்"
        )
    }

    private fun getYearList(): Array<String> {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return Array(currentYear + 2 - 1940) { index -> (currentYear + 1 - index).toString() } // Until next year

    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "ஜனவரி",
            "பிப்ரவரி",
            "மார்ச்",
            "ஏப்ரல்",
            "மே",
            "ஜூன்",
            "ஜூலை",
            "ஆகஸ்ட்",
            "செப்டம்பர்",
            "அக்டோபர்",
            "நவம்பர்",
            "டிசம்பர்"
        )
        return months[month]
    }
}

