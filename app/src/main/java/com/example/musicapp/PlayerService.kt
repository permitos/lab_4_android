package com.example.musicapp

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.*
import com.google.android.exoplayer2.util.NotificationUtil.IMPORTANCE_HIGH

class PlayerService : Service() {

    private val binder = ServiceBinder() as IBinder
    lateinit var player: ExoPlayer
    lateinit var notificationManager: PlayerNotificationManager

    inner class ServiceBinder: Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        // assign variables
        player = ExoPlayer.Builder(applicationContext).build()

        // audio focus attributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()

        player.setAudioAttributes(audioAttributes, true)

        // notification manager
        val channelId = resources.getString(R.string.app_name) + " Music Channel"
        val notificationId = 1111111
        notificationManager = PlayerNotificationManager.Builder(this, notificationId, channelId)
            .setNotificationListener(notificationListener)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setChannelImportance(IMPORTANCE_HIGH)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setNextActionIconResourceId(R.drawable.ic_skip_next)
            .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .setChannelNameResourceId(R.string.app_name)
            .build()

        // set player to notification manager
        notificationManager.setPlayer(player)
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX)
        notificationManager.setUseRewindAction(false)
        notificationManager.setUseFastForwardAction(false)
    }

    override fun onDestroy() {
        // release the player
        if(player.isPlaying) player.stop()
        notificationManager.setPlayer(null)
        player.release()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    // notification listener
    private val notificationListener: NotificationListener = object : NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            stopForeground(true)
            if(player.isPlaying)  player.pause()
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            super.onNotificationPosted(notificationId, notification, ongoing)
            startForeground(notificationId, notification)
        }
    }

    // notification description adapter
    private val descriptionAdapter: MediaDescriptionAdapter = object : MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem?.mediaMetadata?.title.toString()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun createCurrentContentIntent(player: Player): PendingIntent {

            // intent to open the app when clicked
            val openAppIntent = Intent(applicationContext, MainActivity::class.java)
            return PendingIntent.getActivity(applicationContext, 0, openAppIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return null
        }

        override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap {

            // try creating an Image view on fly then get it's drawable
            val imageView = ImageView(applicationContext)
            imageView.setImageURI(player.currentMediaItem?.mediaMetadata?.artworkUri)

            // get view drawable
            return imageView.drawable.toBitmap()
        }
    }
}