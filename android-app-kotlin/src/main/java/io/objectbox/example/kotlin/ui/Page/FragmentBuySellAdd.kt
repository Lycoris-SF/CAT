package io.objectbox.example.kotlin.ui.Page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.QueryActivity
import io.objectbox.example.kotlin.databinding.FragmentBuyselladdBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class FragmentBuySellAdd : DialogFragment(){
    private lateinit var binding: FragmentBuyselladdBinding
    private lateinit var dataListener: DataListener

    fun setDataListener(listener: DataListener) {
        dataListener = listener
    }

    companion object {
        private const val ARG_ITEM_ID = "item_id"

        fun newInstance(itemId: Long): FragmentBuySellAdd {
            val fragment = FragmentBuySellAdd()
            val args = Bundle()
            args.putLong(ARG_ITEM_ID, itemId)
            fragment.arguments = args
            return fragment
        }
    }

    private fun sendDataToFragmentItemEditPage() {
        val itemId = arguments?.getLong(ARG_ITEM_ID, -1L) ?: -1L
        val data = ObjectBoxSC.boxStore.boxFor(Item::class.java)[itemId]

        when(binding.itemType.selectedItem.toString()){
            "BUY" ->{
                val newItem = binding.editText1.text.toString()
                data.buyList?.add(newItem)
                val newPrice = binding.editText2.text.toString()
                data.itemBuyPrice?.add(newPrice)
                val newStorage = binding.editText3.text.toString()
                data.itemBuyStorage?.add(newStorage)
                QueryActivity.tempAddNewItem(data,newItem,newPrice,newStorage,true)
            }
            else -> {
                val newItem = binding.editText1.text.toString()
                data.sellList?.add(newItem)
                val newPrice = binding.editText2.text.toString()
                data.itemSellPrice?.add(newPrice)
                val newStorage = binding.editText3.text.toString()
                data.itemSellStorage?.add(newStorage)
                QueryActivity.tempAddNewItem(data,newItem,newPrice,newStorage,false)
            }
        }

        dataListener.onDataAvailable(data)
        dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBuyselladdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        val displayMetrics = resources.displayMetrics
        params?.width = (displayMetrics.widthPixels * 0.9).toInt() // 设置宽度为屏幕宽度的 80%
        params?.height = LinearLayout.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemTypeSpinner = binding.itemType
        val itemTypes = arrayOf("BUY", "SELL")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemTypeSpinner.adapter = adapter

        binding.buysellSave.setOnClickListener {
            binding.buysellSave.isEnabled = false

            if(binding.editText1.text.isNullOrBlank()){
                binding.editText1.error = "Name should not be null"
                binding.buysellSave.isEnabled = true
                return@setOnClickListener
            }
            else{
                sendDataToFragmentItemEditPage()
            }
        }
    }
}