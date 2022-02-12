package os.abuyahya.theholyquran.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.row_surah.view.*
import os.abuyahya.theholyquran.R
import os.abuyahya.theholyquran.data.entites.Surah
import javax.inject.Inject


class SurahAdapter @Inject constructor(
    private val glide: RequestManager
): BaseAdapter(R.layout.row_surah) {

    override val differ: AsyncListDiffer<Surah> = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val surah = surahs[position]
        holder.itemView.apply {
            tv_title.text = surah.title
            tv_subtitle.text = surah.subtitle
            glide.load(surah.imgUrl).into(img)
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.let { click ->
                click(surah)
            }
        }
    }
}
