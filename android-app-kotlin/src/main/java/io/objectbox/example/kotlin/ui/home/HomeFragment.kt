package io.objectbox.example.kotlin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.objectbox.Box
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemViewManager
import io.objectbox.example.kotlin.ItemsAdapter
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.SearchPageActivity
import io.objectbox.example.kotlin.databinding.FragmentHomeBinding
import io.objectbox.kotlin.boxFor


class HomeFragment : Fragment() {

    private lateinit var itemsBox: Box<Item>
    private lateinit var itemsAdapter: ItemsAdapter
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*val galleryViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)*/

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        }

        binding.searchBar.setOnClickListener {
            startActivity(SearchPageActivity.intent(requireContext(), "EXTRA_SEARCH_NAME",
                SearchPageActivity.SearchMode.ITEM_VIEW))
        }
    }

    private val itemEditListener = OnItemClickListener { _, _, position, _ ->
        itemsAdapter.getItem(position)?.also {
            startActivity(ItemViewManager.intent(requireContext(), it.id))
        }
    }
}