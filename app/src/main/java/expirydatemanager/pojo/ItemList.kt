package expirydatemanager.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ItemList{
    @SerializedName("status")
    @Expose
     val status: String? = null

    @SerializedName("list")
    @Expose
     val list: List<GetList>? = null

    class GetList {
        @SerializedName("id")
        @Expose
         val id: Int? = null

        @SerializedName("category_id")
        @Expose
         val categoryId: Int? = null

        @SerializedName("category_name")
        @Expose
        val categoryName: String? = null

        @SerializedName("custom_date")
        @Expose
         val customDate: Any? = null

        @SerializedName("item_id")
        @Expose
         val itemId: Int? = null

        @SerializedName("action_date")
        @Expose
         val actionDate: String? = null

        @SerializedName("reminder_type")
        @Expose
         val reminderType: String? = null

        @SerializedName("notify_time")
        @Expose
         val notifyTime: String? = null

        @SerializedName("item_name")
        @Expose
         val itemName: String? = null

        @SerializedName("remark")
        @Expose
         val remark: String? = null
    }


}

