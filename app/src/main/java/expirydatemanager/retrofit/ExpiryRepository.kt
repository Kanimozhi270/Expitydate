package expirydatemanager.retrofit

import expirydatemanager.pojo.ItemList

class ExpiryRepository(private val api: ExpiryRetrofitInterface) {

    suspend fun getItemlist(requestInputMap: HashMap<String, Any>): ItemList {
        return api.getItemlist(requestInputMap)
    }
    suspend fun addItem(requestInputMap: HashMap<String, String>): HashMap<String, Any> {
        return api.addItem(requestInputMap)
    }
    suspend fun addCategory(requestInputMap: HashMap<String, Any>): HashMap<String, Any> {
        return api.addCategory(requestInputMap)
    }



}