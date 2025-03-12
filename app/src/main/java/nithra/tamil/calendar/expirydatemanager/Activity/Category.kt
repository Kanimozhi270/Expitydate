package nithra.tamil.calendar.expirydatemanager.Activity

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityCategoryBinding
import nithra.tamil.calendar.expirydatemanager.fragment.ExpiryCategoryFragment
import nithra.tamil.calendar.expirydatemanager.fragment.ExpiryFragment_home
import nithra.tamil.calendar.expirydatemanager.fragment.RenewCategoryFragment
import nithra.tamil.calendar.expirydatemanager.fragment.RenewFragment_home

class Category : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.title = HtmlCompat.fromHtml(
            "<b>Category", HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        setSupportActionBar(binding.appBar)
        supportActionBar!!.title = HtmlCompat.fromHtml(
            "<b>Category", HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Initialize database
        db = openOrCreateDatabase("expirydatemanager.db", MODE_PRIVATE, null)

        // Check if the items table has data
        if (hasItemsData_cat()) {
            print("chexkk data enter")
            showTabs_cat()
        } else {
            print("chexkk data enter else")
            showContentLayout_cat()
        }

        // Set up ViewPager with TabLayout
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) ExpiryCategoryFragment() else RenewCategoryFragment()
            }
            override fun getItemCount() = 2
        }

        // Attach TabLayout to ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Expiry Item" else "Renew Item"
        }.attach()
    }

    private fun hasItemsData_cat(): Boolean {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM categorys", null)
        var hasData = false
        if (cursor.moveToFirst()) {
            hasData = cursor.getInt(0) > 0
        }
        cursor.close()
        return hasData
    }

    private fun showTabs_cat() {
        binding.contentLayout.visibility = View.GONE
        binding.tabLayout.visibility = View.VISIBLE
        binding.viewPager.visibility = View.VISIBLE

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) ExpiryFragment_home() else RenewFragment_home()
            }
            override fun getItemCount() = 2
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Expiry Item" else "Renew Item"
        }.attach()
    }

    private fun showContentLayout_cat() {
        binding.contentLayout.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
    }

    fun fetchCategories(itemType: String): List<String> {
        val categories = mutableListOf<String>()
        val cursor: Cursor = db.rawQuery("SELECT categoryname FROM categorys WHERE itemtype = ?", arrayOf(itemType))
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }
}
