package nithra.tamil.calendar.expirydatemanager.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nithra.tamil.calendar.expirydatemanager.others.Utils
import nithra.tamil.calendar.expirydatemanager.Adapter.ExpiryItemAdapter_home
import nithra.tamil.calendar.expirydatemanager.R
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryDateViewModel

class RenewFragment_home : Fragment() {
    private val addItemViewModel: ExpiryDateViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Map<String, Any>>()  // Store raw data as List of Maps
    lateinit var fragmentActivity: AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expiry_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewExpiry)
        val contentLayout = view.findViewById<LinearLayout>(R.id.contentLayout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ExpiryItemAdapter_home(itemList)
        recyclerView.adapter = adapter

      if (Utils.isNetworkAvailable(requireContext())){
          Utils.mProgress(requireActivity(), "ஏற்றுகிறது. காத்திருக்கவும் ", true)
          addItemViewModel.fetchList(userId = 989015,2,4)
      }else{
          contentLayout.visibility=View.VISIBLE
      }


        // Observe itemList LiveData and update itemList with the raw data
        addItemViewModel.itemList.observe(viewLifecycleOwner) { response ->
            // Extract 'list' directly as a List of Map entries
            val list = (response["list"] as? List<Map<String, Any>>) ?: emptyList()


            Utils.mProgress.dismiss()
            itemList.clear()
            itemList.addAll(list)

            // Notify adapter that the data has been updated
            adapter.notifyDataSetChanged()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Now it's safe to call fragmentActivity
        if (context is AppCompatActivity) {
            fragmentActivity = context
        }
    }
}

