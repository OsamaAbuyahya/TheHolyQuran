package os.abuyahya.theholyquran.exoplayer.callback

import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import os.abuyahya.theholyquran.exoplayer.HolyQuranService

class HolyQuranPlayerEventListener(
    private val holyQuranService: HolyQuranService
): Player.EventListener {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == Player.STATE_READY && !playWhenReady){
            holyQuranService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(holyQuranService, "An unknown error occurred", Toast.LENGTH_SHORT).show()
    }
}
