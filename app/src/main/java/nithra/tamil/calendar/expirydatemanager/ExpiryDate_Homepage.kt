package nithra.tamil.calendar.expirydatemanager

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityExpiryDateHomepageBinding

class ExpiryDate_Homepage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityExpiryDateHomepageBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiryDateHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.expirydate_calendar)

        // Navigation Drawer
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.appBar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Create database and table directly in Homepage
        db = openOrCreateDatabase("expirydatemanager.db", Context.MODE_PRIVATE, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "itemname TEXT, " +
                    "itemtype TEXT, " +
                    "category TEXT, " +
                    "expiry_date TEXT, " +
                    "reminderbefore TEXT, " +
                    "notify_time TEXT, " +
                    "note TEXT)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS itemnames (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "itemname TEXT)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS categorys (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "categoryname TEXT, " +
                    "itemtype TEXT)"
        )



        binding.btnAddItem.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        // Check if the items table has data
        if (hasItemsData()) {
            showTabs()
        } else {
            showContentLayout()
        }
    }


    // Method to check if items table has data
    private fun hasItemsData(): Boolean {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM items", null)
        var hasData = false
        if (cursor.moveToFirst()) {
            hasData = cursor.getInt(0) > 0
        }
        cursor.close()
        return hasData
    }

    // Method to show tabs and hide content layout
    private fun showTabs() {
        binding.contentLayout.visibility = android.view.View.GONE
        binding.tabLayout.visibility = android.view.View.VISIBLE
        binding.viewPager.visibility = android.view.View.VISIBLE

        // Setup ViewPager and Tabs
        binding.viewPager.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): androidx.fragment.app.Fragment {
                return if (position == 0) {
                    ExpiryCategoryFragment()
                } else {
                    RenewCategoryFragment()
                }
            }
        }

        com.google.android.material.tabs.TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Expiry Item" else "Renew Item"
        }.attach()
    }

    // Method to show content layout and hide tabs
    private fun showContentLayout() {
        binding.contentLayout.visibility = android.view.View.VISIBLE
        binding.tabLayout.visibility = android.view.View.GONE
        binding.viewPager.visibility = android.view.View.GONE
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item -> {
                showCreateDialog("itemnames")
            }
            R.id.nav_category -> {
                showCreateDialog("categorys")
            }
            R.id.itemlist -> {
                val i = Intent(this@ExpiryDate_Homepage, ItemNamesList::class.java)
                startActivity(i)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun showCreateDialog(tableName: String) {
        val dialog = Dialog(
            this@ExpiryDate_Homepage,
            android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
        )
        dialog.setContentView(R.layout.dialog_create_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etItemName = dialog.findViewById<EditText>(R.id.etItemName)
        val spinnerItemType = dialog.findViewById<android.widget.Spinner>(R.id.spinnerItemType)
        val btnCreate = dialog.findViewById<Button>(R.id.btnCreate)

        // Show or hide spinner based on table name
        if (tableName == "categorys") {
            etItemName.hint = "Enter Category Name"
            spinnerItemType.visibility = android.view.View.VISIBLE

            // Populate spinner with options
            val itemTypes = arrayOf("expiry item", "renew item")
            val adapter = android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                itemTypes
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerItemType.adapter = adapter
        } else {
            etItemName.hint = "Enter Item Name"
            spinnerItemType.visibility = android.view.View.GONE
        }

        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            val selectedType = if (tableName == "categorys") spinnerItemType.selectedItem.toString() else ""

            if (name.isNotEmpty()) {
                val contentValues = ContentValues()
                if (tableName == "itemnames") {
                    contentValues.put("itemname", name)
                } else if (tableName == "categorys") {
                    contentValues.put("categoryname", name)
                    contentValues.put("itemtype", selectedType)  // Save item type for category
                }

                val result = db.insert(tableName, null, contentValues)
                if (result != -1L) {
                    Toast.makeText(this, "Added successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Failed to add!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }



    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.expiry_home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_info -> {
                val infoDialog = Dialog(
                    this@ExpiryDate_Homepage,
                    android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth
                )
                infoDialog.setContentView(R.layout.expiry_info)
                infoDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                val btnSend: AppCompatButton = infoDialog.findViewById(R.id.btnSend)
                btnSend.setOnClickListener { infoDialog.dismiss() }
                infoDialog.show()
                return true
            }
            R.id.action_category -> {
                val i = Intent(this@ExpiryDate_Homepage, Category::class.java)
                i.putExtra("title", "Homepage")
                startActivity(i)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
