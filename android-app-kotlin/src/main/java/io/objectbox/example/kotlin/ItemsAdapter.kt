package io.objectbox.example.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.DateFormat

class ItemsAdapter : BaseAdapter() {

    private val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)

    private val dataset: MutableList<Item> = mutableListOf()

    private class ItemViewHolder(itemView: View) {
        val text: TextView = itemView.findViewById(R.id.textViewItemText)
        val comment: TextView = itemView.findViewById(R.id.textViewItemComment)
    }

    fun setItems(items: List<Item>) {
        dataset.apply {
            clear()
            addAll(items)
        }
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item, parent, false).also {
                    it.tag = ItemViewHolder(it)
                }
        val holder = view.tag as ItemViewHolder

        val Item = getItem(position)
        if (Item != null) {
            holder.text.text = Item.title
            holder.comment.text = Item.description
            /*holder.comment.text = parent.context.getString(
                    R.string.item_meta_format,
                    Item.date?.let { dateFormat.format(it) } ?: ""
            )*/
        } else {
            holder.text.text = ""
            holder.comment.text = ""
        }

        return view
    }

    override fun getCount(): Int = dataset.size

    override fun getItem(position: Int): Item? {
        return if (position >= 0 && position < dataset.size) {
            dataset[position]
        } else {
            null
        }
    }

    override fun getItemId(position: Int): Long = dataset[position].id

}
