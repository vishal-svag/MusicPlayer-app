package com.example.musicplayer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioModel(
    val title: String,
    val artist: String,
    val data: String
) : Parcelable
