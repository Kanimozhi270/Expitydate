package expirydatemanager.activity

import android.app.Activity
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import expirydatemanager.Adapter.ExpiryItemAdapter_editdelete
import expirydatemanager.fragment.ExpiryViewModelFactory
import expirydatemanager.fragment.RenewFragment_home
import nithra.tamil.calendar.expirydatemanager.R
import expirydatemanager.others.ExpirySharedPreference
import expirydatemanager.others.ExpiryUtils
import expirydatemanager.pojo.ItemList
import expirydatemanager.retrofit.ExpiryRepository
import kotlinx.coroutines.launch
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityExpiryDateHomepageBinding
import nithra.tamil.calendar.expirydatemanager.fragment.ExpiryFragment_home
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryRetrofitInstance
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

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
            if (result.resultCode == Activity.RESULT_OK && result.data?.getBooleanExtra(
                    "item_added",
                    false
                ) == true
            ) {
                val selectedTabIndex = binding.tabLayout.selectedTabPosition
                val fragmentTag = "f$selectedTabIndex"
                val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)

                if (fragment is ExpiryFragment_home) {
                    fragment.refreshList()
                } else if (fragment is RenewFragment_home) {
                    fragment.refreshList()
                }
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



        expiryDateViewModel.categoryResponse.observe(this, Observer { message ->
            Toast.makeText(this, message["status"].toString(), Toast.LENGTH_SHORT).show()
        })

        binding.btnAddItem.setOnClickListener {
            val selectedTab = binding.tabLayout.selectedTabPosition
            val itemType = if (selectedTab == 0) "expiry item" else "renew item"

            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra("itemType", itemType) // pass the itemType
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
        when (item.itemId) {
            R.id.nav_item -> {
                showCreateDialog("itemnames")
            }

            R.id.nav_category -> {
                showCreateDialog("categorys")
            }

            R.id.last3days -> {
               /* val i = Intent(this@ExpiryHomepage, ExpiryHomepage::class.java)
                startActivity(i)*/
            }
            R.id.category -> {
                val i = Intent(this@ExpiryHomepage, ExpiryCategory::class.java)
                i.putExtra("title", "Category")
                startActivity(i)
            }
            R.id.categorylist -> {
                dialog_type = "categorys"
              /*  val i = Intent(this@ExpiryHomepage, ExpiryCategoryList::class.java)
                startActivity(i)*/
            }
            R.id.itemlist -> {
                dialog_type = "Item name"
                if (ExpiryUtils.isNetworkAvailable(this)) {
                    val GetItems =
                        itemNamesList["Items"] as? MutableList<Map<String, Any>> ?: mutableListOf()
                    println("GetItems == $GetItems")

                    showSelectionDialog("Select Item Name", GetItems) { selectedName ->
                    }
                } else {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
           /* R.id.alllist -> {
               *//* val i = Intent(this@ExpiryHomepage, ExpiryAllList::class.java)
                startActivity(i)*//*
            }*/

            R.id.appshare -> {
                val shareText = """நித்ரா காலண்டர் வழியாக பகிரப்பட்டது. ஆண்ட்ராய்டு மொபைலில் தரவிறக்கம் செய்ய https://goo.gl/XOqGPp
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
                expiryDateViewModel.fetchItemNames(989015)
                // Don't rely on items.clear() directly here, use a fresh copy
                expiryDateViewModel.itemNames.observeOnce(this@ExpiryHomepage) { updatedItemsMap ->
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
                expiryDateViewModel.fetchCategories(989015, selectedType)
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
        }, onEdit = { itemName, itemId, itemType ->



        }, onDelete = { itemId, itemType ->


        })


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
                        this["user_id"] = ExpiryUtils.userId
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

// Expiry View model
class ExpiryViewModel(val repository: ExpiryRepository) : ViewModel() {

    // LiveData for responses
    private val _itemNameResponse = MutableLiveData<HashMap<String, Any>>()
    val itemNameResponse: LiveData<HashMap<String, Any>> get() = _itemNameResponse

    private val _itemNameResponse1 = MutableLiveData<HashMap<String, Any>>()
    val itemNameResponse1: LiveData<HashMap<String, Any>> get() = _itemNameResponse1


    private val _deletelistResponse = MutableLiveData<HashMap<String, Any>>()
    val deletelistResponse: LiveData<HashMap<String, Any>> get() = _deletelistResponse

    private val _deleteitemResponse = MutableLiveData<HashMap<String, Any>>()
    val deleteitemResponse: LiveData<HashMap<String, Any>> get() = _deleteitemResponse

    private val _deletecatResponse = MutableLiveData<HashMap<String, Any>>()
    val deletecatResponse: LiveData<HashMap<String, Any>> get() = _deletecatResponse


    private val _categoryResponse = MutableLiveData<HashMap<String, Any>>()
    val categoryResponse: LiveData<HashMap<String, Any>> get() = _categoryResponse

    private val _listResponse = MutableLiveData<String>()
    val listResponse: LiveData<String> get() = _listResponse

    private val _itemNames = MutableLiveData<Map<String, Any>>()
    val itemNames: LiveData<Map<String, Any>> get() = _itemNames

    private val _itemlist1 = MutableLiveData<ItemList>()
    val itemList1: LiveData<ItemList> get() = _itemlist1

    private val _categories = MutableLiveData<Map<String, Any>>()
    val categories: LiveData<Map<String, Any>> get() = _categories

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val apiService = ExpiryRetrofitInstance.instance

    /* fun addItemToServer(itemName: String, itemId: Int) {
         val params = HashMap<String, String>().apply {
             this["action"] = "addItemName"
             this["user_id"] = "989015"
             this["itemname"] = itemName
             this["item_id"] = "$itemId" // if edit only
         }
         println("item name send to server==$params")

         apiService.addItem(params).enqueue(object : Callback<HashMap<String, Any>> {
             override fun onResponse(
                 call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
             ) {
                 if (response.isSuccessful) {
                     println("response body=====vv ${response.body()}")

                     _itemNameResponse.value = response.body()
                 } else {
                     println("response body=====${response.body()}")
                 }
             }

             override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                 _error.value = t.message
             }
         })
     }*/

    fun addItemToServer(params: HashMap<String, String>) {
        viewModelScope.launch {
            try {
                val response = repository.addItem(params)
                _itemNameResponse1.value = response
                println("ExpiryResponse - == ${_itemNameResponse1.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message

            }
        }
    }


    fun addCategoryToServer(params: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.addCategory(params)
                _categoryResponse.value = response
                println("ExpiryResponse - == ${_categoryResponse.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message

            }
        }
    }

    fun fetchItemNames(userId: Int) {
        val action = "getItemName"
        apiService.getItemNames(action, userId).enqueue(object : Callback<HashMap<String, Any>> {
            override fun onResponse(
                call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
            ) {
                if (response.isSuccessful) {
                    val itemsList =
                        (response.body()?.get("Items") as? List<Map<String, Any>>)?.map {
                            it["item_name"] as? String ?: ""
                        } ?: emptyList()

                    _itemNames.value = response.body()
                    println("_itemNames  ===== ${_itemNames.value}")
                }
            }

            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                println("_itemNames  ===== ${t.message}")
            }
        })
    }

    fun fetchCategories(userId: Int, itemType: String) {
        var item_type = if (itemType == "expiry item") "1" else "2"

        apiService.getCategories("getCategory", userId, item_type)
            .enqueue(object : Callback<HashMap<String, Any>> {
                override fun onResponse(
                    call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        val categoryMap = response.body() ?: mapOf()
                        _categories.value = categoryMap as HashMap<String, Any>

                    }
                }

                override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                    println("_categories  ===== ${t.message}")
                }
            })
    }

    fun addListToServer(
        categoryId: Int,
        itemType: Int,
        itemId: Int,
        reminderType: Int,
        notifyTime: String,
        remark: String,
        actionDate: String,
        listId: Int,//if edit only
        customDate: String? = null,
        id: String
    ) {

        val params = HashMap<String, Any>().apply {
            this["action"] = "addList"
            this["user_id"] = ExpiryUtils.userId
            this["category_id"] = categoryId.toString()
            this["item_type"] = itemType
            this["item_id"] = itemId.toString()
            this["reminder_type"] = reminderType.toString()
            this["notify_time"] = notifyTime
            this["remark"] = remark
            this["action_date"] = actionDate
            this["list_id"] = id ?: ""
            customDate?.let { this["custom_date"] = it }
        }

        println("list send to server==$params")

        viewModelScope.launch {
            try {
                val response = repository.addList(params)
                _itemNameResponse.value = response
                println("ExpiryResponse - == ${_deleteitemResponse.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message
            }
        }

        /*  apiService.addList(params).enqueue(object : Callback<HashMap<String, Any>> {
              override fun onResponse(
                  call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
              ) {
                  if (response.isSuccessful) {
                      var data = response.body()
                      println("item added successfully == $data")
                      if (data != null && data.containsKey("status") && data["status"] == "success") {
                          println("item added successfully == $data")
                      } else {
                          println("item not added")

                      }

                  } else {
                      _listResponse.value = "Failed to add list!"
                      println("Failed to add List!")
                  }
              }

              override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                  _listResponse.value = "Error: ${t.message}"
                  println("Error on add list  ==${t.message}")
              }
          })*/
    }

    /*fun fetchList1(userId: Int, item_id: Int, is_days: Int) {
        val action = "getlist"

        apiService.getItemlist(action, userId, item_id, is_days)
            .enqueue(object : Callback<ItemList> {
                override fun onResponse(call: Call<ItemList>, response: Response<ItemList>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        _itemlist1.postValue(responseBody!!)

                    }
                }

                override fun onFailure(call: Call<ItemList>, t: Throwable) {
                    println("_itemNames  ===== ${t.message}")
                }
            })
    }*/

    fun fetchList1(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getItemlist(InputMap)
                _itemlist1.value = response
                println("ExpiryResponse - == ${_itemlist1.value}")
            } catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _error.value = e.message
            } catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _error.value = e.message
            } catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _error.value = e.message
            }
        }
    }

    fun deletelist(userId: Int, list_id: Int, params: HashMap<String, Any>) {

        println("item name send to server==$params")

        viewModelScope.launch {
            try {
                val response = repository.addList(params)
                _deletelistResponse.value = response
                println("ExpiryResponse - == ${_deleteitemResponse.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message
            }
        }
    }


    fun deleteCategory(userId: Int, cat_id: Int) {
        val params = HashMap<String, Any>().apply {
            this["action"] = "deleteCategory"
            this["user_id"] = ExpiryUtils.userId
            this["cat_id"] = "1"

        }
        println("item name send to server==$params")

        viewModelScope.launch {
            try {
                val response = repository.deleteCategory(params)
                _deletecatResponse.value = response
                println("ExpiryResponse - == ${_deleteitemResponse.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message
            }
        }
     /*
        apiService.deletecat(params).enqueue(object : Callback<HashMap<String, Any>> {
            override fun onResponse(
                call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
            ) {
                if (response.isSuccessful) {
                    println("response body=====vv ${response.body()}")

                    //_itemNameResponse.value = response.body()?.get("message") ?: "Item added successfully!"
                    _deletecatResponse.value = response.body()
                } else {
                    println("response body=====${response.body()}")
                    //_itemNameResponse.value = "Failed to add item!"
                }
            }

            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                _error.value = t.message
            }
        })*/
    }

    fun deleteitem(userId: Int, item_id: Int, params: HashMap<String, Any>) {


        println("item name send to server==$params")

        viewModelScope.launch {
            try {
                val response = repository.deleteItem(params)
                _deleteitemResponse.value = response  // ✅ use correct LiveData
                println("ExpiryResponse - == ${_deleteitemResponse.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message
            }
        }
    }


}
