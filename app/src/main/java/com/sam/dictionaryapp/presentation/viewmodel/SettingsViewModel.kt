package com.sam.dictionaryapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sam.dictionaryapp.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private  val repository: SettingsRepository
) : ViewModel() {
//    val theme: StateFlow<String> = repository.theme
//    val theme: StateFlow<String> = repository.theme as StateFlow<String>
    val theme: StateFlow<String> = repository.theme.stateIn(
        scope = viewModelScope, // The CoroutineScope in which the StateFlow should be active
        started = SharingStarted.WhileSubscribed(3000), // How sharing should start/stop
        initialValue = "system"
    )

    fun setTheme(theme:String){
        viewModelScope.launch (Dispatchers.IO){
            repository.setTheme(theme)
        }
    }
}
