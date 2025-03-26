package expirydatemanager.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ExpiryGetLIst {
    @SerializedName("status")
    @Expose
    private val status: String? = null

    @SerializedName("list")
    @Expose
    private val list: List<Expiry_getlist>? = null

    class Expiry_getlist {
        @SerializedName("id")
        @Expose
        private val id: Int? = null

        @SerializedName("category_id")
        @Expose
        private val categoryId: Int? = null

        @SerializedName("custom_date")
        @Expose
        private val customDate: String? = null

        @SerializedName("item_id")
        @Expose
        private val itemId: Int? = null

        @SerializedName("action_date")
        @Expose
        private val actionDate: String? = null

        @SerializedName("reminder_type")
        @Expose
        private val reminderType: String? = null

        @SerializedName("notify_time")
        @Expose
        private val notifyTime: String? = null

        @SerializedName("item_name")
        @Expose
        private val itemName: String? = null

        @SerializedName("remark")
        @Expose
        private val remark: String? = null
    }

}