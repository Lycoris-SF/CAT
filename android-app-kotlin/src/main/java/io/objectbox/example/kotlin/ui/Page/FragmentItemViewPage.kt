package io.objectbox.example.kotlin.ui.Page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemType
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.R
import io.objectbox.example.kotlin.databinding.FragmentItemViewBinding
import io.objectbox.example.kotlin.ui.Adapter.GridBuyReader
import io.objectbox.example.kotlin.ui.Adapter.GridItemDecoration
import io.objectbox.example.kotlin.ui.Adapter.GridSellReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentItemViewPage : Fragment() {
    private lateinit var binding: FragmentItemViewBinding
    private lateinit var adapter1: GridBuyReader
    private lateinit var adapter2: GridSellReader
    private lateinit var selectedItemType: ItemType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //back home
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val itemId = arguments?.getLong(EXTRA_ITEM_ID, -1) ?: -1

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
                    activity?.title = selectedItemType.toString()
                }
                else{
                    activity?.title = "404 NOT FOUND"
                }
                //edit title of page
                setUpViews(existingItem,itemId)
            }
        }
    }

    //back to last
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //requireActivity().onBackPressed()
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
            adapter1 = GridBuyReader(requireContext(), existingItem)
            adapter2 = GridSellReader(requireContext(), existingItem)

            setupRecyclerView(adapter1, R.id.recyclerView1)
            setupRecyclerView(adapter2, R.id.recyclerView2)

            binding.titleReadItem.text = existingItem?.title
            binding.DescriptionReadItem.text = existingItem?.description
        }
    }

    private fun setupRecyclerView(adapter: RecyclerView.Adapter<*>, recyclerViewId: Int) {
        // Reference to your RecyclerView
        val recyclerView: RecyclerView = binding.root.findViewById(recyclerViewId)

        // Set the adapter to the RecyclerView
        recyclerView.adapter = adapter

        // Optionally, you can also set a LayoutManager if needed
        recyclerView.layoutManager = GridLayoutManager(context,1)

        // Optionally, you can add an ItemDecoration for grid lines
        recyclerView.addItemDecoration(GridItemDecoration(requireContext()))
    }

    companion object {
        private const val EXTRA_ITEM_ID: String = "EXTRA_ITEM_ID"

        fun newInstance(itemId: Long? = null): FragmentItemViewPage {
            val fragment = FragmentItemViewPage()
            val args = Bundle()
            if (itemId != null) {
                args.putLong(EXTRA_ITEM_ID, itemId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
