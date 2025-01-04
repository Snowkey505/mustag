package com.example.mustag.data.local.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlbumInfo(
    val id: Long,
    val title: String,
    val year: Int,
    val artist: String,
    val artwork: ByteArray?
): Parcelable