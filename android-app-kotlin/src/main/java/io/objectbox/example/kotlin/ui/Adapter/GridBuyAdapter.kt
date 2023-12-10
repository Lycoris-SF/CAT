package io.objectbox.example.kotlin.ui.Adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.Item
import io.objectbox.example.kotlin.R

class GridBuyAdapter(private val context: Context, private var item: Item?) :
    RecyclerView.Adapter<GridBuyAdapter.ViewHolder>() {

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
            val adapterPosition = position
            var currentItem = item
            val currentBuy = item?.buyList?.getOrNull(position - 1)
            val currentPrice = item?.itemBuyPrice?.getOrNull(position - 1) ?: ""
            val currentStorage = item?.itemBuyStorage?.getOrNull(position - 1) ?: ""
            holder.textView1.setTextColor(colorText)
            holder.textView2.setTextColor(colorText)
            holder.textView3.setTextColor(colorText)
            holder.textView1.text = currentBuy
            holder.textView2.text = currentPrice
            holder.textView3.text = currentStorage
            holder.textView1.setOnClickListener {
                val editText = EditText(holder.itemView.context)
                editText.setText(currentBuy)

                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Edit Text")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val editedText = editText.text.toString()

                        //update
                        currentItem?.buyList?.set(adapterPosition-1, editedText)

                        notifyItemChanged(adapterPosition)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }
            holder.textView2.setOnClickListener {
                val editText = EditText(holder.itemView.context)
                editText.setText(currentPrice)

                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Edit Text")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val editedText = editText.text.toString()

                        //update
                        currentItem?.itemBuyPrice?.set(adapterPosition-1, editedText)

                        notifyItemChanged(adapterPosition)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }
            holder.textView3.setOnClickListener {
                val editText = EditText(holder.itemView.context)
                editText.setText(currentStorage)

                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Edit Text")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val editedText = editText.text.toString()

                        //update
                        currentItem?.itemBuyStorage?.set(adapterPosition-1, editedText)

                        notifyItemChanged(adapterPosition)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }
            holder.textView1.setOnLongClickListener {
                item?.buyList?.removeAt(adapterPosition - 1)
                item?.itemBuyPrice?.removeAt(adapterPosition - 1)
                item?.itemBuyStorage?.removeAt(adapterPosition - 1)

                notifyItemRemoved(adapterPosition)
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