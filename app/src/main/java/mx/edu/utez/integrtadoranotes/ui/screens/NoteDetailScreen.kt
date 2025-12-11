package mx.edu.utez.integrtadoranotes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import mx.edu.utez.integrtadoranotes.utils.CameraManager
import mx.edu.utez.integrtadoranotes.utils.CameraPreview
import mx.edu.utez.integrtadoranotes.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavHostController,
    noteId: String?,
    noteViewModel: NoteViewModel
) {
    val context = LocalContext.current
    val cameraManager = remember { CameraManager(context) }
    val capturedImageUri by cameraManager.capturedImageUri.collectAsState()
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val selectedNote by noteViewModel.selectedNote.collectAsState()
    val isLoading by noteViewModel.isLoading.collectAsState()
    val error by noteViewModel.error.collectAsState()
    
    var showError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedNote) {
        if (selectedNote != null) {
            // Hay una nota seleccionada: cargar sus datos
            title = selectedNote!!.title
            content = selectedNote!!.content
            imageUri = selectedNote!!.imageUrl?.let { Uri.parse(it) }
        } else {
            // No hay nota seleccionada: limpiar formulario
            title = ""
            content = ""
            imageUri = null
        }
    }
    
    LaunchedEffect(error) {
        error?.let {
            showError = it
        }
    }

    // Limpiar o cargar nota según el modo
    LaunchedEffect(noteId) {
        if (noteId != null) {
            // Modo EDITAR: Cargar la nota seleccionada
            noteViewModel.selectNote(
                noteViewModel.notes.value.find { it.id == noteId }
            )
        } else {
            // Modo CREAR: Limpiar la nota seleccionada para empezar con formulario vacío
            noteViewModel.selectNote(null)
        }
    }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "Nueva Nota" else "Editar Nota") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (noteId != null) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    noteViewModel.deleteNote(noteId)
                                    navController.navigateUp()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                    IconButton(
                        onClick = {
                            if (title.isBlank()) {
                                showError = "El título es requerido"
                                return@IconButton
                            }
                            if (content.isBlank()) {
                                showError = "El contenido es requerido"
                                return@IconButton
                            }
                            
                            scope.launch {
                                if (noteId == null) {
                                    noteViewModel.createNote(
                                        title,
                                        content,
                                        imageUri?.toString()
                                    )
                                } else {
                                    noteViewModel.updateNote(
                                        noteId,
                                        title,
                                        content,
                                        imageUri?.toString()
                                    )
                                }
                                // Esperar un poco para que se complete la operación
                                kotlinx.coroutines.delay(500)
                                if (error == null) {
                                    navController.navigateUp()
                                }
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Guardar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 10
            )
            
            // Mostrar error si existe
            if (showError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = showError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                LaunchedEffect(showError) {
                    kotlinx.coroutines.delay(3000)
                    showError = null
                    noteViewModel.clearError()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { showCamera = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cámara")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Galería")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen de la nota",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { imageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar imagen",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            capturedImageUri?.let { uri ->
                LaunchedEffect(uri) {
                    imageUri = uri
                    showCamera = false
                }
            }

            if (showCamera) {
                AlertDialog(
                    onDismissRequest = { showCamera = false },
                    title = { Text("Tomar Foto") },
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            CameraPreview(cameraManager)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                cameraManager.takePicture()
                            }
                        ) {
                            Text("Capturar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCamera = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.cleanup()
        }
    }
}