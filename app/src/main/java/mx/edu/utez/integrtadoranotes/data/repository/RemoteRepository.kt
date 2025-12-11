package mx.edu.utez.integrtadoranotes.data.repository

import mx.edu.utez.integrtadoranotes.data.model.*
import mx.edu.utez.integrtadoranotes.data.remote.RetrofitInstance

class RemoteRepository {
    
    private val api = RetrofitInstance.api
    
    // ========== AUTENTICACIÓN ==========
    
    suspend fun register(name: String, email: String, password: String): Result<Pair<String, User>> {
        return try {
            val response = api.register(RegisterRequest(name, email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(Pair(body.token, body.user))
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "El email ya está registrado"
                    400 -> "Datos inválidos. Verifica nombre, email y contraseña"
                    else -> "Error al registrar usuario"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    
    suspend fun login(email: String, password: String): Result<Pair<String, User>> {
        return try {
            val response = api.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(Pair(body.token, body.user))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Email o contraseña incorrectos"
                    else -> "Error al iniciar sesión"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    
    // ========== NOTAS ==========
    
    suspend fun getNotes(token: String): Result<List<Note>> {
        return try {
            val response = api.getNotes("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener notas"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    
    suspend fun createNote(token: String, title: String, content: String, imageUrl: String?): Result<Note> {
        return try {
            val noteRequest = NoteRequest(title, content, imageUrl)
            val response = api.createNote("Bearer $token", noteRequest)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear nota"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    
    suspend fun updateNote(token: String, noteId: String, title: String, content: String, imageUrl: String?): Result<Note> {
        return try {
            val noteRequest = NoteRequest(title, content, imageUrl)
            val response = api.updateNote("Bearer $token", noteId, noteRequest)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar nota"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    
    suspend fun deleteNote(token: String, noteId: String): Result<Boolean> {
        return try {
            val response = api.deleteNote("Bearer $token", noteId)
            
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar nota"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}

