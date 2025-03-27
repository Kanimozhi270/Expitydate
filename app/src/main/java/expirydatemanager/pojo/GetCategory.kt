package expirydatemanager.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GetCategory {
    @SerializedName("Category")
    @Expose
    private val category: List<CategoryList>? = null

    class CategoryList {

        @SerializedName("id")
        @Expose
        private val id: Int? = null

        @SerializedName("category")
        @Expose
        private val category: String? = null

        @SerializedName("item_type")
        @Expose
        private val itemType: Int? = null
    }
}