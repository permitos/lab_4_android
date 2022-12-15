package com.example.musicapp

import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.imageview.ShapeableImageView

class SongExtensions (
    private val song: Song,                      // ссылка на данные песни
    private val playButton: ShapeableImageView,  // кнопка воспроизведения
    private var playStatus: Boolean,             // статус воспроизведения
    private val favoriteButton: ImageButton,     // кнопка "избранное"
    private var favoriteStatus: Boolean,         // статус избранного
    private val moreButton: ImageButton,         // кнопка с действиями песни
    private val layoutActive: ConstraintLayout,  // фон области песни с действиями
    private var playlist: SongPlaylist?,         // установщик активной песни в текущем плейлисте
    private var player: ExoPlayer?               // объект класса "плеер"
) {

    init {
        loadSettings() // загрузка настроек
    }

    private fun loadSettings() {

        // изменение параметров отображения и статуса
        // "избранного" в зависимости от заданого положения

        favoriteButton.setOnClickListener {
            if (favoriteStatus)
                favoriteButton.setImageResource(R.drawable.ic_favorite)
            else
                favoriteButton.setImageResource(R.drawable.ic_active_favorite)
            favoriteStatus = !favoriteStatus
        }

        // изменение параметров отображения и статуса
        // "воспроизведения" в зависимости от заданого положения

        playButton.setOnClickListener {

            // проверка теущей активности
            if(this != playlist?.songActive){
                playlist?.changeActiveSong(this)
            }

            // установка активной области
            layoutActive.setBackgroundResource(R.drawable.layout_active_bg)

            // в случае, если песня воспроизводится
            if (playStatus) {
                playButton.setBackgroundResource(R.drawable.active_play)
                player?.pause()
            }
            // в случае, если песня не воспроизводится
            else {
                playButton.setBackgroundResource(R.drawable.active_pause)

                // соответствующая настройка плеера
                if(player?.isPlaying == false) {
                    player!!.setMediaItems(MediaLoader.mediaItems, song.id, 0)
                } else {
                    player?.pause()
                    player?.seekTo(song.id, 0)
                }
                player?.prepare()
                player?.play()
            }
            playStatus = !playStatus
        }
    }

    // сброс настроек к статусу "по умолчанию"
    // относительно воспроизведения

    fun resetSettings() {
        playStatus = false
        playButton.setBackgroundResource(R.drawable.not_active_play)
        layoutActive.setBackgroundResource(R.color.background_color)
    }

}