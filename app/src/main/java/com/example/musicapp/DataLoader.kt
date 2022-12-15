package com.example.musicapp

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object DataLoader {

    var songs: MutableList<Song> = mutableListOf() // загруженные песни

    // загрузка песен с носителя
    @SuppressLint("Recycle")
    fun loadSong(contentResolver: ContentResolver) {

        // получаем uri стиля для основного внешнего тома хранилища
        val mediaStoreUri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // основные "подгружаемые" uri данные
        val projection: Array<String> = arrayOf (
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
        )

        // установка сортировки
        val sortOrder: String = MediaStore.Audio.Media.DATE_ADDED + " DESC"

        // получение данных по заданному запросу
        val cursor: Cursor? = contentResolver.query(mediaStoreUri, projection, null, null, sortOrder)

        // получение строк (полей арибутов) запроса
        val idColumn: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameColumn: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val durationColumn: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val albumColumn: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val artistName: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

        // непосредственный процесс инициализации данных
        if (cursor != null) {
            var indx = 0
            while (cursor.moveToNext()) {

                // получение "описательных" данных песни
                val id: Long = cursor.getLong(idColumn!!)
                var name: String = cursor.getString(nameColumn!!)
                name = name.substring(0, name.lastIndexOf("."))
                val duration: Int = cursor.getInt(durationColumn!!)
                val nameArtist: String = cursor.getString(artistName!!)
                val albumId: Long = cursor.getLong(albumColumn!!)

                // uri песни и изображения
                val uri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtworkUri: Uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)

                // инициализации объектов класса Song
                val song = Song(indx, name, uri, albumArtworkUri, nameArtist, duration)
                songs += song
                indx += 1
            }
        }
    }
}