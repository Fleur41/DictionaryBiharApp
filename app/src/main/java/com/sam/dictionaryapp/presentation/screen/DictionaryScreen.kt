package com.sam.dictionaryapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sam.dictionaryapp.R
import com.sam.dictionaryapp.data.model.Definition
import com.sam.dictionaryapp.presentation.viewmodel.DictionaryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    navController : NavHostController,
    viewModel: DictionaryViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
//                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Dictionary App \uD83D\uDCD6",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 24.sp
                            )

                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    },
                    label = {
                        Text(text = "Home")
                    },
                    selected = true,
                    onClick = { navController.navigate("dictionary") }
                )
                NavigationBarItem(
                    icon = {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription ="Saved")
//                        Icon(imageVector = Icons.Default.CollectionsBookmark, contentDescription = null)
                    },
                    label = {
                        Text(text = "Saved")
                    },
                    selected = false,
                    onClick = { navController.navigate("saved") } ,
//                    onClick = { navController.navigate("dictionary") }

                )
                NavigationBarItem(
                    icon = {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    },
                    label = {
                        Text(text = "Settings")
                    },
                    selected = false,
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(8.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = { search ->
                    searchQuery = search
                    viewModel.search(search)
                },
                label = { Text("Search for a word") },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Search",
                        modifier = Modifier.clickable{
                            if (searchQuery.isNotBlank()){
                                viewModel.search(searchQuery)
                                focusManager.clearFocus()
                            }
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()){
                            viewModel.search(searchQuery)
                            focusManager.clearFocus()
                        }
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            when(state){
                is DictionaryViewModel.DictionaryState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "Search for a word",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
//                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
                is DictionaryViewModel.DictionaryState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }

                }
                is DictionaryViewModel.DictionaryState.Success -> {
                    val definitions = (state as DictionaryViewModel.DictionaryState.Success).definitions
                    if (definitions.isEmpty()){
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            Text(text = "No definitions found")
                        }
                    } else {
                        LazyColumn (
                            modifier = Modifier.fillMaxSize()
                        ){
                           items(definitions){definition ->
                               MeaningItem(definition)
                               Spacer(modifier = Modifier.height(8.dp))
                           }
                        }

                    }

                }
                is DictionaryViewModel.DictionaryState.Error -> {
                    val error = (state as DictionaryViewModel.DictionaryState.Error).message
                    Box (
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun MeaningItem(definition: Definition){
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
       Column (
           modifier = Modifier
               .fillMaxWidth()
               .padding(12.dp)
       ){
           Text(
               text = definition.word,
               style = MaterialTheme.typography.headlineMedium,
               color = MaterialTheme.colorScheme.primary
           )
           Spacer(modifier = Modifier.height(8.dp))
           definition.meanings.forEach { meaning ->
               val color = when(meaning.partOfSpeech.lowercase()){
                   "noun" -> Color(0xFFE3F2FD)
                   "verb" -> Color(0xFFE8F5E9)
                   "adjective" -> Color(0xFFFFF3E0)
                   else -> Color(0xFFF3E5F5)
               }
               Column (
                   modifier = Modifier
                       .fillMaxWidth()
                       .background(color)
                       .padding(8.dp)
               ){
                   Text(
                       text = meaning.partOfSpeech,
                       style = MaterialTheme.typography.titleMedium,
                       color = Color.Red
                   )
                   meaning.definitions.forEach { def ->
                       Text(
                           text = "- ${def.definition}",
                           style = MaterialTheme.typography.bodyMedium,
                           modifier = Modifier.padding(vertical = 4.dp)
                       )

                       def.example?.let{ example ->
                           Text(
                               text = "Example: $example",
                               style = MaterialTheme.typography.bodySmall,
                               color = MaterialTheme.colorScheme.onSurface.copy(.7f),
                               modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                           )
                       }
                   }
               }
               Spacer(modifier = Modifier.height(8.dp))
           }
       }
    }
}
