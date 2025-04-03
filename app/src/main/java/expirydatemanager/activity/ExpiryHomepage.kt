package expirydatemanager.activity

import android.app.AlertDialog
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import expirydatemanager.Adapter.ExpiryItemAdapter_editdelete
import expirydatemanager.fragment.ExpiryViewModelFactory
import expirydatemanager.fragment.RenewFragment_home
import expirydatemanager.others.ExpirySharedPreference
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityExpiryDateHomepageBinding
import nithra.tamil.calendar.expirydatemanager.fragment.ExpiryFragment_home
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel

class ExpiryHomepage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityExpiryDateHomepageBinding
    private lateinit var toggle: ActionBarDrawerToggle
    val sharedPreference = ExpirySharedPreference()

    var itemNamesList: Map<String, Any> = hashMapOf()
    var categoriesList: Map<String, Any> = hashMapOf()
    private lateinit var adapter: ExpiryItemAdapter_editdelete
    private var dialog_type = ""
    var selectedType = ""


    private val repository by lazy { ExpiryRepository(ExpiryRetrofitInstance.instance) }
    private val expiryDateViewModel: ExpiryViewModel by viewModels {
        ExpiryViewModelFactory(repository)
    }

    val addItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            println("registerForActivityResult ==$result")
            println("registerForActivityResult == ${result.data?.extras}")

            if (result.resultCode == RESULT_OK && result.data?.getBooleanExtra(
                    "item_added", false
                ) == true
            ) {
                var selectedTabIndex = 0
                if (result.data?.getStringExtra(
                        "item_type",
                    ) == "1"
                ) {
                    selectedTabIndex = 0
                } else {
                    selectedTabIndex = 1
                }
                // selectedTabIndex = binding.tabLayout.selectedTabPosition
                val fragment = supportFragmentManager.fragments[selectedTabIndex]
                binding.viewPager.currentItem = selectedTabIndex
                println("selectedTabIndex ==$selectedTabIndex")
                if (fragment is ExpiryFragment_home) {
                    fragment.refreshList()
                } else if (fragment is RenewFragment_home) {
                    fragment.refreshList()
                }

                println("Fragment found: $fragment")  // Log to check if the fragment is found

            } else {
                println("It calls for delete")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiryDateHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation Drawer
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Set up ViewPager with TabLayout
        setupViewPager()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        if (ExpiryUtils.isNetworkAvailable(this)) {
            // ExpiryUtils.mProgress(this, "ஏற்றுகிறது. காத்திருக்கவும் ", true).show()
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getlist"
            InputMap["user_id"] = ExpiryUtils.userId
            InputMap["item_type"] = "1"

            expiryDateViewModel.fetchList1(InputMap)
        } else {
            // contentLayout.visibility = View.VISIBLE
        }

        val map = HashMap<String,Any>()
        map["action"] = "getItemName"
        map["user_id"] = ""+ExpiryUtils.userId

        expiryDateViewModel.fetchItemNames(map)
       // expiryDateViewModel.fetchItemNames(ExpiryUtils.userId)
        // Don't rely on items.clear() directly here, use a fresh copy
        expiryDateViewModel.itemNames.observeOnce(this@ExpiryHomepage) { updatedItemsMap ->
            itemNamesList = updatedItemsMap


        }


        expiryDateViewModel.categoryResponse.observe(this, Observer { message ->
            Toast.makeText(this, message["status"].toString(), Toast.LENGTH_SHORT).show()
        })

        binding.btnAddItem.setOnClickListener {
            val selectedTab = binding.tabLayout.selectedTabPosition
            val itemType = if (selectedTab == 0) "1" else "2"
            println("itemType for add item ==$itemType")
            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra("itemType", itemType)
            addItemLauncher.launch(intent)
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
        when (item.itemId) {/* R.id.nav_item -> {
                 showCreateDialog("itemnames")
             }

             R.id.nav_category -> {
                 showCreateDialog("categorys")
             }*/

            R.id.last3days -> {/* val i = Intent(this@ExpiryHomepage, ExpiryHomepage::class.java)
                 startActivity(i)*/
            }

            R.id.category -> {
                val i = Intent(this@ExpiryHomepage, ExpiryCategory::class.java)
                i.putExtra("title", "Category")
                startActivity(i)
            }

            /*  R.id.categorylist -> {
                  dialog_type = "categorys"
                  if (ExpiryUtils.isNetworkAvailable(this)) {
                      val GetItems =
                          itemNamesList["Items"] as? MutableList<Map<String, Any>> ?: mutableListOf()
                      println("GetItems == $GetItems")

                      showSelectionDialog("Select Category Name", GetItems) { selectedName ->
                      }
                  } else {
                      Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                  }
              }

              R.id.itemlist -> {
                  dialog_type = "Item name"
                  if (ExpiryUtils.isNetworkAvailable(this)) {
                      val GetItems =
                          itemNamesList["Items"] as? MutableList<Map<String, Any>> ?: mutableListOf()

                      println("GetItems == $itemNamesList")
                      showSelectionDialog("Select Item Name", GetItems) { selectedName ->
                      }
                  } else {
                      Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                  }
              }*//* R.id.alllist -> {
                 val i = Intent(this@ExpiryHomepage, ExpiryAllList::class.java)
                 startActivity(i)
             }*/

            R.id.appshare -> {
                val shareText =
                    """நித்ரா காலண்டர் வழியாக பகிரப்பட்டது. ஆண்ட்ராய்டு மொபைலில் தரவிறக்கம் செய்ய https://goo.gl/XOqGPp
    ஆப்பிள் மொபைலில் தரவிறக்கம் செய்ய : http://bit.ly/iostamilcal
    
    தமிழில் மிகச்சிறந்த காலண்டரான நித்ரா காலண்டரை  இலவசமாக  உங்கள் ஆண்ட்ராய்டு மொபைலில் தரவிறக்கம் செய்ய : https://goo.gl/XOqGPp 
    ஆப்பிள் மொபைலில் தரவிறக்கம் செய்ய : http://bit.ly/iostamilcal"""
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }


            /* R.id.itemlist -> {
                 val i = Intent(this@ExpiryHomepage, ExpiryItemList::class.java)
                 startActivity(i)
             }*/
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
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

            } else {
                expiryDateViewModel.fetchCategories(ExpiryUtils.userId, selectedType)
                expiryDateViewModel.categories.observeOnce(this@ExpiryHomepage) { categories ->
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
        }, onEdit = { itemName, itemId, itemType -> }, onDelete = { itemId, itemType -> })


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Search functionality
        etSearch.addTextChangedListener {
            adapter.filter(it.toString()) // Filter list based on search input
        }

        dialog.show()
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

            val itemTypes = arrayOf("Expiry Item", "Renew Item")
            val adapter =
                android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, itemTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerItemType.adapter = adapter
        } else {
            etItemName.hint = "Enter Item Name"
            spinnerItemType.visibility = View.GONE
            //spinnercard.visibility = View.GONE
        }



        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            val selectedTabIndex = binding.tabLayout.selectedTabPosition
            val selectedItemType =
                if (selectedTabIndex == 0) "1" else "2" // 1 for expiry, 2 for renew

            if (name.isNotEmpty()) {
                if (tableName == "itemnames") {
                    println("item nameeee===== $name")

                    val params = HashMap<String, String>().apply {
                        this["action"] = "addItemName"
                        this["user_id"] = "" + ExpiryUtils.userId
                        this["itemname"] = etItemName.text.toString()
                        this["item_id"] = "" // if edit only
                    }

                    expiryDateViewModel.addItemToServer(params)

                    // expiryDateViewModel.addItemToServer(name, 0)
                } else if (tableName == "categorys") {
                    println("cat nameeee===== $name")

                    val params = HashMap<String, Any>().apply {
                        this["action"] = "addCategory"
                        this["user_id"] = ExpiryUtils.userId
                        this["category"] = etItemName.text.toString().trim()
                        this["item_type"] = selectedItemType  // ✅ Use from selected tab
                        this["cat_id"] = ""
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