package com.sam.dictionaryapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWord(word: SavedWordEntity)

    @Query("SELECT * FROM saved_words ORDER BY savedAt DESC")
    fun getAllSavedWords(): Flow<List<SavedWordEntity>>

    @Delete
    suspend fun deleteWord(word: SavedWordEntity)

    @Query("DELETE FROM saved_words")
    suspend fun clearAllWords()

    @Query("SELECT * FROM saved_words WHERE word LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchSavedWords(query: String): Flow<List<SavedWordEntity>>
}