package os.abuyahya.theholyquran.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import os.abuyahya.theholyquran.R
import os.abuyahya.theholyquran.data.entites.Surah
import os.abuyahya.theholyquran.databinding.FragmentSurahBinding
import os.abuyahya.theholyquran.exoplayer.isPlaying
import os.abuyahya.theholyquran.exoplayer.toSurah
import os.abuyahya.theholyquran.other.Status
import os.abuyahya.theholyquran.ui.viewmodel.MainViewModel
import os.abuyahya.theholyquran.ui.viewmodel.SurahViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SurahFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var binding: FragmentSurahBinding

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val surahViewModel: SurahViewModel by viewModels()

    private var curPlayingSurah: Surah? = null
    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSurahBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        subscribeToObservers()

        binding.seekBar.setOnSeekBarChangeListener(this)
        binding.ivPlayPauseDetail.setOnClickListener {
            curPlayingSurah?.let {
                mainViewModel.playOrToggleSurah(it, true)
            }
        }

        binding.ivSkipNext.setOnClickListener {
            mainViewModel.skipToNextSurah()
        }

        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSurah()
        }
        return binding.root
    }


    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            setCurPlayerTimeToTextView(progress.toLong())
        }
    }
    override fun onStartTrackingTouch(p0: SeekBar?) {
        shouldUpdateSeekBar = false
    }
    override fun onStopTrackingTouch(p0: SeekBar?) {
        binding.seekBar.let {
            mainViewModel.seekTo(it.progress.toLong())
            shouldUpdateSeekBar = true
        }
    }

    private fun updateTitleAndSurahImage(surah: Surah) {
        binding.tvSurahName.text = surah.title
        glide.load(surah.imgUrl).into(binding.ivSurahImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.surahItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    result.data?.let { surah ->
                        if (curPlayingSurah == null && surah.isNotEmpty()) {
                            curPlayingSurah = surah[0]
                            updateTitleAndSurahImage(curPlayingSurah!!)
                        }
                    }
                }
                else -> Unit
            }
        }
        mainViewModel.curPlayingSurah.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            curPlayingSurah = it.toSurah()
            updateTitleAndSurahImage(curPlayingSurah!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            binding.ivPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }
        surahViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekBar) {
                binding.seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }
        surahViewModel.curSurahDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            setCurSurahDurationTextView(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.tvCurTime.text = dateFormat.format(ms)
    }
    private fun setCurSurahDurationTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.tvSurahDuration.text = dateFormat.format(ms)
    }
}
