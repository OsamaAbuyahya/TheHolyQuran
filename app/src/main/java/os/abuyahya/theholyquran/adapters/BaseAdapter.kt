package os.abuyahya.theholyquran.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.android.synthetic.main.row_surah.view.*
import os.abuyahya.theholyquran.data.entites.Surah

abstract class BaseAdapter(val layoutId: Int): RecyclerView.Adapter<BaseAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    protected val diffCallback = object: DiffUtil.ItemCallback<Surah>() {
        override fun areItemsTheSame(oldItem: Surah, newItem: Surah): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Surah, newItem: Surah): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract val differ: AsyncListDiffer<Surah>

    var surahs: List<Surah>
        get() = differ.currentList
        set(value) = differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
        )
    }


    protected var onItemClickListener: ((Surah) -> Unit)? = null

    fun setItemClickListener(listener: (Surah) -> Unit) {
        this.onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return surahs.size
    }

}
