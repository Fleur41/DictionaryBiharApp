package com.sam.dictionaryapp.presentation.screen.saved


import android.R.attr.singleLine
import android.R.attr.text
import android.content.Intent
import android.service.autofill.Validators.or
import android.widget.Toast
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sam.dictionaryapp.R
import com.sam.dictionaryapp.data.local.SavedWordEntity
import com.sam.dictionaryapp.presentation.viewmodel.SavedViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SavedScreen(
    navController: NavHostController,
    viewModel: SavedViewModel = hiltViewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val savedWords by viewModel.savedWords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState() // UPDATED: Use ViewModel's search query
    val context = LocalContext.current
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSaveWordDialog by remember { mutableStateOf(false) }
    var newWord by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) } // UPDATED: Controls search mode

    // Bottom nav selection
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            // UPDATED: New search interface similar to your image
            if (isSearchActive) {
                SearchTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery, // UPDATED: Use ViewModel method
                    onBackClick = {
                        isSearchActive = false
                        viewModel.updateSearchQuery("") // UPDATED: Clear search when going back
                    },
                    onClearSearch = {
                        viewModel.updateSearchQuery("") // UPDATED: Clear search with delete icon
                    }
                )
            } else {
                SavedWordsTopAppBar(
                    onSearchClick = { isSearchActive = true }, // UPDATED: Activate search mode
                    onDeleteAll = { showDeleteAllDialog = true },
                    onAddWord = { showSaveWordDialog = true }
                )
            }
        },
        floatingActionButton = {
            if (savedWords.isNotEmpty() && !isSearchActive) {
                ExtendedFloatingActionButton(
                    onClick = { showExportDialog = true },
                    icon = { Icon(Icons.Default.Share, contentDescription = null) },
                    text = { Text(stringResource(R.string.export)) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        },
        // UPDATED: Bottom navigation always visible (removed isSearchActive condition)
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dictionary") },
                    label = { Text("Dictionary") },
                    selected = currentRoute == "dictionary",
                    onClick = { navController.navigate("dictionary") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                    label = { Text("Saved") },
                    selected = currentRoute == "saved",
                    onClick = { /* Already on saved screen */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                savedWords.isEmpty() && !isSearchActive -> EmptySavedWordsState(onAddClick = { showSaveWordDialog = true })
                // UPDATED: Show search results when in search mode
                isSearchActive -> {
                    if (searchQuery.isNotEmpty()) {
                        FilteredWordsList(
                            words = searchResults,
                            onDelete = viewModel::deleteWord
                        )
                    } else {
                        // UPDATED: Show all words when search is active but query is empty
                        SavedWordsList(
                            words = savedWords,
                            onDelete = viewModel::deleteWord
                        )
                    }
                }
                else -> SavedWordsList(
                    words = savedWords,
                    onDelete = viewModel::deleteWord
                )
            }

            // Delete All Confirmation Dialog
            if (showDeleteAllDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAllDialog = false },
                    title = { Text(stringResource(R.string.clear_all_title)) },
                    text = { Text(stringResource(R.string.clear_all_confirmation)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.clearAllWords()
                                showDeleteAllDialog = false
                            }
                        ) {
                            Text(
                                stringResource(R.string.clear),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteAllDialog = false }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            // Export Options Dialog
            if (showExportDialog) {
                AlertDialog(
                    onDismissRequest = { showExportDialog = false },
                    title = { Text(stringResource(R.string.export_options)) },
                    text = { Text(stringResource(R.string.export_format_prompt)) },
                    confirmButton = {
                        Column {
                            TextButton(
                                onClick = {
                                    exportWords(savedWords, context, "txt")
                                    showExportDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.export_as_txt))
                            }
                            TextButton(
                                onClick = {
                                    exportWords(savedWords, context, "pdf")
                                    showExportDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.export_as_pdf))
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showExportDialog = false }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            // Add New Word Dialog
            if (showSaveWordDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveWordDialog = false },
                    title = { Text(stringResource(R.string.add_new_word)) },
                    text = {
                        OutlinedTextField(
                            value = newWord,
                            onValueChange = { newWord = it },
                            label = { Text(stringResource(R.string.word)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newWord.isNotBlank()) {
                                    viewModel.saveWord(
                                        SavedWordEntity(
                                            word = newWord,
                                            definitions = "[]",
                                            savedAt = Date()
                                        )
                                    )
                                    newWord = ""
                                    showSaveWordDialog = false
                                }
                            }
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showSaveWordDialog = false }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

// UPDATED: New search top bar component similar to your image
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearSearch: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp)
//                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // UPDATED: Enhanced back arrow button with better styling
                Surface(
                    modifier = Modifier.size(40.dp),
                    onClick = onBackClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    shadowElevation = 44.dp

                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back to Saved Screen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                // UPDATED: Back arrow button (far left as requested)
//                IconButton(
//                    onClick = onBackClick,
//                    modifier = Modifier.size(40.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
//                        contentDescription = "Back",
//                        tint = MaterialTheme.colorScheme.onSurface
//                    )
//                }

                // UPDATED: Search text field with delete icon inside
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        onSearchQueryChange(query)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = {
                        Text(
                            "Search saved words...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ){
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    // UPDATED: Delete icon inside the text field (as requested)
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                onClick = onClearSearch,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.error,
                                shadowElevation = 24.dp

                            ){
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ){
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                            }
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
    //search suggestions or recent searches
//    if (searchQuery.isEmpty()){
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = "💡 Tip: Type to search through your saved words",
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//            modifier = Modifier.padding(start = 60.dp)
//        )
//    }
}

// UPDATED: Simplified top app bar for normal mode
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedWordsTopAppBar(
    onSearchClick: () -> Unit,
    onDeleteAll: () -> Unit,
    onAddWord: () -> Unit
) {
    TopAppBar(
        title = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                Text(stringResource(R.string.saved_words))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            // UPDATED: Search click activates search mode
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
            IconButton(onClick = onAddWord) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_word)
                )
            }
            IconButton(onClick = onDeleteAll) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.clear_all),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
private fun EmptySavedWordsState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.BookmarkAdd,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.no_saved_words),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(stringResource(R.string.add_first_word))
        }
    }
}

@Composable
private fun SavedWordsList(
    words: List<SavedWordEntity>,
    onDelete: (SavedWordEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(words, key = { it.word }) { word ->
            SavedWordCard(
                word = word,
                onDelete = { onDelete(word) }
            )
        }
    }
}

@Composable
private fun FilteredWordsList(
    words: List<SavedWordEntity>,
    onDelete: (SavedWordEntity) -> Unit
) {
    if (words.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_matching_words))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(words, key = { it.word }) { word ->
                SavedWordCard(
                    word = word,
                    onDelete = { onDelete(word) }
                )
            }
        }
    }
}

