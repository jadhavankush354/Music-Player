package com.example.musicplayer.entity

data class User (
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var mobileNumber: String = "",
) {
    constructor() : this("", "", "", "", "")
}