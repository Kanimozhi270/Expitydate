package nithra.tamil.calendar.expirydatemanager.fragment

import android.annotation.SuppressLint
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

class ExpiryCategoryFragment : Fragment() {

    private lateinit var db: SQLiteDatabase
    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expiry_cat, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewExpiry)
        db = requireActivity().openOrCreateDatabase("expirydatemanager.db", Context.MODE_PRIVATE, null)
        loadExpiryItems()
        return view
    }

    private fun loadExpiryItems() {
        val cursor: Cursor = db.rawQuery("SELECT * FROM items", null)
        itemList.clear()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("itemname"))
            val expiryDate = cursor.getString(cursor.getColumnIndexOrThrow("itemname"))
            itemList.add(Item(id, name, expiryDate))
        }
        cursor.close()
        val obj_adapter = ItemAdapter_cat(itemList)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = obj_adapter
    }
}
