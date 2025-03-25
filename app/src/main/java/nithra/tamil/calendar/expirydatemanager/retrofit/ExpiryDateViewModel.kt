    package nithra.tamil.calendar.expirydatemanager.retrofit

    import androidx.lifecycle.LiveData
    import androidx.lifecycle.MutableLiveData
    import androidx.lifecycle.ViewModel
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response

    class ExpiryDateViewModel : ViewModel() {

        // LiveData for responses
        private val _itemNameResponse = MutableLiveData<HashMap<String, Any>>()
        val itemNameResponse: LiveData<HashMap<String, Any>> get() = _itemNameResponse

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

        private val _itemlist = MutableLiveData<Map<String, Any>>()
        val itemList: LiveData<Map<String, Any>> get() = _itemlist

        private val _categories = MutableLiveData<Map<String, Any>>()
        val categories: LiveData<Map<String, Any>> get() = _categories

        private val _error = MutableLiveData<List<String>>()
        val error: LiveData<List<String>> get() = _error

        private val apiService = RetrofitClient.instance


        fun addItemToServer(itemName: String) {
            val params = HashMap<String, String>().apply {
                this["action"] = "addItemName"
                this["user_id"] = "989015"
                this["itemname"] = itemName
                this["item_id"] = "" // if edit only
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
                    _error.value = listOf("Error: ${t.message}")
                }
            })
        }

        fun addCategoryToServer(categoryName: String, itemType: String) {
            val params = HashMap<String, Any>().apply {
                this["action"] = "addCategory"
                this["user_id"] = "989015"
                this["category"] = categoryName
                this["item_type"] = if (itemType == "expiry item") "1" else "2"
                this["cat_id"] = "" // if edit only
            }
            println("item name send to server category==$params")
            apiService.addCategory(params).enqueue(object : Callback<HashMap<String, Any>> {
                override fun onResponse(
                    call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        println("_categoryResponse  ===== ${response.body()}")
                        _categoryResponse.value = response.body()
                    } else {
                        _categoryResponse.value = hashMapOf("status" to "Failed to add category!")
                        println("_categoryResponse  ===== ${_categoryResponse.value}")
                    }
                }

                override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                    _categoryResponse.value = hashMapOf("status" to "Error: ${t.message}")
                    println("_categoryResponse  ===== ${_categoryResponse.value}")
                }
            })
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
            customDate: String? = null
        ) {
            val params = HashMap<String, Any>().apply {
                this["action"] = "addList"
                this["user_id"] = "989015"
                this["category_id"] = categoryId.toString()
                this["item_type"] = itemType.toString()
                this["item_id"] = itemId.toString()
                this["reminder_type"] = reminderType.toString()
                this["notify_time"] = notifyTime
                this["remark"] = remark
                this["action_date"] = actionDate
                listId?.let { this["list_id"] = it.toString() }
                customDate?.let { this["custom_date"] = it }
            }

            println("list send to server==$params")

            apiService.addList(params).enqueue(object : Callback<HashMap<String, Any>> {
                override fun onResponse(
                    call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        var data = response.body();
                        if (data != null && data.containsKey("status") && data["status"] == "success") {
                            println("item added successfully")
                        } else {
                            println("item not added")

                        }

                    } else {
                        _listResponse.value = "Failed to add list!"
                    }
                }

                override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                    _listResponse.value = "Error: ${t.message}"
                }
            })
        }


        fun fetchList(userId: Int) {
            val action = "getlist"
            apiService.getItemlist(action, userId, 1, 1)
                .enqueue(object : Callback<HashMap<String, Any>> {
                    override fun onResponse(
                        call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
                    ) {
                        if (response.isSuccessful) {
                            val itemsList =
                                (response.body()?.get("Items") as? List<Map<String, Any>>)?.map {
                                    it["item_name"] as? String ?: ""
                                } ?: emptyList()

                            var data = response.body();

                            if (data != null && data.containsKey("status") && data["status"] == "success") {

                                _itemlist.value = data!!

                            } else {

                                println("Not success")

                            }

                            println("_itemNames  ===== ${_itemlist.value}")
                        }
                    }

                    override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                        println("_itemNames  ===== ${t.message}")
                    }
                })
        }

        /*fun deletelist(itemName: String) {
            val params = HashMap<String, String>().apply {
                this["action"] = "deleteList"
                this["user_id"] = "989015"
                this["list_id"] = "1"
            }
            println("item name send to server==$params")

            apiService.deletelist(params).enqueue(object : Callback<HashMap<String, Any>> {
                override fun onResponse(
                    call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        println("response body=====vv ${response.body()}")

                        //_itemNameResponse.value = response.body()?.get("message") ?: "Item added successfully!"
                        _deletelistResponse.value = response.body()
                    } else {
                        println("response body=====${response.body()}")
                        //_itemNameResponse.value = "Failed to add item!"
                    }
                }

                override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                    _error.value = listOf("Error: ${t.message}")
                }
            })
        }*/

        fun deleteCategory(itemName: String) {
            val params = HashMap<String, Any>().apply {
                this["action"] = "deleteCategory"
                this["user_id"] = "989015"
                this["cat_id"] = "1"

            }
            println("item name send to server==$params")

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
                    _error.value = listOf("Error: ${t.message}")
                }
            })
        }

        fun deleteitem(itemName: String) {
            val params = HashMap<String, Any>().apply {
                this["action"] = "deleteItem"
                this["user_id"] = "989015"
                this["item_id"] = "1"

            }
            println("item name send to server==$params")

            apiService.deleteitem(params).enqueue(object : Callback<HashMap<String, Any>> {
                override fun onResponse(
                    call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        println("response body=====vv ${response.body()}")

                        //_itemNameResponse.value = response.body()?.get("message") ?: "Item added successfully!"
                        _deleteitemResponse.value = response.body()
                    } else {
                        println("response body=====${response.body()}")
                        //_itemNameResponse.value = "Failed to add item!"
                    }
                }

                override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                    _error.value = listOf("Error: ${t.message}")
                }
            })
        }


    }


