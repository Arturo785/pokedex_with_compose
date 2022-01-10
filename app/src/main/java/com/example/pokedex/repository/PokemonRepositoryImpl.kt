package com.example.pokedex.repository

import com.example.pokedex.data.PokeApi
import com.example.pokedex.data.remote.responses.PokemonList
import com.example.pokedex.data.remote.responses.PokemonResponse
import com.example.pokedex.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject


@ActivityScoped
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApi
) : PokemonRepository {


    override suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonList> {
        val response = try {
            api.getPokemonList(
                limit,
                offset
            )
        } catch (ex: Exception) {
            return Resource.Error(ex.message ?: "Something went wrong")
        }

        return Resource.Success(response)
    }

    override suspend fun getPokemonInfo(pokemonName: String): Resource<PokemonResponse> {
        val response = try {
            api.getPokemonInfo(pokemonName)
        } catch (ex: Exception) {
            return Resource.Error(ex.message ?: "Something went wrong")
        }

        return Resource.Success(response)
    }
}