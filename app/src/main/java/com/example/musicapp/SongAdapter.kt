package com.example.musicapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.imageview.ShapeableImageView
import java.io.IOException

class SongAdapter(private var songs: List<Song>, private var playlist: SongPlaylist,
                  private val player: ExoPlayer, private val context: Context):
    RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    private var songsPlaylist: MutableList<SongExtensions> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.song_row_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song: Song = songs[position]
        val viewHolder = holder as SongViewHolder

        viewHolder.nameSong.text = song.nameSong
        viewHolder.nameArtist.text = song.nameArtist

        val artistImage: Uri = song.artistImage

        if(
            try {
                context.contentResolver.openInputStream(artistImage)?.use {}
                true
            }
            catch (e: IOException) {
                false
            }
        ) viewHolder.artistImage.setImageDrawable(
            Drawable.createFromStream(context.contentResolver.openInputStream(artistImage), artistImage.toString()))

        songsPlaylist += SongExtensions(
            song, viewHolder.playButton,
            false, viewHolder.favoriteButton,
            false, viewHolder.moreButton,
            viewHolder.layoutActive, playlist, player
        )
    }

    class SongViewHolder(itemView: View):
        RecyclerView.ViewHolder(itemView) {

        val artistImage: ShapeableImageView = itemView.findViewById(R.id.artistImage)
        val nameArtist: TextView = itemView.findViewById(R.id.nameArtist)
        val nameSong: TextView = itemView.findViewById(R.id.nameSong)
        val playButton: ShapeableImageView = itemView.findViewById(R.id.playButton)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
        val moreButton: ImageButton = itemView.findViewById(R.id.menuButton)
        val layoutActive: ConstraintLayout = itemView.findViewById(R.id.rowLayout)
    }

    override fun getItemCount(): Int = songs.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position


    @SuppressLint("NotifyDataSetChanged")
    fun filterSong(filteredSongs: List<Song>) {
        songs = filteredSongs
        notifyDataSetChanged()
    }
}