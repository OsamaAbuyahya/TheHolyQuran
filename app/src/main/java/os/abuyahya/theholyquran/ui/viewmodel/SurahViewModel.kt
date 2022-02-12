package os.abuyahya.theholyquran.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import os.abuyahya.theholyquran.exoplayer.HolyQuranService
import os.abuyahya.theholyquran.exoplayer.HolyQuranServiceConnection
import os.abuyahya.theholyquran.exoplayer.currentPlaybackPosition
import os.abuyahya.theholyquran.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import javax.inject.Inject

class SurahViewModel @ViewModelInject constructor(
    private val holyQuranServiceConnection: HolyQuranServiceConnection
): ViewModel() {

    val playbackState = holyQuranServiceConnection.playbackState

    private val _curSurahDuration = MutableLiveData<Long>()
    val curSurahDuration: LiveData<Long> = _curSurahDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos)
                    _curSurahDuration.postValue(HolyQuranService.curSurahDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}
