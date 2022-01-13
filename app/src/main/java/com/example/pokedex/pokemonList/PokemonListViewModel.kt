package com.example.pokedex.pokemonList

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.pokedex.data.models.PokedexListEntry
import com.example.pokedex.repository.PokemonRepository
import com.example.pokedex.util.Constants.PAGE_SIZE
import com.example.pokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private var curPage = 0

    var pokemonList = mutableStateOf<List<PokedexListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    private var cachedPokemonList = listOf<PokedexListEntry>()
    private var isSearchStarting = true
    var isSearching = mutableStateOf(false)

    init {
        loadPokemonPaginated()
    }

    fun searchPokemonList(query: String) {

        val listToSearch = if (isSearchStarting) {
            pokemonList.value
        } else {
            cachedPokemonList
        }

        // this dispatcher because starts tp get into heavy lifting operation
        viewModelScope.launch(Dispatchers.Default) {
            // when the user cleans the query
            if (query.isEmpty()) {
                // returns the list to all pokemons
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStarting = true
                return@launch
            }

            // by name or number
            val results = listToSearch.filter {
                it.pokemonName.contains(query.trim(), ignoreCase = true) ||
                        it.number.toString() == query.trim()
            }

            if (isSearchStarting) {
                cachedPokemonList = pokemonList.value
                isSearchStarting = false
            }

            // this list is the one used by the lazy column
            pokemonList.value = results
            isSearching.value = true
        }
    }


    fun loadPokemonPaginated() {
        viewModelScope.launch {

            isLoading.value = true
            // from where to start depending of the page
            val result = repository.getPokemonList(PAGE_SIZE, curPage * PAGE_SIZE)

            when (result) {
                is Resource.Success -> {
                    // checks when they are no more left pokemons
                    endReached.value = curPage * PAGE_SIZE >= result.data!!.count


                    // creates card entries from the result we received
                    val pokedexEntries = result.data.results.mapIndexed { index, entry ->
                        // response looks like this
                        // "url":"https://pokeapi.co/api/v2/pokemon/3/"}
                        val number = if (entry.url.endsWith("/")) {
                            entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                        } else {
                            entry.url.takeLastWhile { it.isDigit() }
                        }

                        //to get the image from the pokemon
                        val urlImage =
                            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"

                        // creates an object of our entry and capitalizes it
                        PokedexListEntry(entry.name.replaceFirstChar {
                            it.titlecase(
                                Locale.ROOT
                            )
                        }, urlImage, number.toInt())
                    }

                    // because we already received the next round of pokemons
                    curPage++

                    loadError.value = ""
                    isLoading.value = false

                    // append to not erase the old ones
                    pokemonList.value += pokedexEntries
                }

                is Resource.Error -> {
                    loadError.value = result.message ?: "Something went wrong"
                    isLoading.value = false
                }
            }
        }
    }


    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // lets us extract the dominant color from the passed bitmap
        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }
}