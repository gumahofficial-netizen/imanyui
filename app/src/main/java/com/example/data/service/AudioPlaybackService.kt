package com.example.data.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.IOException

class AudioPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    companion object {
        const val CHANNEL_ID = "imany_playback_channel"
        const val NOTIFICATION_ID = 997
        
        const val ACTION_PLAY_NEW = "com.example.action.PLAY_NEW"
        const val ACTION_TOGGLE_PLAY = "com.example.action.TOGGLE_PLAY"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREV = "com.example.action.PREV"
        const val ACTION_SEEK = "com.example.action.SEEK"

        const val EXTRA_URL = "extra_audio_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_IS_RADIO = "extra_is_radio"
        const val EXTRA_SEEK_PCT = "extra_seek_pct"
        
        fun startService(context: Context, action: String, extras: Map<String, Any>? = null) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                this.action = action
                extras?.forEach { (key, value) ->
                    when (value) {
                        is String -> putExtra(key, value)
                        is Boolean -> putExtra(key, value)
                        is Float -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupStateCallbacks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_PLAY_NEW -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: ""
                val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
                val subtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""
                val isRadio = intent.getBooleanExtra(EXTRA_IS_RADIO, false)
                playNewUrl(url, title, subtitle, isRadio)
            }
            ACTION_TOGGLE_PLAY -> {
                togglePlay()
            }
            ACTION_STOP -> {
                stopPlaying()
                stopSelf()
            }
            ACTION_NEXT -> {
                AudioPlayerState.onNext?.invoke()
            }
            ACTION_PREV -> {
                AudioPlayerState.onPrevious?.invoke()
            }
            ACTION_SEEK -> {
                val seekPct = intent.getFloatExtra(EXTRA_SEEK_PCT, 0f)
                seekToPercent(seekPct)
            }
        }

        return START_STICKY
    }

    private fun setupStateCallbacks() {
        AudioPlayerState.onPlayPause = {
            togglePlay()
            updateNotification()
        }
        AudioPlayerState.onSeek = { seekPct ->
            seekToPercent(seekPct)
        }
    }

    private fun playNewUrl(url: String, title: String, subtitle: String, isRadio: Boolean) {
        if (url.isEmpty()) return

        stopPlaying()

        AudioPlayerState.activeTrackUrl.value = url
        AudioPlayerState.currentTitleFlow.value = title
        AudioPlayerState.currentSubtitleFlow.value = subtitle
        AudioPlayerState.isRadioActive.value = isRadio
        AudioPlayerState.isPlayingFlow.value = false
        AudioPlayerState.currentProgressFlow.value = 0f

        updateForegroundNotification(title, subtitle, false)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    AudioPlayerState.isPlayingFlow.value = true
                    AudioPlayerState.currentDurationFlow.value = if (isRadio) 0 else mp.duration
                    updateForegroundNotification(title, subtitle, true)
                    startProgressTracker()
                }
                setOnCompletionListener {
                    AudioPlayerState.isPlayingFlow.value = false
                    AudioPlayerState.currentProgressFlow.value = 1f
                    stopProgressTracker()
                    updateForegroundNotification(title, subtitle, false)
                    // Auto-trigger next surah if possible
                    AudioPlayerState.onNext?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    AudioPlayerState.isPlayingFlow.value = false
                    stopProgressTracker()
                    updateForegroundNotification("خطأ في تشغيل الصوت", subtitle, false)
                    true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                updateForegroundNotification("خطأ في جلب المقطع الصوتي", subtitle, false)
            }
        }
    }

    private fun togglePlay() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                AudioPlayerState.isPlayingFlow.value = false
                stopProgressTracker()
                updateForegroundNotification(
                    AudioPlayerState.currentTitleFlow.value,
                    AudioPlayerState.currentSubtitleFlow.value,
                    false
                )
            } else {
                mp.start()
                AudioPlayerState.isPlayingFlow.value = true
                startProgressTracker()
                updateForegroundNotification(
                    AudioPlayerState.currentTitleFlow.value,
                    AudioPlayerState.currentSubtitleFlow.value,
                    true
                )
            }
        }
    }

    private fun seekToPercent(pct: Float) {
        if (AudioPlayerState.isRadioActive.value) return // Can't seek radio
        mediaPlayer?.let { mp ->
            val duration = mp.duration
            if (duration > 0) {
                val dest = (duration * pct).toInt()
                mp.seekTo(dest)
                AudioPlayerState.currentPositionFlow.value = dest
                AudioPlayerState.currentProgressFlow.value = pct
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        if (AudioPlayerState.isRadioActive.value) return // radio has no progress duration

        progressJob = serviceScope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val duration = mp.duration
                        val current = mp.currentPosition
                        if (duration > 0) {
                            AudioPlayerState.currentPositionFlow.value = current
                            AudioPlayerState.currentProgressFlow.value = current.toFloat() / duration.toFloat()
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun stopPlaying() {
        stopProgressTracker()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            it.release()
        }
        mediaPlayer = null
        AudioPlayerState.isPlayingFlow.value = false
    }

    private fun updateNotification() {
        updateForegroundNotification(
            AudioPlayerState.currentTitleFlow.value,
            AudioPlayerState.currentSubtitleFlow.value,
            AudioPlayerState.isPlayingFlow.value
        )
    }

    private fun updateForegroundNotification(title: String, subtitle: String, isPlaying: Boolean) {
        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        val toggleIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY }
        val togglePending = PendingIntent.getService(this, 1, toggleIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPending = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_PREV }
        val prevPending = PendingIntent.getService(this, 3, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        // Intent to launch MainActivity when clicking the notification
        val openIntent = Intent(this, Class.forName("com.example.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(android.R.drawable.presence_audio_online)
            .setContentIntent(openPending)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPending)
            )
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPending)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", togglePending)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)
            .setOngoing(isPlaying)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Imany Playback Service"
            val descriptionText = "Islamic radio and Quran audios background playing service"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopPlaying()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
