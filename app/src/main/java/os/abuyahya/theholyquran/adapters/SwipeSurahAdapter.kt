package os.abuyahya.theholyquran.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.swipe_surah_item.view.*
import os.abuyahya.theholyquran.R
import os.abuyahya.theholyquran.data.entites.Surah
import javax.inject.Inject

class SwipeSurahAdapter: BaseAdapter(R.layout.swipe_surah_item) {

    override val differ: AsyncListDiffer<Surah> = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val surah = surahs[position]
        holder.itemView.apply {
            tvPrimary.text = surah.title
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.let { click ->
                click(surah)
            }
        }
    }
}
