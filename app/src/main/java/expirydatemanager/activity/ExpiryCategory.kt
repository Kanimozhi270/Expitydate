package expirydatemanager.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import expirydatemanager.fragment.ExpiryCategoryFragment
import expirydatemanager.fragment.RenewCategoryFragment
import nithra.tamil.calendar.expirydatemanager.databinding.SmExpiryActivityCategoryBinding

class ExpiryCategory : AppCompatActivity() {

    private lateinit var binding: SmExpiryActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmExpiryActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.title = HtmlCompat.fromHtml(
            "Category", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setSupportActionBar(binding.appBar)
        supportActionBar!!.title = HtmlCompat.fromHtml(
            "Category", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Set up ViewPager with TabLayout
        setupViewPager()

        // Optionally hide the content layout, if needed
        showTabs_cat()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()  // Go back when home button is pressed
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun setupViewPager() {
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

    private fun showTabs_cat() {
        binding.contentLayout.visibility = View.GONE
        binding.tabLayout.visibility = View.VISIBLE
        binding.viewPager.visibility = View.VISIBLE
    }
}
