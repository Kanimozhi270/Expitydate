package expirydatemanager.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class GetItem {
    @SerializedName("Items")
    @Expose
    private val items: List<GetItemlist>? = null

    class GetItemlist {
        @SerializedName("id")
        @Expose
        private val id: Int? = null

        @SerializedName("item_name")
        @Expose
        private val itemName: String? = null

    }
}