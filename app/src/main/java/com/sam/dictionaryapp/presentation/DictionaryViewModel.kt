package com.sam.dictionaryapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sam.dictionaryapp.data.DictionaryRepository
import com.sam.dictionaryapp.data.remote.Definition
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.delay

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val repository: DictionaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<DictionaryState>(DictionaryState.Initial)
    val state: StateFlow<DictionaryState> = _state.asStateFlow()
    private var currentSearchJob = viewModelScope.launch(Dispatchers.IO) {  }

    fun search(word: String){
        currentSearchJob.cancel()
        currentSearchJob = viewModelScope.launch (Dispatchers.IO){
            if (word.isBlank()){
                _state.value = DictionaryState.Initial
                return@launch
            }
            delay(300)
            //Handle loading state
            try{
                _state.value = DictionaryState.Loading
                //
                val definitions = repository.getMeaning(word)
                _state.value = DictionaryState.Success(definitions)
            } catch (e: Exception) {

                //Handle error state
                _state.value = DictionaryState.Error(e.message ?: "Failed to fetch definition")
            }
        }
    }

    sealed interface DictionaryState{
        object Initial : DictionaryState
        object Loading : DictionaryState
        data class Success(val definitions: List<Definition>) : DictionaryState
        data class Error(val message: String) : DictionaryState
    }

}
