package mx.edu.utez.integrtadoranotes.data.model

import java.util.Date

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val userId: String = ""
)

data class NoteRequest(
    val title: String,
    val content: String,
    val imageUrl: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: User
)

data class User(
    val id: String,
    val name: String,
    val email: String
)