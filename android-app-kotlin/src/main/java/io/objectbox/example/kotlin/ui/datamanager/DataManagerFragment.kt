package io.objectbox.example.kotlin.ui.datamanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.objectbox.Box
import io.objectbox.example.kotlin.App
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemEditManager
import io.objectbox.example.kotlin.ItemsAdapter
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.SearchPageActivity
import io.objectbox.example.kotlin.databinding.FragmentDatamanagerBinding
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataManagerFragment : Fragment() {

    private lateinit var itemsBox: Box<Item>
    private lateinit var itemsAdapter: ItemsAdapter
    private var _binding: FragmentDatamanagerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(DataManagerViewModel::class.java)

        _binding = FragmentDatamanagerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //fill in content
        setUpViews()

        // Using ObjectBoxSC Kotlin extension functions (https://docs.objectbox.io/kotlin-support)
        itemsBox = ObjectBoxSC.boxStore.boxFor()

        // See [ObjectBoxSC] on how the Query for this is built.
        // Any changes to an Object in the Item Box will trigger delivery of latest results.
        ObjectBoxSC.itemsLiveData.observe(viewLifecycleOwner, Observer {
            itemsAdapter.setItems(it)
        })

        //complete content

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpViews() {
        itemsAdapter = ItemsAdapter()

        binding.listViewItems.apply {
            adapter = itemsAdapter
            onItemClickListener = itemEditListener
            onItemLongClickListener = itemRemoveListener
        }

        /*binding.searchBar.setOnClickListener {
            startActivity(Intent(requireContext(), ManagerSearchActivity::class.java))
        }*/
        binding.searchBar.setOnClickListener {
            startActivity(
                SearchPageActivity.intent(requireContext(), "EXTRA_SEARCH_NAME",
                SearchPageActivity.SearchMode.EDIT_ITEM))
        }

        binding.buttonAddItem.setOnClickListener {
            startActivity(Intent(requireContext(), ItemEditManager::class.java))
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
            startActivity(ItemEditManager.intent(requireContext(), it.id))
        }
    }
}