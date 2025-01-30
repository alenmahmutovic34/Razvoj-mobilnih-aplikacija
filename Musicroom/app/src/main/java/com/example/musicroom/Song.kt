package com.example.musicroom.api

data class Song(
    val title: String,
    val artist: Artist,
    val album: Album,
    val preview: String,
    var votes: Int = 1
)
