package com.example.mustag.data.local.model

import android.net.Uri
import android.os.Parcelable
import com.example.mustag.data.db.Artist
import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(
    val uri: Uri,
    val displayName: String,
    val id: Long,
    val artistNames: List<String>,
    val data: String,
    val duration: Int,
    val title: String,
    val album: String,
):Parcelable
