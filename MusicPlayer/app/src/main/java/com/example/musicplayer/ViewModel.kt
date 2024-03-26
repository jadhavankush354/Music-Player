package com.example.musicplayer

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.entity.Song
import com.example.musicplayer.entity.User
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    val context: Context = application.applicationContext
    val musicDatabase = MusicDatabase()
    var songs by mutableStateOf<List<Song>>(emptyList())
    var localSongs by mutableStateOf<List<Song>>(emptyList())
    var currentPlayingPlaylist by mutableStateOf<List<Song>>(emptyList())
    var currentPlayingSong by mutableStateOf(Song())
    var allUsersPlaylists by mutableStateOf<List<String>>(emptyList())
    var allArtists by mutableStateOf<List<String>>(emptyList())
    var allAlbums by mutableStateOf<List<String>>(emptyList())
    var users by mutableStateOf<List<User>>(emptyList())
    var user by mutableStateOf(User())
    var shuffle by mutableStateOf("repeat")
    var openPlaylist by mutableStateOf("") // Which playlist have to be shown
    var playListSongsToShow by mutableStateOf<List<Song>>(emptyList()) // Which playlist have to be shown
    var currentView by mutableStateOf("Songs") // Lists, Songs, Artists, Albums
    private val isLoggedInKey = "is_logged_in"
    private val emailKey = "email"
    private val passwordKey = "password"
    lateinit var sharedPreferences: SharedPreferences
    private var logInStatus by mutableStateOf(false)
    private var email by mutableStateOf("")
    private var password by mutableStateOf("")

    fun getLoggedUserStatus(): Boolean {
       return logInStatus
    }

    fun getLoggedUserEmail(): String {
        return email ?: ""
    }

    fun getLoggedUserPassword(): String {
        return password ?: ""
    }

    fun login() {
        sharedPreferences.edit().putBoolean(isLoggedInKey, true).apply()
        sharedPreferences.edit().putString(emailKey, user.email).apply()
        sharedPreferences.edit().putString(passwordKey, user.password).apply()
    }

    fun logout() {
        sharedPreferences.edit().putBoolean(isLoggedInKey, false).apply()
        sharedPreferences.edit().remove(emailKey).apply()
        sharedPreferences.edit().remove(passwordKey).apply()

        // Redirect to LoginScreen
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    object MusicViewModelHolder {
        lateinit var musicViewModel: MusicViewModel
    }

    init {
        sharedPreferences = context.getSharedPreferences("login_state", Context.MODE_PRIVATE)
        logInStatus = sharedPreferences.getBoolean(isLoggedInKey, false)
        email = sharedPreferences.getString(emailKey, "") ?: ""
        password = sharedPreferences.getString(passwordKey, "") ?: ""

        MusicViewModelHolder.musicViewModel = this
        observeSongs()
        observeCurrentView()
    }

    private fun observeSongs() {
        viewModelScope.launch {
            snapshotFlow { songs }
                .collect { songs ->
                    if (songs.isNotEmpty()) {
                        currentPlayingSong = songs[0]
                    }
                }
        }
    }

    private fun observeCurrentView() {
        viewModelScope.launch {
            snapshotFlow { currentView }
                .collect { view ->
                    if (view == "Songs") {
                        playListSongsToShow = songs
                    }
                    openPlaylist = "" // Reset openPlaylist whenever currentView changes
                }
        }
    }

    fun updateCurrentPlayingSong(song: Song, newPlaylist: List<Song> = emptyList()): Song {
        if (newPlaylist.isNotEmpty()){
            Log.d("debug", "First Song : ${newPlaylist[0].title}")
            currentPlayingPlaylist = newPlaylist
        }
        SongHelper.releasePlayer()
        currentPlayingSong = song
        SongHelper.playStream(currentPlayingSong.url, context)
        return currentPlayingSong
    }

    fun nextSong(): Song {
        return updateCurrentPlayingSong(if (currentPlayingPlaylist.indexOf(currentPlayingSong) + 1 >= currentPlayingPlaylist.size) currentPlayingPlaylist.first()
        else currentPlayingPlaylist[currentPlayingPlaylist.indexOf(currentPlayingSong) + 1])
    }

    fun previousSong(): Song {
        return updateCurrentPlayingSong(if (currentPlayingPlaylist.indexOf(currentPlayingSong) - 1 < 0) currentPlayingPlaylist.last()
        else currentPlayingPlaylist[currentPlayingPlaylist.indexOf(currentPlayingSong) - 1])
    }

    fun changeShuffle() {
        shuffle = if (shuffle == "repeat"){
            "repeat one"
        } else  if (shuffle == "repeat one") {
            currentPlayingPlaylist = currentPlayingPlaylist.shuffled()
            "shuffle"
        } else {
            currentPlayingPlaylist = songs
            "repeat"
        }
    }

    fun addSongToPlaylist(userId: String, playlistName: String, song: Song) {
        viewModelScope.launch {
            try {
                musicDatabase.createPlaylist(userId, playlistName, listOf(song))
            } catch (e: Exception) {
                // Handle exceptions here
            }
        }
    }

    fun removeSongFromPlaylist(playlistName: String, mediaId: String) {
        viewModelScope.launch {
            try {
                musicDatabase.deletePlaylistSong(user.userId, playlistName, mediaId)
            } catch (e: Exception) {
                // Handle exceptions here
            }
        }
    }
}