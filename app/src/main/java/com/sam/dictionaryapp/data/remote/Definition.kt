package com.sam.dictionaryapp.data.remote

data class Definition (
    val word: String,
    val meanings: List<Meaning>
)

data class Meaning (
    val partOfSpeech: String,
    val definitions: List<MeaningDefinition>
)

data class MeaningDefinition (
    val definition: String,
    val example: String?
)