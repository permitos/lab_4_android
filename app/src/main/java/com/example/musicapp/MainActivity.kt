package com.example.musicapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.imageview.ShapeableImageView
import java.util.*

class MainActivity : AppCompatActivity() {
    // основные объекты инициализации
    private var allSongs: List<Song> = listOf()
    private var playlist: SongPlaylist = SongPlaylist(null)

    // вспомогательные объекты представления
    // списка песен на основе RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var recyclerView: RecyclerView

    // объект "плеера" песен
    private lateinit var player: ExoPlayer

    // кнопка перехода к меню управления плеером
    private lateinit var playerButton: ImageButton

    // представление управления плеером песен
    private lateinit var playerView: ConstraintLayout

    // кнопка перехода к выбору песен
    private lateinit var playerCloseBtn: ImageButton

    // изображение к песне
    private lateinit var artistImage: ShapeableImageView

    // имя исполнителя и название песни
    private lateinit var nameSongPlayer: TextView
    private lateinit var nameArtistPlayer: TextView

    // кнопки управления плеером
    private lateinit var repeatPlayerBtn: ImageButton
    private lateinit var skipPreviousPlayerBtn: ImageButton
    private lateinit var playButton: ShapeableImageView
    private lateinit var skipNextPlayerBtn: ImageButton

    // слайдер воспроизведения песни
    private lateinit var seekbar: SeekBar

    // время исполнения песни и ее продолжительности
    private lateinit var progressTime: TextView
    private lateinit var durationTime: TextView

    // статус "повтора" песниы
    private var repeatStatus = false

    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // инциализация используемого RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        // инициализация объекта "плеер"
        player = ExoPlayer.Builder(this).build()

        // загрузка песен
        fetchSongs()

        // загрузка toolbar в приложение
        val toolbar: Toolbar = findViewById(R.id.toolbarRight)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // инициализация объектов управления плеером
        playerView =  findViewById(R.id.playerView)
        nameSongPlayer = findViewById(R.id.nameSongPlayer)
        progressTime = findViewById(R.id.progressTime)
        durationTime = findViewById(R.id.durationTime)
        playButton = findViewById(R.id.playButton)
        artistImage = findViewById(R.id.artistImagePlayer)
        seekbar = findViewById(R.id.seekbar)
        nameArtistPlayer = findViewById(R.id.nameArtistPlayer)
        skipPreviousPlayerBtn = findViewById(R.id.skipPreviousPlayer)
        skipNextPlayerBtn = findViewById(R.id.skipNextPlayer)
        repeatPlayerBtn = findViewById(R.id.repeatPlayer)
        playerCloseBtn = findViewById(R.id.playerBack)

        // иницализация кнопки перехода к меню управления плеером
        playerButton = findViewById(R.id.buttonBack)

        // bind to the player service, and do every thing after the binding
        doBindService()