@Composable
private fun SavedWordCard(
    word: SavedWordEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Saved on ${dateFormat.format(word.savedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun exportWords(
    words: List<SavedWordEntity>,
    context: android.content.Context,
    format: String
) {
    try {
        // Create a simple string with all the words and their details
        val content = buildString {
            append("My Saved Words\n\n")
            words.forEach { word ->
                append("${word.word}\n")
                append("Saved on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(word.savedAt)}\n")
                append("\n")
            }
        }
        // Create a file in cache directory
        val fileName = "saved_words_${System.currentTimeMillis()}.$format"
        val file = File(context.cacheDir, fileName)
        file.writeText(content)

        // Create share intent
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = when (format) {
                "pdf" -> "application/pdf"
                else -> "text/plain"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "My Saved Words")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        // Launch the share dialog
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error exporting words", Toast.LENGTH_SHORT).show()
    }
}

//import android.content.Intent
//import android.widget.Toast
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.core.content.FileProvider
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavHostController
//import com.sam.dictionaryapp.R
//import com.sam.dictionaryapp.data.local.SavedWordEntity
//import com.sam.dictionaryapp.presentation.viewmodel.SavedViewModel
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun SavedScreen(
//    navController: NavHostController,
//    viewModel: SavedViewModel = hiltViewModel()
//) {
//    val searchResults by viewModel.searchResults.collectAsState()
//    val savedWords by viewModel.savedWords.collectAsState()
//    val context = LocalContext.current
//    var showDeleteAllDialog by remember { mutableStateOf(false) }
//    var showExportDialog by remember { mutableStateOf(false) }
//    var searchQuery by remember { mutableStateOf("") }
//    var showSaveWordDialog by remember { mutableStateOf(false) }
//    var newWord by remember { mutableStateOf("") }
//    var isSearchActive by remember { mutableStateOf(false) }
//
//    // Bottom nav selection
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    Scaffold(
//        topBar = {
//            SavedWordsTopAppBar(
//                searchQuery = searchQuery,
//                onSearchQueryChange = { query ->
//                    searchQuery = query
//                    // Live search functionality
//                    if (query.isNotEmpty()) {
//                        viewModel.searchSavedWords(query)
//                    }
//                },
//                onSearchActiveChange = { active ->
//                    isSearchActive = active
//                    if (!active) {
//                        searchQuery = "" // Clear search when closing
//                    }
//                },
//                isSearchActive = isSearchActive,
//                onDeleteAll = { showDeleteAllDialog = true },
//                onAddWord = { showSaveWordDialog = true }
//            )
//        },
//        floatingActionButton = {
//            if (savedWords.isNotEmpty() && !isSearchActive) {
//                ExtendedFloatingActionButton(
//                    onClick = { showExportDialog = true },
//                    icon = { Icon(Icons.Default.Share, contentDescription = null) },
//                    text = { Text(stringResource(R.string.export)) },
//                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
//                )
//            }
//        },
//        bottomBar = {
//            if (!isSearchActive) {
//                NavigationBar {
//                    NavigationBarItem(
//                        icon = { Icon(Icons.Default.Home, contentDescription = "Dictionary") },
//                        label = { Text("Dictionary") },
//                        selected = currentRoute == "dictionary",
//                        onClick = { navController.navigate("dictionary") }
//                    )
//                    NavigationBarItem(
//                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
//                        label = { Text("Saved") },
//                        selected = currentRoute == "saved",
//                        onClick = { /* Already on saved screen */ }
//                    )
//                    NavigationBarItem(
//                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
//                        label = { Text("Settings") },
//                        selected = currentRoute == "settings",
//                        onClick = { navController.navigate("settings") }
//                    )
//                }
//            }
//        }
//    ) { padding ->
//        Box(modifier = Modifier.padding(padding)) {
//            when {
//                savedWords.isEmpty() -> EmptySavedWordsState(onAddClick = { showSaveWordDialog = true })
//                searchQuery.isNotEmpty() -> FilteredWordsList(
//                    words = searchResults,
////                    words = savedWords.filter {
////                        it.word.contains(searchQuery, ignoreCase = true)
////                    },
//                    onDelete = viewModel::deleteWord
//                )
//                else -> SavedWordsList(
//                    words = savedWords,
//                    onDelete = viewModel::deleteWord
//                )
//            }
//
//            // Delete All Confirmation Dialog
//            if (showDeleteAllDialog) {
//                AlertDialog(
//                    onDismissRequest = { showDeleteAllDialog = false },
//                    title = { Text(stringResource(R.string.clear_all_title)) },
//                    text = { Text(stringResource(R.string.clear_all_confirmation)) },
//                    confirmButton = {
//                        TextButton(
//                            onClick = {
//                                viewModel.clearAllWords()
//                                showDeleteAllDialog = false
//                            }
//                        ) {
//                            Text(
//                                stringResource(R.string.clear),
//                                color = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    },
//                    dismissButton = {
//                        TextButton(
//                            onClick = { showDeleteAllDialog = false }
//                        ) {
//                            Text(stringResource(R.string.cancel))
//                        }
//                    }
//                )
//            }
//
//            // Export Options Dialog
//            if (showExportDialog) {
//                AlertDialog(
//                    onDismissRequest = { showExportDialog = false },
//                    title = { Text(stringResource(R.string.export_options)) },
//                    text = { Text(stringResource(R.string.export_format_prompt)) },
//                    confirmButton = {
//                        Column {
//                            TextButton(
//                                onClick = {
//                                    exportWords(savedWords, context, "txt")
//                                    showExportDialog = false
//                                }
//                            ) {
//                                Text(stringResource(R.string.export_as_txt))
//                            }
//                            TextButton(
//                                onClick = {
//                                    exportWords(savedWords, context, "pdf")
//                                    showExportDialog = false
//                                }
//                            ) {
//                                Text(stringResource(R.string.export_as_pdf))
//                            }
//                        }
//                    },
//                    dismissButton = {
//                        TextButton(
//                            onClick = { showExportDialog = false }
//                        ) {
//                            Text(stringResource(R.string.cancel))
//                        }
//                    }
//                )
//            }
//
//            // Add New Word Dialog
//            if (showSaveWordDialog) {
//                AlertDialog(
//                    onDismissRequest = { showSaveWordDialog = false },
//                    title = { Text(stringResource(R.string.add_new_word)) },
//                    text = {
//                        OutlinedTextField(
//                            value = newWord,
//                            onValueChange = { newWord = it },
//                            label = { Text(stringResource(R.string.word)) },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    },
//                    confirmButton = {
//                        TextButton(
//                            onClick = {
//                                if (newWord.isNotBlank()) {
//                                    viewModel.saveWord(
//                                        SavedWordEntity(
//                                            word = newWord,
//                                            definitions = "[]",
//                                            savedAt = Date()
//                                        )
//                                    )
//                                    newWord = ""
//                                    showSaveWordDialog = false
//                                }
//                            }
//                        ) {
//                            Text(stringResource(R.string.save))
//                        }
//                    },
//                    dismissButton = {
//                        TextButton(
//                            onClick = { showSaveWordDialog = false }
//                        ) {
//                            Text(stringResource(R.string.cancel))
//                        }
//                    }
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun SavedWordsTopAppBar(
//    searchQuery: String,
//    onSearchQueryChange: (String) -> Unit,
//    onSearchActiveChange: (Boolean) -> Unit,
//    isSearchActive: Boolean,
//    onDeleteAll: () -> Unit,
//    onAddWord: () -> Unit
//) {
//    if (isSearchActive) {
//        SearchBar(
//            query = searchQuery,
//            onQueryChange = onSearchQueryChange,
//            onSearch = { onSearchActiveChange(false) },
//            active = true,
//            onActiveChange = onSearchActiveChange,
//            modifier = Modifier.fillMaxWidth(),
//            leadingIcon = {
//                IconButton(onClick = { onSearchActiveChange(false) }) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowBack,
//                        contentDescription = stringResource(R.string.back)
//                    )
//                }
//            },
//            placeholder = { Text("Search saved words") }
//        ) {}
//    } else {
//        TopAppBar(
//            title = {
//                Row(modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp)) {
//                    Text(stringResource(R.string.saved_words))
//                }
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
//                titleContentColor = MaterialTheme.colorScheme.primary
//            ),
//            actions = {
//                IconButton(onClick = { onSearchActiveChange(true) }) {
//                    Icon(
//                        Icons.Default.Search,
//                        contentDescription = stringResource(R.string.search)
//                    )
//                }
//                IconButton(onClick = onAddWord) {
//                    Icon(
//                        Icons.Default.Add,
//                        contentDescription = stringResource(R.string.add_word)
//                    )
//                }
//                IconButton(onClick = onDeleteAll) {
//                    Icon(
//                        Icons.Default.Delete,
//                        contentDescription = stringResource(R.string.clear_all),
//                        tint = MaterialTheme.colorScheme.error
//                    )
//                }
//            }
//        )
//    }
//}
//
//@Composable
//private fun EmptySavedWordsState(onAddClick: () -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Icon(
//            Icons.Default.BookmarkAdd,
//            contentDescription = null,
//            modifier = Modifier.size(72.dp),
//            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            stringResource(R.string.no_saved_words),
//            style = MaterialTheme.typography.headlineSmall,
//            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(
//            onClick = onAddClick,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.primaryContainer
//            )
//        ) {
//            Text(stringResource(R.string.add_first_word))
//        }
//    }
//}
//
//@Composable
//private fun SavedWordsList(
//    words: List<SavedWordEntity>,
//    onDelete: (SavedWordEntity) -> Unit
//) {
//    LazyColumn(
//        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        items(words, key = { it.word }) { word ->
//            SavedWordCard(
//                word = word,
//                onDelete = { onDelete(word) }
//            )
//        }
//    }
//}
//
//@Composable
//private fun FilteredWordsList(
//    words: List<SavedWordEntity>,
//    onDelete: (SavedWordEntity) -> Unit
//) {
//    if (words.isEmpty()) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(stringResource(R.string.no_matching_words))
//        }
//    } else {
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            items(words, key = { it.word }) { word ->
//                SavedWordCard(
//                    word = word,
//                    onDelete = { onDelete(word) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun SavedWordCard(
//    word: SavedWordEntity,
//    onDelete: () -> Unit
//) {
//    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = word.word,
//                    style = MaterialTheme.typography.titleLarge.copy(
//                        fontWeight = FontWeight.Bold
//                    )
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = "Saved on ${dateFormat.format(word.savedAt)}",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            }
//            IconButton(onClick = onDelete) {
//                Icon(
//                    Icons.Default.Delete,
//                    contentDescription = stringResource(R.string.delete),
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
//        }
//    }
//}
//
//private fun exportWords(
//    words: List<SavedWordEntity>,
//    context: android.content.Context,
//    format: String
//) {
//    try {// Create a simple string with all the words and their details
//        val content = buildString {
//            append("My Saved Words\n\n")
//            words.forEach { word ->
//                append("${word.word}\n")
//                append("Saved on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(word.savedAt)}\n")
//                append("\n")
//            }
//        }
//
//        // Create a file in cache directory
//        val fileName = "saved_words_${System.currentTimeMillis()}.$format"
//        val file = File(context.cacheDir, fileName)
//        file.writeText(content)
//
//        // Create share intent
//        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = when (format) {
//                "pdf" -> "application/pdf"
//                else -> "text/plain"
//            }
//            putExtra(Intent.EXTRA_STREAM, uri)
//            putExtra(Intent.EXTRA_SUBJECT, "My Saved Words")
//            //putExtra(Intent.EXTRA_TEXT, "Check out my saved words!")
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//        }
//        // Launch the share dialog
//        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
//    } catch (e: Exception) {
//        e.printStackTrace()
//        Toast.makeText(context, "Error exporting words", Toast.LENGTH_SHORT).show()
//    }
//
//}

