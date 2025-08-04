package com.sam.dictionaryapp.data

import com.sam.dictionaryapp.data.remote.Definition
import com.sam.dictionaryapp.data.remote.DictionaryApi
import javax.inject.Inject

class DictionaryRepository @Inject constructor(
    private val api: DictionaryApi
) {
    suspend fun getMeaning(word: String): List<Definition> =
        api.getDefinition(word)
}