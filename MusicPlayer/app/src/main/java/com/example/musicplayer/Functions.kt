package com.example.musicplayer

import android.widget.Toast
import com.example.musicplayer.entity.User

fun AuthenticateUser(users: List<User>, email: String, password: String): User? {
    for (user in users) {
        if (user.email == email && user.password == password) {
            return user // Authentication successful
        }
    }
    return null // Authentication failed
}

fun formatTime(milliseconds: Int): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun isValidMobileNumber(mobileNumber: String, viewModel: MusicViewModel): Boolean {
    if (mobileNumber.all { it.isDigit() }) {
        if (mobileNumber.length == 10) {
            if (mobileNumber.first() != '0') {
                return true
            } else {
                Toast.makeText(viewModel.context, "Mobile number should not start with 0", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            Toast.makeText(viewModel.context, "Mobile number should be exactly 10 digits long", Toast.LENGTH_SHORT).show()
            return false
        }
    } else {
        Toast.makeText(viewModel.context, "Mobile number should contain only digits", Toast.LENGTH_SHORT).show()
        return false
    }
}