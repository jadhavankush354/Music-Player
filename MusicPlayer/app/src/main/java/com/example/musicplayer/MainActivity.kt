package com.example.musicplayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.entity.User
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private val permissionRequestCode = 123
    override fun onDestroy() {
        SongHelper.releasePlayer()
        Intent(this, NotificationService::class.java).also {
            it.action = NotificationService.Actions.STOP.toString()
            startService(it)
        }
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "running_channel",
                "Running Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MusicViewModel = viewModel()
                    var isPlaying by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            isPlaying = SongHelper.mediaPlayer?.isPlaying?: false
                            delay(1000)
                        }
                    }
                    if (isPlaying) {
                        Intent(this, NotificationService::class.java).also {
                            it.action = NotificationService.Actions.START.toString()
                            startService(it)
                        }
                    }
                    NavController(viewModel, activity = this@MainActivity)
                }
            }
        }
        requestPermission()
    }
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                permissionRequestCode
            )
        }
    }
}

@Composable
fun NavController(viewModel: MusicViewModel, activity: MainActivity) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.users = viewModel.musicDatabase.getAllUsers()
            isLoggedIn = viewModel.getLoggedUserStatus()
            email = viewModel.getLoggedUserEmail()
            password = viewModel.getLoggedUserPassword()
            viewModel.songs = viewModel.musicDatabase.getAllSongs()
            if (ContextCompat.checkSelfPermission(viewModel.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                viewModel.localSongs = viewModel.musicDatabase.getAllLocalSongs(viewModel.context)
            if (viewModel.currentPlayingPlaylist.isEmpty())
                viewModel.currentPlayingPlaylist = viewModel.songs
            if (viewModel.playListSongsToShow.isEmpty())
                viewModel.playListSongsToShow = viewModel.songs
            if (viewModel.user != User())
                viewModel.allUsersPlaylists = viewModel.musicDatabase.getAllPlaylists(viewModel.user.userId)
            viewModel.allArtists = viewModel.musicDatabase.getAllArtists()
            viewModel.allAlbums = viewModel.musicDatabase.getAllAlbums()
            delay(1000)
        }
    }
    NavHost(navController = navController, startDestination = "LogInScreen") {
        composable("LogInScreen") {
            if (viewModel.users.isNotEmpty()) {
                if (email.isNotEmpty() && password.isNotEmpty() && isLoggedIn && viewModel.users.isNotEmpty()) {
                    viewModel.user = AuthenticateUser(viewModel.users, email, password)?: User()
                    navController.navigate("PlayerScreen")
                } else {
                    LogInScreen(navController, viewModel)
                }
            } else {
                CircularIndicator()
            }
        }
        composable("RegistrationScreen") {
            RegistrationScreen(navController, viewModel)
        }
        composable("ForgotPasswordScreen") {
            ForgotPasswordScreen(navController, viewModel)
        }
        composable("PlayerScreen") {
            if (viewModel.songs.isNotEmpty()) {
                PlayerScreen(navController, viewModel)
            }
        }
        composable("HomeScreen") {
            HomeScreen(navController, viewModel)
        }
        composable("ProfileScreen") {
            ProfileScreen(navController, viewModel, activity)
        }
    }
}