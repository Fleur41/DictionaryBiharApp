package com.sam.dictionaryapp.presentation.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sam.dictionaryapp.presentation.screen.DictionaryScreen
import com.sam.dictionaryapp.presentation.screen.SettingsScreen
import com.sam.dictionaryapp.presentation.screen.saved.SavedScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    startDestination: NavigationDestination
) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination.route
    ){
        composable(
            NavigationDestination.Dictionary.route
        ){
            DictionaryScreen(navController)
        }

        composable(
            NavigationDestination.Saved.route
        ){
            SavedScreen(navController)
        }

        composable(
            NavigationDestination.Settings.route
        ){
            SettingsScreen(navController)
        }
    }
}