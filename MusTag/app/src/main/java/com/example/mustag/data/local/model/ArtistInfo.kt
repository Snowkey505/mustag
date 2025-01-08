package com.example.mustag.data.local.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArtistInfo(
    val id: Long,
    val name: String,
    val albumsCnt: Int,
    val artwork: ByteArray?
): Parcelable