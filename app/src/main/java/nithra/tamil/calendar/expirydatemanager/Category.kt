package nithra.tamil.calendar.expirydatemanager

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nithra.tamil.calendar.expirydatemanager.databinding.ActivityCategoryBinding

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

        // Set up ViewPager with TabLayout
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    ExpiryCategoryFragment()
                } else {
                    RenewCategoryFragment()
                }
            }
        }

        // Attach TabLayout to ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Expiry Item" else "Renew Item"
        }.attach()
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
