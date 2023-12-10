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
import io.objectbox.example.kotlin.R

class ExtraReader(private val context: Context, private var item: Item?, val type: Boolean) :
    RecyclerView.Adapter<ExtraReader.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.linear_col_recyclerview2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView1.setTypeface(null, Typeface.NORMAL)
        holder.textView2.setTypeface(null, Typeface.NORMAL)
        var colorText = ContextCompat.getColor(context, R.color.black)

        if (position == 0) {
            holder.textView1.setTextColor(colorText)
            holder.textView2.setTextColor(colorText)
            if(type) holder.textView1.text = "Assessment"
            else holder.textView1.text = "Comment"
            holder.textView2.text = "Content"

            holder.textView1.setTypeface(null, Typeface.BOLD)
            holder.textView2.setTypeface(null, Typeface.BOLD)
        } else {
            if(type){
                val currentTitle = item?.assessTitle?.getOrNull(position - 1)
                val currentContent = item?.assessContent?.getOrNull(position - 1)
                holder.textView1.setTextColor(colorText)
                holder.textView2.setTextColor(colorText)
                holder.textView1.text = currentTitle
                holder.textView2.text = currentContent
            }
            else{
                val currentTitle = item?.extraTitle?.getOrNull(position - 1)
                val currentContent = item?.extraContent?.getOrNull(position - 1)
                holder.textView1.setTextColor(colorText)
                holder.textView2.setTextColor(colorText)
                holder.textView1.text = currentTitle
                holder.textView2.text = currentContent
            }
        }
    }

    override fun getItemCount(): Int {
        if(type) return item?.assessTitle?.size?.plus(1) ?: 1
        else return item?.extraTitle?.size?.plus(1) ?: 1
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView1: TextView = itemView.findViewById(R.id.textView1)
        val textView2: TextView = itemView.findViewById(R.id.textView2)
    }
}
