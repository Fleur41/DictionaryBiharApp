package com.sam.dictionaryapp.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sam.dictionaryapp.data.local.SavedWordEntity
import com.sam.dictionaryapp.data.repository.SavedWordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: SavedWordsRepository
) : ViewModel() {
    // State for all saved words
    val savedWords = repository.getAllSavedWords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = emptyList()
        )

    // State for search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // State for search results
    private val _searchResults = MutableStateFlow<List<SavedWordEntity>>(emptyList())
    val searchResults: StateFlow<List<SavedWordEntity>> = _searchResults.asStateFlow()

    // Update search query and perform search
    fun searchSavedWords(query: String) {
        _searchQuery.value = query
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isNotEmpty()) {
                repository.searchSavedWords(query).collect { results ->
                    _searchResults.value = results
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }
    // State for search query
//    private val _searchQuery = MutableStateFlow("")
//    val searchQuery = _searchQuery.asStateFlow()
//
//    // State for search results
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val searchResults = _searchQuery.flatMapLatest { query ->
//        if (query.isBlank()){
//            repository.getAllSavedWords()
//        } else {
//            repository.searchSavedWords(query)
//        }
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(3000),
//        initialValue = emptyList()
//    )

    // Update search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Save a new word
    fun saveWord(word: SavedWordEntity) {
        viewModelScope.launch (Dispatchers.IO){
            repository.saveWord(word)
        }
    }

    // Delete a word
    fun deleteWord(word: SavedWordEntity){
        viewModelScope.launch (Dispatchers.IO){
            repository.deleteWord(word)
        }
    }

    // Clear all words
    fun clearAllWords(){
        viewModelScope.launch (Dispatchers.IO){
            repository.clearAllWords()
        }
    }
}
