package expirydatemanager.retrofit

import com.google.gson.JsonObject
import expirydatemanager.pojo.ItemList
import retrofit2.Call
import retrofit2.http.*

interface ExpiryRetrofitInterface {

    // Add List
    @FormUrlEncoded
    @POST("expiryData")
    fun addList(
        @FieldMap params: HashMap<String, Any>
    ): Call<HashMap<String, Any>>

    // Add Item
    @FormUrlEncoded
    @POST("expiryData")
    suspend fun addItem(
        @FieldMap params: HashMap<String, String>
    ): HashMap<String, Any>

    /*@FormUrlEncoded
    @POST("expiryData")
    suspend fun addItem(
        @FieldMap params: HashMap<String, Any>
    ): ItemList*/


    // Add Category
    @FormUrlEncoded
    @POST("expiryData")
    suspend fun addCategory(
        @FieldMap params: HashMap<String, Any>
    ): HashMap<String, Any>

    // Fetch Item Names
    @FormUrlEncoded
    @POST("expiryData")
    fun getItemNames(
        @Field("action") action: String,
        @Field("user_id") userId: Int
    ): Call<HashMap<String, Any>>

    //fetch list
    /*@FormUrlEncoded
    @POST("expiryData")
    fun getItemlist(
        @Field("action") action: String,
        @Field("user_id") userId: Int,
       // @Field("catgeory_id") cat_id: Int,
        @Field("item_type") item_id: Int,
        @Field("is_days") is_days: Int
    ): Call<ItemList>*/
    @FormUrlEncoded
    @POST("expiryData")
    suspend fun getItemlist(
        @FieldMap params: HashMap<String, Any>
    ): ItemList


    // Fetch Categories
    @FormUrlEncoded
    @POST("expiryData")
    fun getCategories(
        @Field("action") action: String,
        @Field("user_id") userId: Int,
        @Field("item_type") itemType: String
    ): Call<HashMap<String, Any>>

    //delete list
    @FormUrlEncoded
    @POST("expiryData")
    fun deletelist(
        @Field("action") action: String,
        @Field("user_id") userId: Int,
        @Field("list_id") itemType: Int
    ): Call<HashMap<String, Any>>

    //delete category
    @FormUrlEncoded
    @POST("expiryData")
    fun deletecat(
        @FieldMap params: HashMap<String, Any>
    ): Call<HashMap<String, Any>>

    //delete item
    @FormUrlEncoded
    @POST("expiryData")
    fun deleteitem(
        @FieldMap params: HashMap<String, Any>
    ): Call<HashMap<String, Any>>


}



