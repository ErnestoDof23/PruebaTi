package mx.edu.utez.integrtadoranotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.edu.utez.integrtadoranotes.data.model.Note
import mx.edu.utez.integrtadoranotes.data.repository.RemoteRepository

class NoteViewModel : ViewModel() {

    private val repository = RemoteRepository()

    private val _token = MutableStateFlow<String?>(null)
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setToken(token: String) {
        println("🔑 NoteViewModel - Token configurado: ${token.take(20)}...")
        _token.value = token
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _token.value?.let { token ->
                    println("📋 Cargando notas con token...")
                    val result = repository.getNotes(token)
                    result.onSuccess { notesList ->
                        println("✅ Notas cargadas: ${notesList.size}")
                        _notes.value = notesList
                    }.onFailure { exception ->
                        println("❌ Error al cargar notas: ${exception.message}")
                        _error.value = exception.message ?: "Error al cargar notas"
                    }
                } ?: println("⚠️ No hay token para cargar notas")
            } catch (e: Exception) {
                println("❌ Excepción al cargar notas: ${e.message}")
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNote(title: String, content: String, imageUrl: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _token.value?.let { token ->
                    println("📝 Creando nota: $title")
                    println("🔑 Usando token: ${token.take(20)}...")
                    val result = repository.createNote(token, title, content, imageUrl)
                    result.onSuccess {
                        println("✅ Nota creada exitosamente")
                        loadNotes() // Recargar lista
                    }.onFailure { exception ->
                        println("❌ Error al crear nota: ${exception.message}")
                        _error.value = exception.message ?: "Error al crear nota"
                    }
                } ?: run {
                    println("⚠️ No hay token disponible para crear nota")
                    _error.value = "No estás autenticado. Inicia sesión nuevamente."
                }
            } catch (e: Exception) {
                println("❌ Excepción al crear nota: ${e.message}")
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(id: String, title: String, content: String, imageUrl: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _token.value?.let { token ->
                    val result = repository.updateNote(token, id, title, content, imageUrl)
                    result.onSuccess {
                        loadNotes() // Recargar lista
                    }.onFailure { exception ->
                        _error.value = exception.message ?: "Error al actualizar nota"
                    }
                }
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
            _error.value = null
            try {
                _token.value?.let { token ->
                    val result = repository.deleteNote(token, id)
                    result.onSuccess {
                        loadNotes() // Recargar lista
                    }.onFailure { exception ->
                        _error.value = exception.message ?: "Error al eliminar nota"
                    }
                }
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

    fun clearError() {
        _error.value = null
    }
}