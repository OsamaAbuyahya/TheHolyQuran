package os.abuyahya.theholyquran.exoplayer

import android.support.v4.media.MediaMetadataCompat
import os.abuyahya.theholyquran.data.entites.Surah

fun MediaMetadataCompat.toSurah(): Surah {
    return description.let {
        Surah(
            it.mediaId ?: "", it.title.toString(),
            it.subtitle.toString(), it.iconUri.toString(), it.mediaUri.toString()
        )
    }
}
