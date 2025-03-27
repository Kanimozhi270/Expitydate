package expirydatemanager.retrofit

import expirydatemanager.pojo.ItemList

class ExpiryRepository(private val api: ExpiryRetrofitInterface) {

    suspend fun getItemlist(requestInputMap: HashMap<String, Any>): ItemList {
        return api.getItemlist(requestInputMap)
    }

}