package nithra.tamil.calendar.expirydatemanager.fragment

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.Others.Item
import nithra.tamil.calendar.expirydatemanager.Adapter.ItemAdapter_home
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryDateViewModel

class ExpiryFragment_home : Fragment() {


    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expiry_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewExpiry)

        return view
    }

}
