package com.sam.dictionaryapp.data.repository

import com.sam.dictionaryapp.data.local.SavedWordDao
import com.sam.dictionaryapp.data.local.SavedWordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SavedWordsRepository @Inject constructor(
    private val dao: SavedWordDao
) {
    fun getAllSavedWords(): Flow<List<SavedWordEntity>> {
        return dao.getAllSavedWords()
    }

    suspend fun saveWord(word: SavedWordEntity) {
        return dao.saveWord(word)
    }

    suspend fun deleteWord(word: SavedWordEntity){
        return dao.deleteWord(word)
    }

    suspend fun clearAllWords(){
        return dao.clearAllWords()
    }

    fun searchSavedWords(query: String): Flow<List<SavedWordEntity>> {
        return dao.searchSavedWords(query)
    }

}