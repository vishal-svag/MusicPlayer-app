package com.example.musicplayer

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.databinding.ActivityMainBinding
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var runnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var isSeekBarTracking = false
    private var currentIndex = 0
    private var musicList: ArrayList<AudioModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the music list and selected song index from the Intent
        currentIndex = intent.getIntExtra("SELECTED_SONG_INDEX", 0)
        musicList = intent.getParcelableArrayListExtra("MUSIC_LIST") ?: arrayListOf()

        if (musicList.isNotEmpty()) {
            initializeMediaPlayer(musicList[currentIndex].data)
            updateSongDetails(musicList[currentIndex])
        } else {
            binding.startTime.text = "Error: No music files"
            return
        }

        binding.playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        binding.prevButton.setOnClickListener {
            playPreviousSong()
        }

        binding.nextButton.setOnClickListener {
            playNextSong()
        }
        binding.backbtn.setOnClickListener {
            onBackPressed()
        }

        binding.waveform.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    binding.startTime.text = formatTime(mediaPlayer?.currentPosition ?: 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeekBarTracking = true
                mediaPlayer?.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeekBarTracking = false
                mediaPlayer?.start()
            }
        })

        runnable = Runnable {
            if (!isSeekBarTracking) {
                mediaPlayer?.let {
                    binding.waveform.progress = it.currentPosition
                    binding.startTime.text = formatTime(it.currentPosition)
                }
            }
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)

        mediaPlayer?.setOnCompletionListener {
            playNextSong()
        }
    }

    private fun initializeMediaPlayer(filePath: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                binding.waveform.max = duration
                binding.endTime.text = formatTime(duration)
                if (isPlaying) {
                    start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                binding.startTime.text = "Error: Unable to play file"
            }
        }
    }

    private fun updateSongDetails(audioModel: AudioModel) {
        binding.songTitle.text = audioModel.title
        binding.artistName.text = audioModel.artist
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer?.pause()
            binding.playPauseButton.setImageResource(R.drawable.play)
        } else {
            mediaPlayer?.start()
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
            handler.postDelayed(runnable, 1000)
        }
        isPlaying = !isPlaying
    }

    private fun playNextSong() {
        currentIndex = (currentIndex + 1) % musicList.size
        initializeMediaPlayer(musicList[currentIndex].data)
        updateSongDetails(musicList[currentIndex])
        mediaPlayer?.start() // Ensure the media starts playing after initialization
        binding.playPauseButton.setImageResource(R.drawable.ic_pause) // Update the button icon
        isPlaying = true
    }

    private fun playPreviousSong() {
        currentIndex = if (currentIndex - 1 < 0) musicList.size - 1 else currentIndex - 1
        initializeMediaPlayer(musicList[currentIndex].data)
        updateSongDetails(musicList[currentIndex])
        mediaPlayer?.start() // Ensure the media starts playing after initialization
        binding.playPauseButton.setImageResource(R.drawable.ic_pause) // Update the button icon
        isPlaying = true
    }


    @SuppressLint("DefaultLocale")
    private fun formatTime(milliseconds: Int): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        handler.removeCallbacks(runnable)
    }
}
