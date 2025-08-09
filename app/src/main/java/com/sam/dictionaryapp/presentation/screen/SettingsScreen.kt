package com.sam.dictionaryapp.presentation.screen

import android.R.attr.theme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.sam.dictionaryapp.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sam.dictionaryapp.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeState by viewModel.theme.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scrollState = rememberScrollState()
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
//            CenterAlignedTopAppBar()
//                title = {Text(stringResource(R.string.settings))},
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer

        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dictionary") },
                    label = {Text(text = "Home")},
                    selected = currentRoute == "home",
                    onClick = {navController.navigate("dictionary")}
                )

                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Saved") },
                    label = {Text(text = "Saved")},
                    selected = currentRoute == "saved",
                    onClick = {navController.navigate("saved")}
                )

                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                    label = {Text(text = "Settings")},
                    selected = currentRoute == "settings",
                    onClick = {navController.navigate("settings")}
                )
            }

        }

    ){ innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ){
            // Theme Selection Section
            SettingsSection(tittle = stringResource(R.string.app_theme)){
                Column (
                    modifier = Modifier.selectableGroup()
                ){
                   listOf("Light", "Dark", "System").forEach { theme ->
                       Row (
                           modifier = Modifier
                               .fillMaxWidth()
                               .selectable(
                                   selected = (theme.lowercase() == themeState),
                                   enabled = true, //it can be removed
                                   role = Role.RadioButton,
                                   onClick = {viewModel.setTheme(theme.lowercase())},
                               )
                               .padding(vertical = 8.dp),
                           verticalAlignment = Alignment.CenterVertically
                       ){
                           RadioButton(
                               selected = (theme.lowercase() == themeState),
                               onClick = null
                           )
                           Spacer(modifier = Modifier.width(16.dp))
                           Text(
                               modifier = Modifier.weight(1f),
                               text = theme,
                               style = MaterialTheme.typography.bodyLarge
                           )

                       }
                   }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Notification Preferences Section
            SettingsSection(tittle = stringResource(R.string.notifications)) {
                var notificationsEnabled by remember { mutableStateOf(true) }
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {notificationsEnabled = it}
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "Enable notifications",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(tittle = stringResource(R.string.about)) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "Dictionary App\nVersion 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "Developed by Sam\n© 2025 Your's Company",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 1f)
                )
//                Spacer(modifier = Modifier.height(2.dp))
                Image(
                    modifier = Modifier.align(Alignment.Start).size(60.dp),
                    painter = painterResource(id = R.drawable.outline_dictionary_24),
                    contentDescription = "App Logo"
                )
            }
        }

    }
}

@Composable
private fun SettingsSection(
    tittle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = tittle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Surface (
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface //It can be removed
        ){
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                content()
            }
        }
    }

}

//@Preview
//@Composable
//private fun SettingsSectionPreview() {
//    SettingsSection(
//        tittle = "App Theme",
//        content = {}
//    )
//}




//@Composable
//fun SettingsScreen(
//    viewModel: SettingsViewModel = hiltViewModel(),
//) {
//    val themeState by viewModel.theme.collectAsState()
//    var selectedTheme by remember { mutableStateOf(themeState) }
//    Column (
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ){
//        Text(
//            text = "App Theme",
//            style = MaterialTheme.typography.titleLarge
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        listOf("Light", "Dark", "System").forEach { theme ->
//            Row (
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .selectable(
//                        selected = (theme == selectedTheme),
//                        onClick = {
//                            selectedTheme = theme
//                            viewModel.setTheme(theme.lowercase())
//                        }
//                    )
//                    .padding(16.dp)
//            ){
//                RadioButton(
//                    selected = (theme == selectedTheme),
//                    onClick = {
//                        selectedTheme = theme
//                        viewModel.setTheme(theme.lowercase())
//                    }
//                )
//                Text(
//                    text = theme,
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(start = 16.dp)
//
//                )
//            }
//        }
//    }
//}