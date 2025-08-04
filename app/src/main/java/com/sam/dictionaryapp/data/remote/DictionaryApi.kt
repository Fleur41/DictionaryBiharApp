package com.sam.dictionaryapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("/api/v2/entries/en/{word}")
    suspend fun getDefinition(@Path("word") word: String): List<Definition>
}