package os.abuyahya.theholyquran.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import os.abuyahya.theholyquran.data.remote.HolyQuranDatabase
import os.abuyahya.theholyquran.exoplayer.State.*
import javax.inject.Inject

class FirebaseHolyQuranSource @Inject constructor(
    private val holyQuranDatabase: HolyQuranDatabase
) {

    var surahs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        val allSurahs = holyQuranDatabase.getAllSurahs()
        surahs = allSurahs.map { surah ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, surah.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, surah.id)
                .putString(METADATA_KEY_TITLE, surah.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, surah.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, surah.imgUrl)
                .putString(METADATA_KEY_MEDIA_URI, surah.url)
                .putString(METADATA_KEY_ALBUM_ART_URI, surah.imgUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, surah.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, surah.subtitle)
                .build()
        }
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        surahs.forEach { surah ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(surah.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = surahs.map { surah ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(surah.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(surah.description.title)
            .setSubtitle(surah.description.subtitle)
            .setMediaId(surah.description.mediaId)
            .setIconUri(surah.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE )
    }.toMutableList()

    private val onReadyListeners = mutableListOf< (Boolean) -> Unit >()
    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR){
                synchronized(onReadyListeners){
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING){
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITIALIZED)
            true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}
