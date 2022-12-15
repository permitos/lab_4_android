package com.example.musicapp

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata

object MediaLoader {

    var mediaItems: MutableList<MediaItem> = mutableListOf() // список воспроизводимых пеесен

    // подгружаем заданные песни

    fun loadMediaItems(songs: List<Song>) {
        for(song in songs) {
            val mediaItem = MediaItem.Builder()
                .setUri(song.src)
                .setMediaMetadata(getMetadata(song))
                .build()
            mediaItems += mediaItem
        }
    }

    // получаем метаданные этих же песен

    private fun getMetadata(song: Song): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.nameSong)
            .setArtworkUri(song.artistImage)
            .build()
    }
}