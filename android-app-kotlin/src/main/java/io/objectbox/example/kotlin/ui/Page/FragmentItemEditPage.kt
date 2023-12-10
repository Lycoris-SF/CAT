package io.objectbox.example.kotlin.ui.Page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemLegal
import io.objectbox.example.kotlin.ItemType
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.QueryActivity
import io.objectbox.example.kotlin.R
import io.objectbox.example.kotlin.databinding.FragmentEditItemBinding
import io.objectbox.example.kotlin.ui.Adapter.GridBuyAdapter
import io.objectbox.example.kotlin.ui.Adapter.GridItemDecoration
import io.objectbox.example.kotlin.ui.Adapter.GridSellAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class FragmentItemEditPage : Fragment(), DataListener{
    private lateinit var binding: FragmentEditItemBinding
    private lateinit var itemTypeSpinner: Spinner
    private lateinit var itemLegalSpinner: Spinner
    private lateinit var adapter1: GridBuyAdapter
    private lateinit var adapter2: GridSellAdapter
    private lateinit var selectedItemType: ItemType
    private lateinit var selectedLegal: ItemLegal
    private var itemId: Long = -1
    private lateinit var existingItem: Item
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        //back home
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        itemId = arguments?.getLong(EXTRA_ITEM_ID, -1) ?: -1
        itemTypeSpinner = binding.itemType
        val itemTypes = arrayOf("Item", "Commodity", "Location")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemTypeSpinner.adapter = adapter

        itemLegalSpinner = binding.itemLegal
        val itemLegal = arrayOf("Neutral", "Legal", "Contraband")
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemLegal)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemLegalSpinner.adapter = adapter2

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // If given the Object ID, get an existing Item Object to edit.
                if (itemId != -1L){
                    val idItem = withContext(Dispatchers.IO) {
                        ObjectBoxSC.boxStore.boxFor(Item::class.java).get(itemId)
                    }
                    existingItem = idItem
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
                                ItemType.ITEM} // Default to Item
                        }
                        viewModel.type_share.value = selectedItemType.toString()
                        //setUpViews(existingItem)
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                        viewModel.type_share.value = ItemType.ITEM.toString()
                    }
                }
                itemLegalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedLegal = when (position) {
                            1 -> ItemLegal.LEGAL
                            2 -> ItemLegal.CONTRABAND
                            else -> {
                                ItemLegal.NEUTRAL} // Default to Item
                        }
                        viewModel.legal_share.value = selectedLegal.toString()
                    }
                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                        viewModel.legal_share.value = ItemLegal.NEUTRAL.toString()
                    }
                }
                //load
                if (itemId != -1L) {
                    selectedItemType = when (existingItem.type) {
                        "COMMODITY" -> ItemType.COMMODITY
                        "LOCATION" -> ItemType.LOCATION
                        else -> {
                            ItemType.ITEM} // Default to Item
                    }
                    selectedLegal = when (existingItem.legality) {
                        "LEGAL" -> ItemLegal.LEGAL
                        "CONTRABAND" -> ItemLegal.CONTRABAND
                        else -> ItemLegal.NEUTRAL // Default to Item
                    }
                    viewModel.item_id.value = existingItem.id
                    viewModel.type_share.value = existingItem.type
                    viewModel.legal_share.value = existingItem.legality
                    viewModel.title_share.value = existingItem.title
                    viewModel.description_share.value = existingItem.description
                }else{
                    activity?.title = "New item"
                    selectedItemType = ItemType.ITEM
                    selectedLegal = ItemLegal.NEUTRAL
                    viewModel.item_id.value = -1
                    viewModel.title_share.value = ""
                    viewModel.description_share.value = ""
                }
                setUpViews()
            }
        }
    }

    private fun setUpViews() {
        if (itemId != -1L) {
            existingItem?.type?.let {
                val position = selectedItemType.ordinal
                itemTypeSpinner.setSelection(position)
            }
            existingItem?.legality?.let {
                val position = selectedLegal.ordinal
                itemLegalSpinner.setSelection(position)
            }


            adapter1 = GridBuyAdapter(requireContext(), existingItem)
            adapter2 = GridSellAdapter(requireContext(), existingItem)
            adapter1.onItemLongClickListener = {
                0
                binding.buttonSave.performClick()
            }
            adapter2.onItemLongClickListener = {
                0
                binding.buttonSave.performClick()
            }

            setupRecyclerView(adapter1, R.id.recyclerView1)
            setupRecyclerView(adapter2, R.id.recyclerView2)

            binding.editTitleItem.setText(existingItem?.title)
            binding.editDescriptionItem.setText(existingItem?.description)
        }

        binding.editTitleItem.addTextChangedListener{
            text -> viewModel.title_share.value = text.toString()
        }
        binding.editDescriptionItem.addTextChangedListener{
            text -> viewModel.description_share.value = text.toString()
        }

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
                putItem(title,text)
                withContext(Dispatchers.Main) {
                    binding.buttonSave.isEnabled = true
                }
            }
        }
        binding.buttonAdd.setOnClickListener{
            binding.buttonAdd.isEnabled = false

            if (itemId == -1L){
                popToast("Invalid Item, create it first")
                binding.buttonAdd.isEnabled = true
                return@setOnClickListener
            }
            else{
                lifecycleScope.launch() {
                    val title = viewModel.title_share.value.toString()
                    val description = viewModel.description_share.value.toString()
                    putItem(title,description)
                }
                val dialog = FragmentBuySellAdd.newInstance(existingItem.id)
                dialog.setDataListener(this)
                dialog.show(childFragmentManager, "BuySellAdd")
                binding.buttonAdd.isEnabled = true
                return@setOnClickListener
            }
        }
        binding.buttonAssess.setOnClickListener {
            binding.buttonAssess.isEnabled = false

            if (itemId == -1L) {
                popToast("Invalid Item, create it first")
                binding.buttonAssess.isEnabled = true
                return@setOnClickListener
            }
            else if(selectedItemType==ItemType.ITEM){
                popToast("'Item' is not allowed to assess")
                binding.buttonAssess.isEnabled = true
                return@setOnClickListener
            }
            else {
                lifecycleScope.launch {
                    QueryActivity.mergeData(existingItem)
                    val title = viewModel.title_share.value.toString()
                    val description = viewModel.description_share.value.toString()
                    putItem(title, description)

                    withContext(Dispatchers.Main) {
                        adapter1 = GridBuyAdapter(requireContext(), existingItem)
                        adapter2 = GridSellAdapter(requireContext(), existingItem)
                        setupRecyclerView(adapter1, R.id.recyclerView1)
                        setupRecyclerView(adapter2, R.id.recyclerView2)

                        binding.buttonAssess.isEnabled = true
                    }
                }
                return@setOnClickListener
            }
        }
    }

    private fun popToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(activity?.applicationContext, text, duration)
        toast.show()
    }
    private fun setupRecyclerView(adapter: RecyclerView.Adapter<*>, recyclerViewId: Int) {
        // Reference to your RecyclerView
        val recyclerView: RecyclerView = binding.root.findViewById(recyclerViewId)

        // Set the adapter to the RecyclerView
        recyclerView.adapter = adapter

        // Optionally, you can also set a LayoutManager if needed
        recyclerView.layoutManager = GridLayoutManager(requireContext(),1)

        // Optionally, you can add an ItemDecoration for grid lines
        recyclerView.addItemDecoration(GridItemDecoration(requireContext()))
    }

    private suspend fun putItem(
        itemTitle: String,
        itemText: String,
    ) {
        val searchResults = QueryActivity.searchForItemByName(itemTitle)
        if (itemId != -1L) {
            if (searchResults.isNotEmpty() && searchResults.first().id == existingItem.id) {
                withContext(Dispatchers.IO) {
                    existingItem.apply {
                        type = selectedItemType.toString()
                        legality = selectedLegal.toString()
                        title = itemTitle
                        description = itemText
                        date = Date()
                    }
                    ObjectBoxSC.boxStore.boxFor(Item::class.java).put(existingItem)
                }
                popToast("Item Saved")
            } else {
                popToast("Item already exist")
            }
        }
        else {
            if (searchResults.isNotEmpty()){
                popToast("Item already exist")
            }
            else{
                val newItem = Item(type = selectedItemType.toString(), legality = selectedLegal.toString(), title = itemTitle, description = itemText, date = Date())
                withContext(Dispatchers.IO) {
                    itemId = ObjectBoxSC.boxStore.boxFor(Item::class.java).put(newItem)
                }
                popToast("Item Saved")
                requireActivity().finish()
            }
        }
    }

    companion object {
        private const val EXTRA_ITEM_ID: String = "EXTRA_ITEM_ID"

        fun newInstance(itemId: Long? = null): FragmentItemEditPage {
            val fragment = FragmentItemEditPage()
            val args = Bundle()
            if (itemId != null) {
                args.putLong(EXTRA_ITEM_ID, itemId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDataAvailable(data: Item) {
        existingItem = data
        setUpViews()
        lifecycleScope.launch() {
            putItem(existingItem.title.toString(),existingItem.description.toString())
        }
    }
}

interface DataListener {
    fun onDataAvailable(data: Item)
}