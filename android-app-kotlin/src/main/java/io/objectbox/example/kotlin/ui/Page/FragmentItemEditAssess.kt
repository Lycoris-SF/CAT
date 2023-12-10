package io.objectbox.example.kotlin.ui.Page

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.AssessmentActivity
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemLegal
import io.objectbox.example.kotlin.ItemType
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.QueryActivity
import io.objectbox.example.kotlin.R
import io.objectbox.example.kotlin.databinding.FragmentCommentItemBinding
import io.objectbox.example.kotlin.ui.Adapter.GridItemDecoration
import io.objectbox.example.kotlin.ui.Adapter.ExtraAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Date


class FragmentItemEditAssess : Fragment() , DataListener2{
    private lateinit var binding: FragmentCommentItemBinding
    private lateinit var adapter1: ExtraAdapter
    private lateinit var adapter2: ExtraAdapter
    private lateinit var selectedItemType: ItemType
    private lateinit var selectedLegal: ItemLegal
    private var itemId: Long = -1
    private lateinit var existingItem: Item
    private lateinit var viewModel: SharedViewModel

    private val REQUEST_CODE = 1
    private var imageBool = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        //back home
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        itemId = arguments?.getLong(EXTRA_ITEM_ID, -1) ?: -1

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // If given the Object ID, get an existing Item Object to edit.
                if (itemId != -1L){
                    val idItem = withContext(Dispatchers.IO) {
                        ObjectBoxSC.boxStore.boxFor(Item::class.java).get(itemId)
                    }
                    existingItem = idItem
                }
                //load
                if (itemId != -1L) {
                    selectedItemType = ItemType.valueOf(existingItem.type ?: "ITEM")
                    selectedLegal = ItemLegal.valueOf(existingItem.legality ?: "NEUTRAL")
                    activity?.title = selectedItemType.toString()
                }
                else{
                    activity?.title = "New item"
                    selectedItemType = ItemType.valueOf(viewModel.type_share.value ?: "ITEM")
                    selectedLegal = ItemLegal.valueOf(viewModel.legal_share.value ?: "NEUTRAL")
                }
                //edit title of page
                setUpViews()
            }
        }
    }

    private fun setUpViews() {
        if(itemId==-1L){
            binding.titleReadItem.text = "New item"
            binding.DescriptionReadItem.text = "Creating new item in DataManager"
        }
        else{
            binding.titleReadItem.setTextColor(Color.BLACK)
            binding.titleReadItem.text = existingItem?.title
            binding.DescriptionReadItem.setTextColor(Color.BLACK)
            binding.DescriptionReadItem.text = existingItem?.description

            paint_pic(existingItem)

            adapter1 = ExtraAdapter(requireContext(), existingItem,true, false)
            adapter2 = ExtraAdapter(requireContext(), existingItem,false,false)
            setupRecyclerView(adapter1, R.id.recyclerView1)
            setupRecyclerView(adapter2, R.id.recyclerView2)
        }

        viewModel.title_share.observe(viewLifecycleOwner, Observer {
            text -> binding.titleReadItem.text = text
            binding.titleReadItem.setTextColor(Color.BLACK)
            if (text.isNullOrBlank()) {
                binding.titleReadItem.setTextColor(Color.RED)
                binding.titleReadItem.text = "Empty!"
            }
        })
        viewModel.description_share.observe(viewLifecycleOwner, Observer {
            text -> binding.DescriptionReadItem.text = text
            binding.DescriptionReadItem.setTextColor(Color.BLACK)
            if (text.isNullOrBlank()) {
                binding.DescriptionReadItem.setTextColor(Color.RED)
                binding.DescriptionReadItem.text = "Item must not be empty"
            }
        })
        viewModel.type_share.observe(viewLifecycleOwner, Observer {
            text -> selectedItemType = when (text) {
                "COMMODITY" -> ItemType.COMMODITY
                "LOCATION" -> ItemType.LOCATION
                else -> ItemType.ITEM // Default to Item
            }
        })
        viewModel.legal_share.observe(viewLifecycleOwner, Observer {
            text -> selectedLegal= when (text) {
                "LEGAL" -> ItemLegal.LEGAL
                "CONTRABAND" -> ItemLegal.CONTRABAND
                else -> ItemLegal.NEUTRAL // Default to Item
            }
        })

        binding.buttonSave.setOnClickListener {
            // Prevent multiple clicks.
            binding.buttonSave.isEnabled = false

            val title = viewModel.title_share.value
            val text = viewModel.description_share.value

            if(title.isNullOrBlank()||text.isNullOrBlank()){
                binding.titleReadItem.setTextColor(Color.RED)
                binding.titleReadItem.text = "Empty!"
                binding.DescriptionReadItem.setTextColor(Color.RED)
                binding.DescriptionReadItem.text = "Item must not be empty"
                popToast("Invalid Item, create it first")
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
                val dialog = FragmentCommentAdd.newInstance(existingItem.id)
                dialog.setDataListener(this)
                dialog.show(childFragmentManager, "Comment")
                binding.buttonAdd.isEnabled = true
                return@setOnClickListener
            }
        }
        binding.buttonAssess.setOnClickListener {
            binding.buttonAssess.isEnabled = false
            if (itemId == -1L){
                popToast("Invalid Item, create it first")
                binding.buttonAssess.isEnabled = true
                return@setOnClickListener
            }
            else{
                AssessDemo()
                binding.buttonAssess.isEnabled = true
                return@setOnClickListener
            }
        }
        binding.imageView1.setOnClickListener{
            if (itemId != -1L) {
                imageBool = true
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
        binding.imageView1.setOnLongClickListener{
            if (itemId != -1L) {
                if (existingItem != null) {
                    existingItem.imageData1 = Item.createDefaultImage()
                }
                val imageData: ByteArray? = existingItem?.imageData1
                val bitmap = imageData?.let { BitmapFactory.decodeByteArray(imageData, 0, it.size) }
                binding.imageView1.setImageBitmap(bitmap)
            }
            true
        }
        binding.imageView2.setOnClickListener{
            if (itemId != -1L) {
                imageBool = false
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
        binding.imageView2.setOnLongClickListener{
            if (itemId != -1L) {
                if (existingItem != null) {
                    existingItem.imageData2 = Item.createDefaultImage()
                }
                val imageData: ByteArray? = existingItem?.imageData2
                val bitmap = imageData?.let { BitmapFactory.decodeByteArray(imageData, 0, it.size) }
                binding.imageView2.setImageBitmap(bitmap)
            }
            true
        }
    }
    private fun popToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(activity?.applicationContext, text, duration)
        toast.show()
    }
    private fun UpdateAssess(existingItem: Item?){
        adapter1 = ExtraAdapter(requireContext(), existingItem,true, true)
        setupRecyclerView(adapter1, R.id.recyclerView1)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Load Image
            val imageUri = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
            if(imageBool)binding.imageView1.setImageBitmap(bitmap)
            else binding.imageView2.setImageBitmap(bitmap)
            // Save Image
            saveBitmapToDatabase(bitmap)
        }
    }
    private fun saveBitmapToDatabase(bitmap: Bitmap) {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val maxWidth = 485
        val maxHeight = 500

        var newWidth: Int
        var newHeight: Int

        if (originalWidth.toFloat() / originalHeight > maxWidth.toFloat() / maxHeight) {
            newWidth = maxWidth
            newHeight = (originalHeight * (maxWidth.toFloat() / originalWidth)).toInt()
            if (newHeight > maxHeight) {
                newWidth = (originalWidth * (maxHeight.toFloat() / originalHeight)).toInt()
                newHeight = maxHeight
            }
        } else {
            newHeight = maxHeight
            newWidth = (originalWidth * (maxHeight.toFloat() / originalHeight)).toInt()
            if (newWidth > maxWidth) {
                newHeight = (originalHeight * (maxWidth.toFloat() / originalWidth)).toInt()
                newWidth = maxWidth
            }
        }

        val compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        val stream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // Save to the database
        if(imageBool){
            if(existingItem.imageData1==null){
                existingItem.imageData1 = Item.createDefaultImage()
            }
            else{
                existingItem.imageData1 = byteArray
            }
        }
        else{
            if(existingItem.imageData2==null){
                existingItem.imageData2 = Item.createDefaultImage()
            }
            else{
                existingItem.imageData2 = byteArray
            }
        }

        lifecycleScope.launch(){
            putItem(existingItem.title.toString(),existingItem.description.toString())
        }
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

    private fun AssessDemo(){
        lifecycleScope.launch() {
            val title = viewModel.title_share.value.toString()
            val description = viewModel.description_share.value.toString()
            putItem(title,description)
            withContext(Dispatchers.Main) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val result = AssessmentActivity().MakeAssessment(existingItem)
                        withContext(Dispatchers.Main) {
                            if(result==0){
                                popToast("Invalid Item, assess COMMODITY/LOCATION only")
                            }
                            else{
                                UpdateAssess(existingItem)
                            }
                        }
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_ITEM_ID: String = "EXTRA_ITEM_ID"

        fun newInstance(itemId: Long? = null): FragmentItemEditAssess {
            val fragment = FragmentItemEditAssess()
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
        lifecycleScope.launch(){
            putItem(existingItem.title.toString(),existingItem.description.toString())
        }
    }
}
interface DataListener2 {
    fun onDataAvailable(data: Item)
}
