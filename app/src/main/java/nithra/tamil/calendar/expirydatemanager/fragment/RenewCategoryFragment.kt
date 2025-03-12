package nithra.tamil.calendar.expirydatemanager.fragment

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.Others.Item
import nithra.tamil.calendar.expirydatemanager.Adapter.ItemAdapter_cat
import nithra.tamil.calendar.expirydatemanager.R

class RenewCategoryFragment : Fragment() {

    private lateinit var db: SQLiteDatabase
    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_renew_cat, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewRenew)
        db = requireActivity().openOrCreateDatabase("expirydatemanager.db", Context.MODE_PRIVATE, null)
        loadRenewItems()
        return view
    }

    private fun loadRenewItems() {
        val cursor: Cursor = db.rawQuery("SELECT * FROM items WHERE itemtype = 'renew item'", null)
        itemList.clear()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("itemname"))
            val expiryDate = cursor.getString(cursor.getColumnIndexOrThrow("expiry_date"))
            itemList.add(Item(id, name, expiryDate))
        }
        cursor.close()
        val obj_adapter = ItemAdapter_cat(itemList)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = obj_adapter
    }
}
