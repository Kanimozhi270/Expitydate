/*
package nithra.tamil.calendar.expirydatemanager.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import expirydatemanager.pojo.ItemList
import expirydatemanager.retrofit.ExpiryRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

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

    */
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
     }*//*


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
            this["user_id"] = "989015"
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

        */
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
          })*//*

    }

    */
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
    }*//*


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
                val response = repository.deleteList(params)
                _deletelistResponse.value = response
                println("ExpiryResponse - == ${_deletelistResponse.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message
            }
        }
    }


    fun deleteCategory(userId: Int, cat_id: Int, params: HashMap<String, Any>) {
        val params = HashMap<String, Any>().apply {
            this["action"] = "deleteCategory"
            this["user_id"] = "989015"
            this["cat_id"] = cat_id
        }
        println("item name send to server==$params")
        viewModelScope.launch {
            try {
                val response = repository.deleteCategory(params)
                _deletecatResponse.value = response  // ✅ use correct LiveData
                println("ExpiryResponse - == ${_deleteitemResponse.value}")
            } catch (t: SocketTimeoutException) {
                println("exception == ${t.toString()}")
                _error.value = t.message
            }
        }
        */
/*  println("item name send to server==$params")
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
          })*//*

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


}*/
