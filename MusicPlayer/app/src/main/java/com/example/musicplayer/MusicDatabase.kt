package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import com.example.musicplayer.entity.Song
import com.example.musicplayer.entity.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID


class MusicDatabase {

    private val firestore = Firebase.firestore

    private val storageRef = FirebaseStorage.getInstance("gs://musicplayer-fd3fd.appspot.com").reference

    suspend fun createUser(user: User): String {
        try {
            val usersCollectionRef = firestore.collection("Users")
            val newUser = hashMapOf("name" to user.name, "email" to user.email, "password" to user.password, "mobileNumber" to user.mobileNumber)
            val documentReference = usersCollectionRef.add(newUser).await()
            val documentId = documentReference.id
            val playlistNamesCollectionRef = usersCollectionRef.document(documentId).collection("PlaylistNames")
            playlistNamesCollectionRef.document("Favorite").set(hashMapOf<String, Any>())
            return documentId
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserDetails(user: User, password: String): String {
        try {
            val usersCollectionRef = firestore.collection("Users")
            val documentReference = usersCollectionRef.document(user.userId) // Assuming you have a userId field in your User model

            val updatedData = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "password" to password,
                "mobileNumber" to user.mobileNumber
            )

            documentReference.set(updatedData, SetOptions.merge()).await()
            return "Password updated successfully"
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllUsers(): List<User> {
        try {
            val usersList = mutableListOf<User>()
            val result = firestore.collection("Users").get().await()
            for (document in result) {
                val userId = document.id
                val name = document.getString("name") ?: ""
                val email = document.getString("email") ?: ""
                val password = document.getString("password") ?: ""
                val mobileNumber = document.getString("mobileNumber") ?: ""
                val user = User(userId, name, email, password, mobileNumber)
                if (email.isNotBlank() && password.isNotBlank())
                    usersList.add(User(userId, name, email, password, mobileNumber))
            }
            return usersList
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllSongs(): List<Song> {
        try {
            return convertListIntoListOfSongs(firestore.collection("AllSongs").get().await())
        } catch (e: Exception) {
            throw e
        }
    }

    fun uploadLocalSong(context: Context, uri: Uri, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val songDetails = getSongDetailsFromUri(context, uri)
        val songRef: StorageReference = storageRef.child("songs/${songDetails.title}_${UUID.randomUUID()}")
        val uploadTask = songRef.putFile(uri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            songRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val songData = hashMapOf(
                    "image" to songDetails.image,
                    "artist" to songDetails.artist,
                    "url" to downloadUri.toString(),
                    "title" to songDetails.title,
                    "lyrics" to songDetails.lyrics
                )
                firestore.collection("AllSongs").add(songData)
                    .addOnSuccessListener {
                        onSuccess.invoke()
                    }
                    .addOnFailureListener { exception ->
                        onFailure.invoke(exception.message ?: "Unknown error occurred")
                    }
            }
        }.addOnFailureListener { exception ->
            onFailure.invoke(exception.message ?: "Unknown error occurred")
        }
    }

    private fun getSongDetailsFromUri(context: Context, uri: Uri): Song {
        val song = mutableStateOf(Song())
        context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST), "${MediaStore.Audio.Media.DATA} = ?", arrayOf(uri.path), null)?.use {
            while (it.moveToNext()) {
                val fetchedSong = Song(
                    mediaId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)).toString(),
                    image = "https://cdn.saleminteractivemedia.com/shared/images/default-cover-art.png",
                    artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist",
                    url = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))).toString(),
                    title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown",
                    lyrics = "No lyrics"
                )
                if (fetchedSong.url.isNotBlank())
                    song.value = fetchedSong
            }
