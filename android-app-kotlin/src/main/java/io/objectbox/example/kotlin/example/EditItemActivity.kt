package io.objectbox.example.kotlin.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import io.objectbox.example.kotlin.databinding.ActivityAddItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemType
import io.objectbox.example.kotlin.NavmenuActivity
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.R
import io.objectbox.example.kotlin.ui.Adapter.GridBuyAdapter
import io.objectbox.example.kotlin.ui.Adapter.GridItemDecoration
import io.objectbox.example.kotlin.ui.Adapter.GridSellAdapter
import java.util.Date

class EditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding
    private lateinit var itemTypeSpinner: Spinner
    private lateinit var adapter1: GridBuyAdapter
    private lateinit var adapter2: GridSellAdapter
    private lateinit var selectedItemType: ItemType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //back home
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        itemTypeSpinner = findViewById(R.id.itemType)

        val itemTypes = arrayOf("Item", "Commodity", "Location")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemTypeSpinner.adapter = adapter

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
                // Set a listener to handle item selection
                itemTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View?,
                        position: Int,
                        id: Long
                    ) {
                        // Get the selected item type
                        selectedItemType = when (position) {
                            1 -> ItemType.COMMODITY
                            2 -> ItemType.LOCATION
                            else -> {
                                ItemType.ITEM
                            } // Default to Item
                        }
                        //setUpViews(existingItem)
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                        // Do nothing here if needed
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
                }
                setUpViews(existingItem)
            }
        }
    }

    //back to nav/home
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val upIntent = NavmenuActivity.intentWithDestination(this, R.id.data_manager)
                navigateUpTo(upIntent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpViews(existingItem: Item?) {
        existingItem?.type?.let {
            val position = selectedItemType.ordinal
            itemTypeSpinner.setSelection(position)
        }

        adapter1 = GridBuyAdapter(this,existingItem)
        adapter2 = GridSellAdapter(this,existingItem)

        setupRecyclerView(adapter1, R.id.recyclerView1)
        setupRecyclerView(adapter2, R.id.recyclerView2)

        binding.editTitleItem.setText(existingItem?.title)
        binding.editDescriptionItem.setText(existingItem?.description)

        binding.buttonSave.setOnClickListener {
            // Prevent multiple clicks.
            binding.buttonSave.isEnabled = false

            val title = binding.editTitleItem.text?.toString()
            val text = binding.editDescriptionItem.text?.toString()
            if (title.isNullOrBlank()) {
                binding.titleInputItem.error = "Item must not be empty"
                binding.buttonSave.isEnabled = true
                return@setOnClickListener
            }
            if (text.isNullOrBlank()) {
                binding.textInputItem.error = "Item must not be empty"
                binding.buttonSave.isEnabled = true
                return@setOnClickListener
            }

            lifecycleScope.launch() {
                putItem(title,text,existingItem)
                finish()
            }
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

    private suspend fun putItem(
        itemTitle: String,
        itemText: String,
        existingItem: Item?
    ) = withContext(Dispatchers.IO) {
        if (existingItem != null) {
            existingItem?.apply {
                type = selectedItemType.toString()
                title = itemTitle
                description = itemText
            }
            ObjectBoxSC.boxStore.boxFor(Item::class.java).put(existingItem)
        } else {
            val newItem = Item(type = "ITEM", legality = "NEUTRAL", title = itemTitle, description = itemText, date = Date())
            ObjectBoxSC.boxStore.boxFor(Item::class.java).put(newItem)
        }
    }

    companion object {
        private const val EXTRA_ITEM_ID: String = "EXTRA_ITEM_ID"

        fun intent(context: Context, itemId: Long? = null): Intent {
            return Intent(context, EditItemActivity::class.java).apply {
                if (itemId != null) {
                    putExtra(EXTRA_ITEM_ID, itemId)
                }
            }
        }
    }

}