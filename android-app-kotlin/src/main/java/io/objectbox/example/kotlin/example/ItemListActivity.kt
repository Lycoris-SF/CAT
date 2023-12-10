package io.objectbox.example.kotlin.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import io.objectbox.Box
import io.objectbox.example.kotlin.App
import io.objectbox.example.kotlin.DbErrorActivity
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemsAdapter
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.SearchPageActivity
import io.objectbox.example.kotlin.databinding.ActivityItemListBinding
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemListActivity : AppCompatActivity() {

    private lateinit var itemsBox: Box<Item>
    private lateinit var itemsAdapter: ItemsAdapter
    private lateinit var binding: ActivityItemListBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If failed to build BoxStore, notify user.
        if (ObjectBoxSC.dbExceptionMessage != null) {
            startActivity(Intent(this, DbErrorActivity::class.java))
            finish()
            return
        }

        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViews()

        // Using ObjectBoxSC Kotlin extension functions (https://docs.objectbox.io/kotlin-support)
        itemsBox = ObjectBoxSC.boxStore.boxFor()

        // See [ObjectBoxSC] on how the Query for this is built.
        // Any changes to an Object in the Item Box will trigger delivery of latest results.
        ObjectBoxSC.itemsLiveData.observe(this, Observer {
            itemsAdapter.setItems(it)
        })
    }

    private fun setUpViews() {
        itemsAdapter = ItemsAdapter()

        binding.listViewItems.apply {
            adapter = itemsAdapter
            onItemClickListener = itemEditListener
            onItemLongClickListener = itemRemoveListener
        }

        binding.searchBar.setOnClickListener {
            startActivity(Intent(this, SearchPageActivity::class.java))
        }

        binding.buttonAddItem.setOnClickListener {
            startActivity(Intent(this, EditItemActivity::class.java))
        }
    }

    private val itemRemoveListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
        itemsAdapter.getItem(position)?.also {
            lifecycleScope.launch(Dispatchers.IO) {
                // Pass the Item Object to remove it.
                val removed = itemsBox.remove(it)
                // Can also remove by passing an Object ID:
                // itemsBox.remove(it.id)

                if (removed) Log.d(App.TAG, "Deleted item, ID: " + it.id)
            }
        }
        true
    }

    private val itemEditListener = OnItemClickListener { _, _, position, _ ->
        itemsAdapter.getItem(position)?.also {
            startActivity(EditItemActivity.intent(this, it.id))
        }
    }

}