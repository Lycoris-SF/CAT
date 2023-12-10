package io.objectbox.example.kotlin.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemType
import io.objectbox.example.kotlin.NavmenuActivity
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.R
import io.objectbox.example.kotlin.databinding.FragmentItemViewBinding
import io.objectbox.example.kotlin.ui.Adapter.GridBuyReader
import io.objectbox.example.kotlin.ui.Adapter.GridItemDecoration
import io.objectbox.example.kotlin.ui.Adapter.GridSellReader

class ItemViewActivity : AppCompatActivity() {
    private lateinit var binding: FragmentItemViewBinding
    private lateinit var adapter1: GridBuyReader
    private lateinit var adapter2: GridSellReader
    private lateinit var selectedItemType: ItemType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //back home
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = FragmentItemViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // If given the Object ID, get an existing Item Object to edit.
                val existingItem = withContext(Dispatchers.IO) {
                    if (itemId != -1L) {
                        ObjectBoxSC.boxStore.boxFor(Item::class.java)[itemId]
                    } else {
                        null
                    }
                }
                //load
                if (existingItem != null) {
                    selectedItemType = when (existingItem.type) {
                        "COMMODITY" -> ItemType.COMMODITY
                        "LOCATION" -> ItemType.LOCATION
                        else -> {
                            ItemType.ITEM
                        } // Default to Item
                    }
                    title = selectedItemType.toString()
                }
                else{
                    title = "404 NOT FOUND"
                }
                //edit title of page
                setUpViews(existingItem,itemId)
            }
        }
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

    private fun setUpViews(existingItem: Item?, itemId: Long) {
        if(itemId==-1L){
            binding.titleReadItem.text = "Item not found"
            binding.DescriptionReadItem.text = "You can create it in the DataManager"
        }
        else{
            adapter1 = GridBuyReader(this,existingItem)
            adapter2 = GridSellReader(this,existingItem)

            setupRecyclerView(adapter1, R.id.recyclerView1)
            setupRecyclerView(adapter2, R.id.recyclerView2)

            binding.titleReadItem.text = existingItem?.title
            binding.DescriptionReadItem.text = existingItem?.description
        }
    }

    private fun setupRecyclerView(adapter: RecyclerView.Adapter<*>, recyclerViewId: Int) {
        // Reference to your RecyclerView
        val recyclerView: RecyclerView = findViewById(recyclerViewId)

        // Set the adapter to the RecyclerView
        recyclerView.adapter = adapter

        // Optionally, you can also set a LayoutManager if needed
        recyclerView.layoutManager = GridLayoutManager(this,1)

        // Optionally, you can add an ItemDecoration for grid lines
        recyclerView.addItemDecoration(GridItemDecoration(this))
    }

    companion object {
        private const val EXTRA_ITEM_ID: String = "EXTRA_ITEM_ID"

        fun intent(context: Context, itemId: Long? = null): Intent {
            return Intent(context, ItemViewActivity::class.java).apply {
                if (itemId != null) {
                    putExtra(EXTRA_ITEM_ID, itemId)
                }
            }
        }
    }
}
