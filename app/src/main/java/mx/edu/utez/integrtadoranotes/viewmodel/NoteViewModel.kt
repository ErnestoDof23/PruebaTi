package mx.edu.utez.integrtadoranotes.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utez.integrtadoranotes.data.model.Note
import mx.edu.utez.integrtadoranotes.data.model.NoteRequest
import mx.edu.utez.integrtadoranotes.data.remote.RetrofitInstance
import java.util.Date
import java.util.UUID

class NoteViewModel : ViewModel() {

    companion object {
        // MODO DESARROLLO: Notas mock compartidas entre instancias
        private val mockNotes = mutableListOf(
            Note(
                id = "1",
                title = "Nota para mi novia",
                content = "Te amo princesa,",
                imageUrl = null,
                createdAt = Date(),
                updatedAt = Date(),
                userId = "dev-user"
            ),
            Note(
                id = "2",
                title = "Bienvenido a Notas App",
                content = "Esta es tu primera nota de ejemplo. Puedes editarla, eliminarla o crear nuevas notas con imágenes.",
                imageUrl = null,
                createdAt = Date(),
                updatedAt = Date(),
                userId = "dev-user"
            ),
            Note(
                id = "3",
                title = "Lista de Compras",
                content = "Leche, Pan, Huevos, Frutas, Verduras",
                imageUrl = null,
                createdAt = Date(),
                updatedAt = Date(),
                userId = "dev-user"
            ),
            Note(
                id = "4",
                title = "Ideas para Proyecto",
                content = "1. Implementar tema oscuro\n2. Agregar búsqueda avanzada\n3. Sincronización en la nube\n4. Recordatorios",
                imageUrl = null,
                createdAt = Date(),
                updatedAt = Date(),
                userId = "dev-user"
            )
        )
    }

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentToken: String? = null

    fun setToken(token: String) {
        currentToken = token
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // MODO DESARROLLO: Cargar notas mock
                kotlinx.coroutines.delay(300) // Simular latencia de red
                _notes.value = mockNotes.toList()
                
                // PRODUCCIÓN: Descomentar para usar API real
                /*
                currentToken?.let { token ->
                    val response = RetrofitInstance.api.getNotes("Bearer $token")
                    if (response.isSuccessful) {
                        _notes.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Error al cargar notas"
                    }
                }
                */
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNote(title: String, content: String, imageUrl: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // MODO DESARROLLO: Crear nota localmente
                kotlinx.coroutines.delay(200)
                val newNote = Note(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    imageUrl = imageUrl,
                    createdAt = Date(),
                    updatedAt = Date(),
                    userId = "dev-user"
                )
                mockNotes.add(0, newNote)
                loadNotes()
                
                // PRODUCCIÓN: Descomentar para usar API real
                /*
                currentToken?.let { token ->
                    RetrofitInstance.api.createNote("Bearer $token", NoteRequest(title, content, imageUrl))
                    loadNotes()
                }
                */
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(id: String, title: String, content: String, imageUrl: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // MODO DESARROLLO: Actualizar nota localmente
                kotlinx.coroutines.delay(200)
                val index = mockNotes.indexOfFirst { it.id == id }
                if (index != -1) {
                    val oldNote = mockNotes[index]
                    mockNotes[index] = oldNote.copy(
                        title = title,
                        content = content,
                        imageUrl = imageUrl,
                        updatedAt = Date()
                    )
                }
                loadNotes()
                
                // PRODUCCIÓN: Descomentar para usar API real
                /*
                currentToken?.let { token ->
                    RetrofitInstance.api.updateNote("Bearer $token", id, NoteRequest(title, content, imageUrl))
                    loadNotes()
                }
                */
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // MODO DESARROLLO: Eliminar nota localmente
                kotlinx.coroutines.delay(200)
                mockNotes.removeIf { it.id == id }
                loadNotes()
                
                // PRODUCCIÓN: Descomentar para usar API real
                /*
                currentToken?.let { token ->
                    RetrofitInstance.api.deleteNote("Bearer $token", id)
                    loadNotes()
                }
                */
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectNote(note: Note?) {
        _selectedNote.value = note
    }
}