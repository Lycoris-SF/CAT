package io.objectbox.example.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.objectbox.example.kotlin.ui.Page.FragmentItemViewAssess
import io.objectbox.example.kotlin.ui.Page.FragmentItemViewPage

class ItemViewManager : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_view_manager)

        //back home
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val itemId = intent.getLongExtra("EXTRA_ITEM_ID", -1)

        viewPager = findViewById(R.id.viewPager)

        // 创建一个FragmentStateAdapter的实例
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> FragmentItemViewPage.newInstance(itemId)
                    else -> FragmentItemViewAssess.newInstance(itemId)
                }
            }
        }

        viewPager.adapter = pagerAdapter
    }

    //back to nav/home
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpTo(Intent(this, NavmenuActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_ITEM_ID: String = "EXTRA_ITEM_ID"

        fun intent(context: Context, itemId: Long? = null): Intent {
            return Intent(context, ItemViewManager::class.java).apply {
                if (itemId != null) {
                    putExtra(EXTRA_ITEM_ID, itemId)
                }
            }
        }
    }
}