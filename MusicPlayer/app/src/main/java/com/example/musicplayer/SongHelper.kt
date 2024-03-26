package com.example.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class SongHelper {
    companion object {
        var mediaPlayer: MediaPlayer? = null
        var currentPosition = 0
        var duration = 0

        fun playStream(url: String, context: Context) {
            mediaPlayer?.let {
                if(it.isPlaying) {
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                }
            }
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                when {
                    url.startsWith("https://") || url.startsWith("http://") -> {
                        setDataSource(url)
                    }
                    url.startsWith("content://") -> {
                        setDataSource(context, Uri.parse(url))
                    }
                    else -> {
                        // Handle unsupported URI type
                    }
                }
                prepareAsync()
            }
            mediaPlayer?.setOnPreparedListener { mediaPlayer ->
                duration = mediaPlayer.duration
                mediaPlayer.seekTo(currentPosition)
                mediaPlayer.start()
            }
        }


        fun pauseStream() {
            mediaPlayer?.let {
                currentPosition = it.currentPosition
                it.pause()
            }
        }

        fun stopStream() {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            currentPosition = 0
        }

        fun releasePlayer() {
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
            currentPosition = 0
        }
    }
}