package os.abuyahya.theholyquran.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import os.abuyahya.theholyquran.data.entites.Surah
import os.abuyahya.theholyquran.exoplayer.HolyQuranServiceConnection
import os.abuyahya.theholyquran.exoplayer.isPlayEnabled
import os.abuyahya.theholyquran.exoplayer.isPlaying
import os.abuyahya.theholyquran.exoplayer.isPrepared
import os.abuyahya.theholyquran.other.Constants.MEDIA_ROOT_ID
import os.abuyahya.theholyquran.other.Resource

class MainViewModel @ViewModelInject constructor(
    private val holyQuranServiceConnection: HolyQuranServiceConnection
): ViewModel() {

    private val _surahItems = MutableLiveData<Resource<List<Surah>>>()
    val surahItems: LiveData<Resource<List<Surah>>> = _surahItems

    val isConnected = holyQuranServiceConnection.isConnected
    val networkError = holyQuranServiceConnection.networkError
    val curPlayingSurah = holyQuranServiceConnection.curPlayingSurah
    val playbackState = holyQuranServiceConnection.playbackState

    init {
        _surahItems.postValue(Resource.loading(null))
        holyQuranServiceConnection.subscribe(MEDIA_ROOT_ID,
            object: MediaBrowserCompat.SubscriptionCallback() {

                override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Surah(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.iconUri.toString(),
                            it.description.mediaUri.toString()
                        )
                    }
                    _surahItems.postValue(Resource.success(items))

            }
        })
    }

    fun skipToNextSurah() {
        holyQuranServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSurah() {
        holyQuranServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        holyQuranServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSurah(surahItem: Surah, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && surahItem.id == curPlayingSurah.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) holyQuranServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled ->  holyQuranServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            holyQuranServiceConnection.transportControls.playFromMediaId(surahItem.id, null)
            Log.d("MainViewModel","Msg: else")
        }

    }

    override fun onCleared() {
        super.onCleared()
        holyQuranServiceConnection.unsubscribe(
            MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {}
        )
    }
}

