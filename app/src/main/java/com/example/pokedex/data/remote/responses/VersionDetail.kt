package com.example.pokedex.data.remote.responses


import com.google.gson.annotations.SerializedName

data class VersionDetail(
    val rarity: Int,
    val version: VersionX
)