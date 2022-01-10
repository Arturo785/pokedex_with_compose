package com.example.pokedex.data.remote.responses


import com.google.gson.annotations.SerializedName

data class Item(
    val name: String,
    val url: String
)