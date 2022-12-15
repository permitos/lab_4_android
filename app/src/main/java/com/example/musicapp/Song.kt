package com.example.musicapp

import android.net.Uri

data class Song(
    val id: Int,            // идентификатор песни
    var nameSong: String,   // название песни
    val src: Uri,           // путь к исходному файлу песни
    val artistImage: Uri,   // путь к изображению песни
    val nameArtist: String, // имя исполнителя
    val duration: Int,      // длительность песни
)