package mx.edu.utez.integrtadoranotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utez.integrtadoranotes.data.model.User
import mx.edu.utez.integrtadoranotes.data.repository.RemoteRepository

class AuthViewModel : ViewModel() {

    private val repository = RemoteRepository()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                println("🔐 Intentando login: $email")
                val result = repository.login(email, password)
                result.onSuccess { (token, user) ->
                    println("✅ Login exitoso - Token: ${token.take(20)}...")
                    _token.value = token
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }.onFailure { exception ->
                    println("❌ Login fallido: ${exception.message}")
                    _error.value = exception.message ?: "Error al iniciar sesión"
                }
            } catch (e: Exception) {
                println("❌ Excepción en login: ${e.message}")
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                println("📝 Intentando registro: $email")
                val result = repository.register(name, email, password)
                result.onSuccess { (token, user) ->
                    println("✅ Registro exitoso - Token: ${token.take(20)}...")
                    _token.value = token
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }.onFailure { exception ->
                    println("❌ Registro fallido: ${exception.message}")
                    _error.value = exception.message ?: "Error al registrarse"
                }
            } catch (e: Exception) {
                println("❌ Excepción en registro: ${e.message}")
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _token.value = null
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun clearError() {
        _error.value = null
    }
}