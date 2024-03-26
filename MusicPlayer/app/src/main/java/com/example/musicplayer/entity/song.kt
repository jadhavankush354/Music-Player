package com.example.musicplayer.entity

data class Song (
    var mediaId: String = "",
    var image: String = "",
    var artist: String = "",
    var url: String = "",
    var title: String = "",
    var lyrics: String = ""
) {
    constructor() : this("", "", "", "", "", "")
}