package com.example.musicroom.api

data class Album(val title: String) {
    override fun toString(): String {
        return "Album(title=$title)"
    }
}
