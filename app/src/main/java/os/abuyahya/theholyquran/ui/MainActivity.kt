package os.abuyahya.theholyquran.ui

import android.graphics.Color
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.tapadoo.alerter.Alerter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert.*
import kotlinx.android.synthetic.main.fragment_home.*
import os.abuyahya.theholyquran.R
import os.abuyahya.theholyquran.adapters.SwipeSurahAdapter
import os.abuyahya.theholyquran.data.entites.Surah
import os.abuyahya.theholyquran.databinding.ActivityMainBinding
import os.abuyahya.theholyquran.exoplayer.isPlaying
import os.abuyahya.theholyquran.exoplayer.toSurah
import os.abuyahya.theholyquran.other.Status
import os.abuyahya.theholyquran.ui.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSurahAdapter: SwipeSurahAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSurah: Surah? = null

    private var playbackState: PlaybackStateCompat? = null

    private lateinit var binding: ActivityMainBinding

    private var test: Surah? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()

        binding.vpSurah.adapter = swipeSurahAdapter
        findNavController(R.id.fragment).addOnDestinationChangedListener(this)

        binding.vpSurah.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSurah(swipeSurahAdapter.surahs[position])
                } else {
                    curPlayingSurah = swipeSurahAdapter.surahs[position]
                }

            }
        })

        swipeSurahAdapter.setItemClickListener {
            findNavController(R.id.fragment).navigate(
                R.id.global_action_to_surah_fragment
            )
        }

        binding.ivPlayPause.setOnClickListener {
            curPlayingSurah?.let {
                mainViewModel.playOrToggleSurah(it, true)
            }
        }
    }


    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.surahFragment -> hideBottomBar()
            R.id.homeFragment -> showBottomBar()
            else -> Unit
        }
    }
    private fun hideBottomBar() {
        binding.vpSurah.isVisible = false
        binding.ivCurSurahImage.isVisible = false
        binding.ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        binding.vpSurah.isVisible = true
        binding.ivCurSurahImage.isVisible = true
        binding.ivPlayPause.isVisible = true
    }

    private fun switchViewPagerToCurrentSurah(surah: Surah) {
        val newItemIndex = swipeSurahAdapter.surahs.indexOf(surah)
        if (newItemIndex != -1) {
            binding.vpSurah.currentItem = newItemIndex
            curPlayingSurah = surah
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.surahItems.observe(this) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { surahs ->
                        swipeSurahAdapter.surahs = surahs
                        if (surahs.isNotEmpty()) {
                            glide.load((curPlayingSurah
                                ?: surahs[0]).imgUrl).into(binding.ivCurSurahImage)
                        }
                        switchViewPagerToCurrentSurah(curPlayingSurah ?: return@observe)
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> Unit
            }
        }
        mainViewModel.curPlayingSurah.observe(this) {
            if (it == null) return@observe

            curPlayingSurah = it.toSurah()
            glide.load(curPlayingSurah?.imgUrl).into(binding.ivCurSurahImage)
            switchViewPagerToCurrentSurah(curPlayingSurah ?: return@observe)
        }
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.root, result.message
                        ?: "An unknown error occurred",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    else -> Unit
                }
            }
        }
        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.root, result.message
                        ?: "An unknown error occurred",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    else -> Unit
                }
            }
        }

    }

}