//            if (it.moveToFirst()) {
//                val mediaId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)).toString()
//                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
//                val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
//                val url = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId.toLong()).toString()
//                return Song(image = "https://cdn.saleminteractivemedia.com/shared/images/default-cover-art.png", artist = artist, url = url, title = title, lyrics = "No lyrics")
//            }
        }
        return Song(image = "https://cdn.saleminteractivemedia.com/shared/images/default-cover-art.png", artist = "Unknown Artist", url = uri.toString(), title = "Unknown", lyrics = "No lyrics")
    }

    @SuppressLint("SuspiciousIndentation")
    fun getAllLocalSongs(context: Context): List<Song> {
        try {
            val songs = mutableListOf<Song>()
                context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST), MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null)?.use {
                while (it.moveToNext()) {
                    val song = Song(
                        mediaId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)).toString(),
                        image = "https://cdn.saleminteractivemedia.com/shared/images/default-cover-art.png",
                        artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist",
                        url = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))).toString(),
                        title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown",
                        lyrics = "No lyrics"
                    )
                    if (song.url.isNotBlank())
                        songs.add(song)
                }
            }
            return songs
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createPlaylist(userId: String, collectionName: String, songs: List<Song>) {
        try {
            val userDocumentRef = firestore.collection("Users").document(userId)
            userDocumentRef.collection("PlaylistNames").document(collectionName).set(hashMapOf<String, Any>())
            val newCollectionRef = userDocumentRef.collection(collectionName)
            for (song in songs) { newCollectionRef.add(song) }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createArtist(collectionName: String, songs: List<Song>) {
        try {
            val userDocumentRef = firestore.collection("AllSongs").document("Artists")
            userDocumentRef.collection("ArtistsNames").document(collectionName).set(hashMapOf<String, Any>())
            val newCollectionRef = userDocumentRef.collection(collectionName)
            for (song in songs) { newCollectionRef.add(song) }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createAlbum(collectionName: String, songs: List<Song>) {
        try {
            val userDocumentRef = firestore.collection("AllSongs").document("Albums")
            userDocumentRef.collection("AlbumNames").document(collectionName).set(hashMapOf<String, Any>())
            val newCollectionRef = userDocumentRef.collection(collectionName)
            for (song in songs) { newCollectionRef.add(song) }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getPlaylist(userId: String, playlistName: String): List<Song> {
        try {
            return convertListIntoListOfSongs(firestore.collection("Users").document(userId).collection(playlistName).get().await())
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getArtist(artistName: String): List<Song> {
        try {
            return convertListIntoListOfSongs(firestore.collection("AllSongs").document("Artists").collection(artistName).get().await())
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAlbums(albumsName: String): List<Song> {
        try {
            val songsList = mutableListOf<Song>()
            val result = firestore.collection("AllSongs").document("Albums").collection(albumsName).get().await()
            for (document in result) {
                val mediaId = document.id
                val image = document.getString("image")?.takeIf { it.isNotBlank() } ?: "https://cdn.saleminteractivemedia.com/shared/images/default-cover-art.png"
                val artist = document.getString("artist")?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
                val url = document.getString("url") ?: ""
                val title = document.getString("title")?.takeIf { it.isNotBlank() } ?: "Unknown"
                val lyrics = document.getString("lyrics")?.takeIf { it.isNotBlank() } ?: "No lyrics"
                if (url.isNotBlank())
                    songsList.add(Song(mediaId, image, artist, url, title, lyrics))
            }
            return songsList
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllPlaylists(userId: String): List<String> {
        val playListNames = mutableListOf<String>()
        try {
            val userDocumentRef = firestore.collection("Users").document(userId)
            val playlistNamesSnapshot = userDocumentRef.collection("PlaylistNames").get().await()
            for (document in playlistNamesSnapshot.documents) {
                playListNames.add(document.id)
            }
        } catch (e: Exception) {
            throw e
        }
        return playListNames
    }

    suspend fun getAllArtists(): List<String> {
        val documents = mutableListOf<String>()
        try {
            val userDocumentRef = firestore.collection("AllSongs").document("Artists")
            val playlistNamesSnapshot = userDocumentRef.collection("ArtistsNames").get().await()
            for (document in playlistNamesSnapshot.documents) {
                documents.add(document.id)
            }
        } catch (e: Exception) {
            throw e
        }
        return documents
    }

    suspend fun getAllAlbums(): List<String> {
        val documents = mutableListOf<String>()
        try {
            val userDocumentRef = firestore.collection("AllSongs").document("Albums")
            val playlistNamesSnapshot = userDocumentRef.collection("AlbumNames").get().await()
            for (document in playlistNamesSnapshot.documents) {
                documents.add(document.id)
            }
        } catch (e: Exception) {
            throw e
        }
        return documents
    }

    suspend fun deletePlaylistSong(userId: String, playlistName: String, mediaId: String) {
        try {
            val userDocumentRef = firestore.collection("Users").document(userId)
            val favoritePlaylistRef = userDocumentRef.collection(playlistName)
            favoritePlaylistRef.document(mediaId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun isSongPresentInPlaylist(userId: String, playlistName: String, mediaId: String): Boolean {
        return try {
            val userDocumentRef = firestore.collection("Users").document(userId)
            val favoritePlaylistRef = userDocumentRef.collection(playlistName)
            favoritePlaylistRef.document(mediaId).get().await().id.isNotEmpty()
        } catch (e: Exception) {
            throw e
        }
    }

    private fun convertListIntoListOfSongs(result: QuerySnapshot): List<Song> {
        val songsList = mutableListOf<Song>()
        for (document in result) {
            val mediaId = document.id
            val image = document.getString("image")?.takeIf { it.isNotBlank() } ?: "https://cdn.saleminteractivemedia.com/shared/images/default-cover-art.png"
            val artist = document.getString("artist")?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val url = document.getString("url") ?: ""
            val title = document.getString("title")?.takeIf { it.isNotBlank() } ?: "Unknown"
            val lyrics = document.getString("lyrics")?.takeIf { it.isNotBlank() } ?: "No lyrics"
            if (url.isNotBlank())
                songsList.add(Song(mediaId, image, artist, url, title, lyrics))
        }
        return songsList
    }

}