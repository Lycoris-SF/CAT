package io.objectbox.example.kotlin.ui.Adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.example.kotlin.R

class GridItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val dividerHeight: Int = 2 // 分割线高度，单位为像素
    private val dividerColor: Int = ContextCompat.getColor(context, R.color.black)

    private val paint = Paint()

    init {
        paint.color = dividerColor
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawVerticalDivider(c, parent)
        drawHorizontalDivider(c, parent)
    }

    private fun drawVerticalDivider(canvas: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val top = child.top
            val right = left + dividerHeight
            val bottom = child.bottom
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }

    private fun drawHorizontalDivider(canvas: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left
            val top = child.bottom + params.bottomMargin
            val right = child.right
            val bottom = top + dividerHeight
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }
}
