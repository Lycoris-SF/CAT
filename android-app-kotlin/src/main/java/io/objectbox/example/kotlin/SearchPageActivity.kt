package io.objectbox.example.kotlin

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import java.util.Date
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import io.objectbox.example.kotlin.databinding.ActivitySearchBinding
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.launch

class SearchPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private var mode: SearchMode = SearchMode.ITEM_VIEW

    enum class SearchMode {
        EDIT_ITEM,
        ITEM_VIEW,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getSerializableExtra(EXTRA_MODE) as? SearchMode ?: SearchMode.ITEM_VIEW

        val autoCompleteTextView = binding.autoCompleteTextView
        val historyListView = binding.historyListView

        val historyItems = ObjectBoxSC.boxStore.boxFor(SearchHistory::class.java).query().build().find()
        val historyAdapter = ArrayAdapter(this, R.layout.simple_list_item_1, historyItems.map { it.query })
        historyListView.adapter = historyAdapter

        historyListView.setOnItemClickListener { _, _, position, _ ->
            val selectedQuery = historyItems[position].query
            autoCompleteTextView.setText(selectedQuery)
        }

        val suggestions = arrayOf("Suggest Query 1", "Suggest Query 2", "Suggest Query 3")
        val autoCompleteAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, suggestions)

        autoCompleteTextView.setAdapter(autoCompleteAdapter)

        binding.searchButton.setOnClickListener {
            // Prevent multiple clicks.
            binding.searchButton.isEnabled = false

            var name = binding.autoCompleteTextView.text?.toString()
            if (name.isNullOrBlank()) {
                binding.autoCompleteTextView.error = "Input must not be empty"
                binding.searchButton.isEnabled = true
                return@setOnClickListener
            }

            lifecycleScope.launch() {
                searchfor(name)
                //write into history
                val existingItem = ObjectBoxSC.boxStore.boxFor(SearchHistory::class.java)
                    .query().equal(SearchHistory_.query, name, QueryBuilder.StringOrder.CASE_INSENSITIVE).build().findFirst()
                if (existingItem == null) {
                    val searchHistoryItem = SearchHistory(query = name, timestamp = Date())
                    ObjectBoxSC.boxStore.boxFor(SearchHistory::class.java).put(searchHistoryItem)
                }
                finish()
            }
        }
        binding.deleteButton.setOnClickListener{
            val historyBox = ObjectBoxSC.boxStore.boxFor(SearchHistory::class.java)
            historyBox.removeAll()
            historyListView.adapter = null
            autoCompleteTextView.setAdapter(null)
            autoCompleteTextView.text = null
        }

        autoCompleteTextView.addTextChangedListener {
            editable ->
            val query = editable.toString()
            updateAutoCompleteSuggestions(query)
        }
    }

    private fun updateAutoCompleteSuggestions(query: String) {
        val lowerCaseQuery = query.lowercase()
        val searchQuery = ObjectBoxSC.boxStore.boxFor(Item::class.java)
            .query()
            .startsWith(Item_.title, lowerCaseQuery, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build()

        val suggestions = searchQuery.find().map { it.title?.lowercase() ?: "" }
            .filter { it.startsWith(lowerCaseQuery) }

        searchQuery.close()

        val autoCompleteAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            suggestions
        )
        val autoCompleteTextView = binding.autoCompleteTextView
        autoCompleteTextView.setAdapter(autoCompleteAdapter)
    }
    private fun searchfor(name: String) {
        val lowerCaseName = name.lowercase()

        val searchQuery = ObjectBoxSC.boxStore.boxFor(Item::class.java)
            .query().equal(Item_.title, lowerCaseName, QueryBuilder.StringOrder.CASE_INSENSITIVE).build()

        val result = searchQuery.find()
        searchQuery.close()

        if (result.isEmpty()) {
            startActivity(ItemViewManager.intent(this))
        } else {
            //old
            /*when (mode) {
                SearchMode.EDIT_ITEM -> startActivity(EditItemActivity.intent(this, result.first().id))
                SearchMode.ITEM_VIEW -> startActivity(ItemViewActivity.intent(this, result.first().id))
                // Add more cases as needed
            }*/
            when (mode) {
                SearchMode.EDIT_ITEM -> startActivity(ItemEditManager.intent(this, result.first().id))
                SearchMode.ITEM_VIEW -> startActivity(ItemViewManager.intent(this, result.first().id))
            }
        }
    }

    companion object {
        private const val EXTRA_SEARCH_NAME = "EXTRA_SEARCH_NAME"
        private const val EXTRA_MODE = "EXTRA_MODE"

        fun intent(context: Context, name: String, mode: SearchMode): Intent {
            return Intent(context, SearchPageActivity::class.java).apply {
                putExtra(EXTRA_SEARCH_NAME, name)
                putExtra(EXTRA_MODE, mode)
            }
        }
    }
}