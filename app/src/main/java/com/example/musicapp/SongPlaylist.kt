package com.example.musicapp

class SongPlaylist(var songActive: SongExtensions?) {

    // изменить текущую песню на воспроизводимую
    fun changeActiveSong(song: SongExtensions) {
        songActive?.resetSettings()
        songActive = song
    }
}