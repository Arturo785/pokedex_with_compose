package com.example.pokedex.di

import com.example.pokedex.data.PokeApi
import com.example.pokedex.repository.PokemonRepository
import com.example.pokedex.repository.PokemonRepositoryImpl
import com.example.pokedex.util.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesRetrofitInstance(): PokeApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(PokeApi::class.java)
    }

    @Singleton
    @Provides
    fun providesRepository(api: PokeApi): PokemonRepository {
        return PokemonRepositoryImpl(api) as PokemonRepository
    }

}