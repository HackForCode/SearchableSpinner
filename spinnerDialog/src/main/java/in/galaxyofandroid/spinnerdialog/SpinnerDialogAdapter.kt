package `in`.galaxyofandroid.spinnerdialog

import android.app.Activity
import android.os.Build
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import java.lang.Math.min


/**
 * Created by Mike on 07.04.17
 */
class SpinnerDialogAdapter<E : Parcelable>(
        private val activity: Activity,
        private val itemManager: ItemManager<E>
) : RecyclerView.Adapter<SpinnerDialogAdapter<E>.SearchItemHolder>() {

    var listener: OnItemClickListener<E>? = null

    private var items: List<E> = emptyList()

    fun setData(new: List<E>) {
        val old = items
        items = ArrayList(new)
        val min = min(old.size, new.size)
        for (i in 0 until min) {
            if (!itemManager.equals(old[i], new[i])) {
                notifyItemChanged(i)
            }
        }
        val diff = new.size - old.size
        if (diff > 0) {
            // added
            notifyItemRangeInserted(min, diff)
        } else if (diff < 0) {
            // removed
            notifyItemRangeRemoved(min, -diff)
        }

    }

    override fun onBindViewHolder(holder: SearchItemHolder, position: Int) {
        holder.view.text = itemManager.toString(items[position])
        listener?.onItemBound(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemHolder =
            SearchItemHolder(
                    LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                            .apply {
                                val a = activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
                                val selectableItemBackground = a.getDrawable(0) // requires separate drawable per item
                                a.recycle()
                                if (Build.VERSION.SDK_INT >= 16) background = selectableItemBackground else setBackgroundDrawable(selectableItemBackground)
                                isClickable = true
                            } as TextView)

    override fun getItemCount(): Int =
            items.size

    inner class SearchItemHolder(
            val view: TextView
    ) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                listener?.onItemClick(items[adapterPosition])
            }
        }
    }

    interface OnItemClickListener<E> { // O_o can't rename
        fun onItemClick(e: E)
        fun onItemBound(position: Int)
    }

}