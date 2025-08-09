package com.sam.dictionaryapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "saved_words")
@TypeConverters(DateConverters::class)
data class SavedWordEntity (
    @PrimaryKey
    val word: String,
    val definitions: String,
    val savedAt: Date = Date()
)