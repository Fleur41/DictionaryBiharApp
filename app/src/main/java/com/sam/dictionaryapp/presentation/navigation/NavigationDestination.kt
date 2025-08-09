package com.sam.dictionaryapp.presentation.navigation

sealed interface NavigationDestination {
    val title: String
    val route: String

    object Dictionary : NavigationDestination {
        override val title = "Dictionary"
        override val route = "dictionary"
    }

    object Saved : NavigationDestination {
        override val title = "Saved"
        override val route = "saved"
    }

    object Settings : NavigationDestination {
        override val title = "Settings"
        override val route = "settings"
    }

}