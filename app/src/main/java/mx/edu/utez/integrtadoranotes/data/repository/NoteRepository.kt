package mx.edu.utez.integrtadoranotes.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.integrtadoranotes.data.local.AppDatabase
import mx.edu.utez.integrtadoranotes.data.local.entities.NoteEntity
import mx.edu.utez.integrtadoranotes.data.local.entities.UserEntity
import mx.edu.utez.integrtadoranotes.utils.PasswordUtils

class NoteRepository(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val noteDao = database.noteDao()
    
    // ========== AUTH ==========
    
    suspend fun register(name: String, email: String, password: String): Result<UserEntity> {
        return try {
            // Verificar si el email ya existe
            if (userDao.emailExists(email) > 0) {
                Result.failure(Exception("El email ya está registrado"))
            } else {
                // Validaciones
                if (name.length < 3) {
                    return Result.failure(Exception("El nombre debe tener al menos 3 caracteres"))
                }
                if (password.length < 6) {
                    return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
                }
                
                val hashedPassword = PasswordUtils.hashPassword(password)
                val user = UserEntity(
                    name = name,
                    email = email,
                    password = hashedPassword
                )
                val userId = userDao.insertUser(user)
                val newUser = user.copy(id = userId)
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null && PasswordUtils.verifyPassword(password, user.password)) {
                Result.success(user)
            } else {
                Result.failure(Exception("Email o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }
    
    // ========== NOTES ==========
    
    fun getNotesByUser(userId: Long): Flow<List<NoteEntity>> {
        return noteDao.getNotesByUser(userId)
    }
    
    suspend fun getNoteById(noteId: String, userId: Long): NoteEntity? {
        return noteDao.getNoteById(noteId, userId)
    }
    
    suspend fun insertNote(note: NoteEntity) {
        noteDao.insertNote(note)
    }
    
    suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note)
    }
    
    suspend fun deleteNote(noteId: String, userId: Long) {
        noteDao.deleteNoteById(noteId, userId)
    }
}