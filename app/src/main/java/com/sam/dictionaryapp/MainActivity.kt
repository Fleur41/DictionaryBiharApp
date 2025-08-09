package com.sam.dictionaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.sam.dictionaryapp.presentation.navigation.AppNavigation
import com.sam.dictionaryapp.presentation.navigation.NavigationDestination
import com.sam.dictionaryapp.presentation.screen.DictionaryScreen
import com.sam.dictionaryapp.presentation.screen.saved.SavedScreen
import com.sam.dictionaryapp.presentation.viewmodel.SettingsViewModel
import com.sam.dictionaryapp.ui.theme.DictionaryAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DictionaryAppTheme {
//
                val navController = rememberNavController()
                val settingsViewModel = hiltViewModel<SettingsViewModel>()
                val theme by settingsViewModel.theme.collectAsState()

                DictionaryTheme(theme = theme){
                    val startDestination = NavigationDestination.Dictionary
                    AppNavigation( startDestination = startDestination)
//                    DictionaryScreen(
//                        navController = navController
//                    )
//                    dictionaryNavGraph(
//                        navController = navController
//                    )
                }
            }
        }
    }
}

@Composable
fun DictionaryTheme(
    theme: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when(theme){
        "dark" -> dynamicDarkColorScheme(context)
        "light" -> dynamicLightColorScheme(context)
        else -> if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )

}

