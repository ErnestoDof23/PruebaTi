package mx.edu.utez.integrtadoranotes.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.utez.integrtadoranotes.data.model.LoginRequest
import mx.edu.utez.integrtadoranotes.data.remote.RetrofitInstance

class AuthViewModel : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // MODO DESARROLLO: Login automático sin backend
                kotlinx.coroutines.delay(500) // Simular carga
                _token.value = "dev-token-123456"
                _isLoggedIn.value = true
                
                // PRODUCCIÓN: Descomentar para usar API real
                /*
                val response = RetrofitInstance.api.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        _token.value = it.token
                        _isLoggedIn.value = true
                    }
                } else {
                    _error.value = "Error de autenticación"
                }
                */
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _token.value = null
        _isLoggedIn.value = false
    }
}