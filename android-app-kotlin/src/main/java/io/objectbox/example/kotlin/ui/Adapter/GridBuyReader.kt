package io.objectbox.example.kotlin.ui.Adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.ItemViewManager
import io.objectbox.example.kotlin.Item_
import io.objectbox.example.kotlin.ObjectBoxSC
import io.objectbox.example.kotlin.R
import io.objectbox.query.QueryBuilder

class GridBuyReader(private val context: Context, private var item: Item?) :
    RecyclerView.Adapter<GridBuyReader.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.linear_col_recyclerview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView1.setTypeface(null, Typeface.NORMAL)
        holder.textView2.setTypeface(null, Typeface.NORMAL)
        holder.textView3.setTypeface(null, Typeface.NORMAL)
        var colorText = ContextCompat.getColor(context, R.color.black)

        if (position == 0) {
            holder.textView1.setTextColor(colorText)
            holder.textView2.setTextColor(colorText)
            holder.textView3.setTextColor(colorText)
            holder.textView1.text = "BUY"
            holder.textView2.text = "Price"
            holder.textView3.text = "Storage"

            holder.textView1.setTypeface(null, Typeface.BOLD)
            holder.textView2.setTypeface(null, Typeface.BOLD)
            holder.textView3.setTypeface(null, Typeface.BOLD)
        } else {
            val currentBuy = item?.buyList?.getOrNull(position - 1)
            val currentPrice = item?.itemBuyPrice?.getOrNull(position - 1) ?: ""
            val currentStorage = item?.itemBuyStorage?.getOrNull(position - 1) ?: ""
            holder.textView1.setTextColor(colorText)
            holder.textView2.setTextColor(colorText)
            holder.textView3.setTextColor(colorText)
            holder.textView1.text = currentBuy
            holder.textView2.text = currentPrice
            holder.textView3.text = currentStorage
            holder.textView1.setOnLongClickListener {
                val searchQuery = ObjectBoxSC.boxStore.boxFor(Item::class.java)
                    .query().equal(Item_.title, currentBuy, QueryBuilder.StringOrder.CASE_INSENSITIVE).build()
                val result = searchQuery.find()
                searchQuery.close()
                if (result.isNotEmpty()) {
                    val itemId = result.first().id
                    val context = holder.itemView.context
                    context.startActivity(ItemViewManager.intent(context, itemId))
                }
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return item?.buyList?.size?.plus(1) ?: 1
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView1: TextView = itemView.findViewById(R.id.textView1)
        val textView2: TextView = itemView.findViewById(R.id.textView2)
        val textView3: TextView = itemView.findViewById(R.id.textView3)
    }
}
