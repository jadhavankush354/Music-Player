package com.example.musicplayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.musicplayer.entity.Song

class NotificationService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val currentPlayingSong = MusicViewModel.MusicViewModelHolder.musicViewModel.currentPlayingSong
        when(intent?.action) {
            Actions.START.toString() -> start(currentPlayingSong)
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("ForegroundServiceType")
    private fun start(currentPlayingSong: Song) {
        val notification = NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.play)
            .setContentTitle(currentPlayingSong.title)
            .setContentText(currentPlayingSong.artist)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.applicationicon))
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        startForeground(1, notification)
    }

    enum class Actions {
        START, STOP
    }
}