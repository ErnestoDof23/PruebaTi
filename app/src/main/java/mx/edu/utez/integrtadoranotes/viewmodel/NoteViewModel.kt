package mx.edu.utez.integrtadoranotes.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utez.integrtadoranotes.data.model.Note
import mx.edu.utez.integrtadoranotes.data.model.NoteRequest
import mx.edu.utez.integrtadoranotes.data.remote.RetrofitInstance

class NoteViewModel : ViewModel() {

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
                currentToken?.let { token ->
                    val response = RetrofitInstance.api.getNotes("Bearer $token")
                    if (response.isSuccessful) {
                        _notes.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Error al cargar notas"
                    }
                }
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
                currentToken?.let { token ->
                    RetrofitInstance.api.createNote("Bearer $token", NoteRequest(title, content, imageUrl))
                    loadNotes()
                }
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
                currentToken?.let { token ->
                    RetrofitInstance.api.updateNote("Bearer $token", id, NoteRequest(title, content, imageUrl))
                    loadNotes()
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
            try {
                currentToken?.let { token ->
                    RetrofitInstance.api.deleteNote("Bearer $token", id)
                    loadNotes()
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
}