package mx.edu.utez.integrtadoranotes.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import mx.edu.utez.integrtadoranotes.viewmodel.AuthViewModel
import mx.edu.utez.integrtadoranotes.viewmodel.NoteViewModel

@Composable
fun NoteListScreen(
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = viewModel()
    val noteViewModel: NoteViewModel = viewModel()

    val notes by noteViewModel.notes.collectAsState()
    val isLoading by noteViewModel.isLoading.collectAsState()
    val token by authViewModel.token.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        token?.let {
            noteViewModel.setToken(it)
            noteViewModel.loadNotes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Notas") },
                actions = {
                    IconButton(onClick = { /* Buscar */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("note/edit") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva nota")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.content.contains(searchQuery, ignoreCase = true)
                    }) { note ->
                        NoteItem(
                            note = note,
                            onClick = {
                                navController.navigate("note/detail/${note.id}")
                            },
                            onDelete = {
                                noteViewModel.deleteNote(note.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: mx.edu.utez.integrtadoranotes.data.model.Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Actualizado: ${note.updatedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}