        // определение действий кнопок и обработка событий других,
        // используемых в плеере объектов
        playerControls()
    }

    private fun doBindService() {
        val playerServiceIntent = Intent(this, PlayerService::class.java)
        bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startServiceFun() {
        startService(Intent(this, PlayerService::class.java))
    }

    private val playerServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // get the service instance
            val binder = service as PlayerService.ServiceBinder
            binder.getService().player = player
            isBound = true
            startServiceFun()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    private fun playerControls() {
        // предоставление пользователю окна управления плеером
        playerButton.setOnClickListener {
            playerView.visibility = View.VISIBLE
        }

        // предоставление пользователю окна выбора действия с песнями
        playerCloseBtn.setOnClickListener {
            playerView.visibility = View.GONE
        }

        // переключение на предыдущую песню
        skipPreviousPlayerBtn.setOnClickListener {
            if(player.hasPreviousMediaItem()) {
                player.seekToPrevious()
                player.play()
            }
        }

        // переключение на следующую песню
        skipNextPlayerBtn.setOnClickListener {
            if(player.hasNextMediaItem()) {
                player.seekToNext()
                player.play()
            }
        }

        // переключение в режим воспроизведения/приостановления песни
        playButton.setOnClickListener {
            if(player.isPlaying) {
                player.pause()
                playButton.setBackgroundResource(R.drawable.active_play)
            }
            else {
                player.play()
                playButton.setBackgroundResource(R.drawable.active_pause)
            }
        }

        // предоставление пользователю возможности перемотки на нужный момент песни
        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            var progressValue = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressValue = seekBar!!.progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(player.playbackState == ExoPlayer.STATE_READY){
                    seekBar!!.progress = progressValue
                    progressTime.text = getReadableTime(progressValue)
                    player.seekTo(progressValue.toLong())
                }
            }
        })

        // включение/отключение режима "повтора" песни
        repeatPlayerBtn.setOnClickListener {
            if(repeatStatus) {
                player.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                repeatPlayerBtn.setImageResource(R.drawable.ic_repeat)
            }
            else {
                player.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                repeatPlayerBtn.setImageResource(R.drawable.ic_active_repeat)
            }

            repeatStatus = !repeatStatus
        }

        // обработка событий "перехода" медиафайла и
        // изменения состояния воспроизведения
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                super.onMediaItemTransition(item, reason)
                assert(item != null)

                nameSongPlayer.text = item!!.mediaMetadata.title
                nameArtistPlayer.text = item.mediaMetadata.artist

                setInfoSeekbar()
                playButton.setBackgroundResource(R.drawable.active_pause)

                showCurrentArtwork()
                updatePlayerPositionProgress()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == ExoPlayer.STATE_READY) {
                    nameSongPlayer.text = Objects.requireNonNull(player.
                    currentMediaItem?.
                    mediaMetadata?.title)

                    setInfoSeekbar()
                    playButton.setBackgroundResource(R.drawable.active_pause)

                    showCurrentArtwork()
                    updatePlayerPositionProgress()
                }
                else {
                    playButton.setBackgroundResource(R.drawable.active_play)
                }
            }
        })
    }

    // предоставления текущего изображения песни
    fun showCurrentArtwork() {
        artistImage.setImageURI(player.currentMediaItem
                ?.mediaMetadata
                ?.artworkUri
        )
    }

    // установка параметров слайдера
    fun setInfoSeekbar() {
        progressTime.text = getReadableTime(player.currentPosition.toInt())
        seekbar.progress = player.currentPosition.toInt()
        seekbar.max = player.duration.toInt()
        durationTime.text = getReadableTime(player.duration.toInt())
    }

    // обновления положения "ползунка" слайдера
    fun updatePlayerPositionProgress() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(player.isPlaying) {
                    progressTime.text = getReadableTime(player.currentPosition.toInt())
                    seekbar.progress = player.currentPosition.toInt()
                }
                updatePlayerPositionProgress()
            }, 1000)
    }

    // конвертирование значения воспроизведения в "нормальный" вид
    fun getReadableTime(duration: Int): String {
        val hrs = duration / (1000 * 60 * 60)
        val min = (duration % (1000 * 60 * 60)) / (1000 * 60)
        val secs = (((duration % (1000 * 60 * 60)) % (1000 * 60 * 60)) % (1000 * 60)) / 1000

        return  if(hrs < 1) "$min:$secs" else "$hrs:$min:$secs"
    }

    // загрузка песен и дальнейшее их представление в RecyclerView
    private fun fetchSongs() {
        DataLoader.loadSong(contentResolver)
        showSongs(DataLoader.songs)
    }

    // представление списка песен пользователю
    private fun showSongs(songs: List<Song>) {
        if(songs.isEmpty()) {
            Toast.makeText(this, "No Songs", Toast.LENGTH_SHORT).show()
            return
        }

        allSongs = songs
        MediaLoader.loadMediaItems(allSongs)

        songAdapter = SongAdapter(allSongs, playlist, player,this)
        recyclerView.adapter = songAdapter
    }

    // предоставление списка песен пользователю по запросу
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_btn, menu)

        val menuItem = menu?.findItem(R.id.searchBtn)
        val searchView = menuItem?.actionView as SearchView

        searchSong(searchView)
        return super.onCreateOptionsMenu(menu)
    }

    // поиск списка песен по запросу
    private fun searchSong(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.lowercase(Locale.getDefault())?.let { filterSong(it) }
                return true
            }
        })
    }

    // загрузка только тех песен, которые удовлетворяют запросу
    private fun filterSong(query: String) {
        val filteredSongs = mutableListOf<Song>()

        if(allSongs.isNotEmpty()) {
            for(song in allSongs) {
                if(song.nameSong.lowercase(Locale.getDefault()).contains(query))
                    filteredSongs += song
            }

            songAdapter.filterSong(filteredSongs)
        }
    }
}