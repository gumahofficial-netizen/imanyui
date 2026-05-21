package com.example.data.service

import kotlinx.coroutines.flow.MutableStateFlow

object AudioPlayerState {
    val isPlayingFlow = MutableStateFlow(false)
    val currentTitleFlow = MutableStateFlow("لم يتم تحديد تلاوة")
    val currentSubtitleFlow = MutableStateFlow("اختر قارئاً وسورة للبدء")
    val currentProgressFlow = MutableStateFlow(0f)
    val currentDurationFlow = MutableStateFlow(0)
    val currentPositionFlow = MutableStateFlow(0)
    
    val activeTrackUrl = MutableStateFlow("")
    val isRadioActive = MutableStateFlow(false)

    // Callbacks to be controlled from Player screen
    var onPlayPause: (() -> Unit)? = null
    var onNext: (() -> Unit)? = null
    var onPrevious: (() -> Unit)? = null
    var onSeek: ((Float) -> Unit)? = null
}
