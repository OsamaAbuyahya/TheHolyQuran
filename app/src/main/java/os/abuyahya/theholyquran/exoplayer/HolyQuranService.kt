package os.abuyahya.theholyquran.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.MediaSessionManager
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import os.abuyahya.theholyquran.data.entites.Surah
import os.abuyahya.theholyquran.exoplayer.callback.HolyQuranPlaybackPrepare
import os.abuyahya.theholyquran.exoplayer.callback.HolyQuranPlayerEventListener
import os.abuyahya.theholyquran.exoplayer.callback.HolyQuranPlayerNotificationListener
import os.abuyahya.theholyquran.other.Constants.MEDIA_ROOT_ID
import os.abuyahya.theholyquran.other.Constants.NETWORK_ERROR
import javax.inject.Inject

private const val SERVICE_TAG = "HolyQuranService"

@AndroidEntryPoint
class HolyQuranService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory
    @Inject
    lateinit var exoPlayer: SimpleExoPlayer
    @Inject
    lateinit var firebaseHolyQuranSource: FirebaseHolyQuranSource

    private lateinit var holyQuranNotificationManger: HolyQuranNotificationManger

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false
    private var isPlayerInitialized = false

    private var curPlayingSurah: MediaMetadataCompat? = null

    private lateinit var holyQuranPlayerEventListener: HolyQuranPlayerEventListener

    companion object {
        var curSurahDuration = 0L
            private set
    }
    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            firebaseHolyQuranSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        holyQuranNotificationManger = HolyQuranNotificationManger(
            this,
            mediaSession.sessionToken,
            HolyQuranPlayerNotificationListener(this)
        ) {
            curSurahDuration = exoPlayer.duration
        }

        val holyQuranPlaybackPreparer = HolyQuranPlaybackPrepare(firebaseHolyQuranSource) {
            curPlayingSurah = it
            preparePlayer(
                firebaseHolyQuranSource.surahs,
                curPlayingSurah!!,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(holyQuranPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(HolyQuranQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        holyQuranPlayerEventListener = HolyQuranPlayerEventListener(this)
        exoPlayer.addListener(holyQuranPlayerEventListener)
        holyQuranNotificationManger.showNotification(exoPlayer)
    }

    private inner class HolyQuranQueueNavigator: TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseHolyQuranSource.surahs[windowIndex].description
        }

    }

    private fun preparePlayer(
        surahs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat,
        playNow: Boolean
    ) {
        val curSurahIndex = if (curPlayingSurah == null) 0 else surahs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseHolyQuranSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSurahIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(holyQuranPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId) {
            MEDIA_ROOT_ID -> {
                val resultSend = firebaseHolyQuranSource.whenReady { isInitialized ->
                    if (isInitialized){
                        result.sendResult(firebaseHolyQuranSource.asMediaItems())
                        if (!isPlayerInitialized && firebaseHolyQuranSource.surahs.isNotEmpty()){
                            preparePlayer(firebaseHolyQuranSource.surahs, firebaseHolyQuranSource.surahs[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultSend) {
                    result.detach()
                }
            }
        }
    }
}
