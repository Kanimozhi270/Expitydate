package expirydatemanager.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import expirydatemanager.fragment.ExpiryViewModelFactory
import expirydatemanager.fragment.RenewFragment_home
import nithra.tamil.calendar.expirydatemanager.R
import expirydatemanager.others.ExpirySharedPreference
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityExpiryDateHomepageBinding
import nithra.tamil.calendar.expirydatemanager.fragment.ExpiryFragment_home
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel

class ExpiryHomepage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityExpiryDateHomepageBinding
    private lateinit var toggle: ActionBarDrawerToggle
    val sharedPreference = ExpirySharedPreference()
    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    private val expiryDateViewModel: ExpiryViewModel by viewModels{
        ExpiryViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiryDateHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation Drawer
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.appBar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Set up ViewPager with TabLayout
        setupViewPager()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        // Observe LiveData from the ViewModel
        expiryDateViewModel.itemNameResponse.observe(this, Observer { message ->

        })

        expiryDateViewModel.categoryResponse.observe(this, Observer { message ->
            Toast.makeText(this, message["status"].toString(), Toast.LENGTH_SHORT).show()
        })

        binding.btnAddItem.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) ExpiryFragment_home() else RenewFragment_home()
            }
            override fun getItemCount() = 2
        }

        // Attach TabLayout to ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Expiry Item" else "Renew Item"
        }.attach()
    }

    override fun onResume() {
        super.onResume()

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
                val i = Intent(this@ExpiryHomepage, ExpiryItemList::class.java)
                startActivity(i)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun showCreateDialog(tableName: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etItemName = dialog.findViewById<EditText>(R.id.etItemName)
        val spinnerItemType = dialog.findViewById<android.widget.Spinner>(R.id.spinnerItemType)
        //val spinnercard = dialog.findViewById<androidx.cardview.widget.CardView>(R.id.spinnerItemType)
        val btnCreate = dialog.findViewById<Button>(R.id.btnCreate)

        if (tableName == "categorys") {
            etItemName.hint = "Enter Category Name"
            spinnerItemType.visibility = View.VISIBLE

            val itemTypes = arrayOf("expiry item", "renew item")
            val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, itemTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerItemType.adapter = adapter
        } else {
            etItemName.hint = "Enter Item Name"
            spinnerItemType.visibility = View.GONE
            //spinnercard.visibility = View.GONE
        }

        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            val selectedType = if (tableName == "categorys") spinnerItemType.selectedItem.toString() else ""

            if (name.isNotEmpty()) {
                if (tableName == "itemnames") {
                    println("item nameeee===== $name")

                    val params = HashMap<String, String>().apply {
                        this["action"] = "addItemName"
                        this["user_id"] = "989015"
                        this["itemname"] = etItemName.text.toString()
                        this["item_id"] = "" // if edit only
                    }

                    expiryDateViewModel.addItemToServer(params)

                   // expiryDateViewModel.addItemToServer(name, 0)
                } else if (tableName == "categorys") {
                    println("cat nameeee===== $name")

                    val params = HashMap<String, Any>().apply {
                        this["action"] = "addCategory"
                        this["user_id"] = "989015"
                        this["category"] = etItemName
                        this["item_type"] = if (spinnerItemType.selectedItem.toString() == "expiry item") "1" else "2"
                        this["cat_id"] = "" // if edit only
                    }

                    expiryDateViewModel.addCategoryToServer(params)
                }
                dialog.dismiss()
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
                    this@ExpiryHomepage,
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
                val i = Intent(this@ExpiryHomepage, ExpiryCategory::class.java)
                i.putExtra("title", "Homepage")
                startActivity(i)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
