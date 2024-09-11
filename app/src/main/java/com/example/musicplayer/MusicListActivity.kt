package com.example.musicplayer


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.databinding.ActivityMusiclistBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusiclistBinding
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var adapter: MusicAdapter
    private val musicList = mutableListOf<AudioModel>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusiclistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MusicAdapter(musicList) { audioModel ->
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SELECTED_SONG_INDEX", musicList.indexOf(audioModel))
                putParcelableArrayListExtra("MUSIC_LIST", ArrayList(musicList))
            }
            startActivity(intent)
        }

        binding.recyclerViewMusic.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMusic.adapter = adapter

        // Check and request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                PERMISSION_REQUEST_CODE)
        } else {
            loadMusicFiles()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMusicFiles()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMusicFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            val audioList = getAllAudioFromDevice()
            withContext(Dispatchers.Main) {
                musicList.addAll(audioList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getAllAudioFromDevice(): List<AudioModel> {
        val audioList = mutableListOf<AudioModel>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use {
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)

            while (it.moveToNext()) {
                val title = it.getString(titleIndex)
                val data = it.getString(dataIndex)
                val artist = it.getString(artistIndex)
                audioList.add(AudioModel(title, artist, data))
            }
        }
        return audioList
    }
}
