package io.objectbox.example.kotlin.ui.Page

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.ui.Adapter.ExtraReader
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemEditManager
import io.objectbox.example.kotlin.ItemType
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.R
import io.objectbox.example.kotlin.databinding.FragmentAssessItemBinding
import io.objectbox.example.kotlin.ui.Adapter.GridItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentItemViewAssess : Fragment() {
    private lateinit var binding: FragmentAssessItemBinding
    private lateinit var adapter1: ExtraReader
    private lateinit var adapter2: ExtraReader
    private lateinit var selectedItemType: ItemType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessItemBinding.inflate(inflater, container, false)
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
                            ItemType.ITEM} // Default to Item
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

    private fun setUpViews(existingItem: Item?,itemId: Long) {
        if(itemId==-1L){
            binding.titleReadItem.text = "Item not found"
            binding.DescriptionReadItem.text = "You can create it in the DataManager"
        }
        else{
            binding.titleReadItem.text = existingItem?.title
            binding.DescriptionReadItem.text = existingItem?.description

            paint_pic(existingItem)

            adapter1 = ExtraReader(requireContext(), existingItem,true)
            adapter2 = ExtraReader(requireContext(), existingItem,false)
            setupRecyclerView(adapter1, R.id.recyclerView1)
            setupRecyclerView(adapter2, R.id.recyclerView2)
        }

        binding.buttonEdit.setOnClickListener {
            // Prevent multiple clicks.
            binding.buttonEdit.isEnabled = false

            lifecycleScope.launch() {
                activity?.title = "New item"
                binding.titleReadItem.text = "New item"
                binding.DescriptionReadItem.text = "Creating new item in DataManager"
                if(itemId==-1L) startActivity(ItemEditManager.intent(requireContext()))
                else startActivity(ItemEditManager.intent(requireContext(),itemId))
                activity?.finish()
            }
        }
    }

    private fun paint_pic(existingItem: Item?){
        val imageData: ByteArray? = existingItem?.imageData1
        val bitmap = imageData?.let { BitmapFactory.decodeByteArray(imageData, 0, it.size) }
        val imageView: ImageView = binding.imageView1
        imageView.setImageBitmap(bitmap)

        val imageData2: ByteArray? = existingItem?.imageData2
        val bitmap2 = imageData2?.let { BitmapFactory.decodeByteArray(imageData2, 0, it.size) }
        val imageView2: ImageView = binding.imageView2
        imageView2.setImageBitmap(bitmap2)
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

        fun newInstance(itemId: Long? = null): FragmentItemViewAssess {
            val fragment = FragmentItemViewAssess()
            val args = Bundle()
            if (itemId != null) {
                args.putLong(EXTRA_ITEM_ID, itemId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
