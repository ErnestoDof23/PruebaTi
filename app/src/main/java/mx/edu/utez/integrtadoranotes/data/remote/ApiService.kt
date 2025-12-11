package mx.edu.utez.integrtadoranotes.data.remote


import mx.edu.utez.integrtadoranotes.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("notes")
    suspend fun getNotes(@Header("Authorization") token: String): Response<List<Note>>

    @GET("notes/{id}")
    suspend fun getNote(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Note>

    @POST("notes")
    suspend fun createNote(
        @Header("Authorization") token: String,
        @Body note: NoteRequest
    ): Response<Note>

    @PUT("notes/{id}")
    suspend fun updateNote(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body note: NoteRequest
    ): Response<Note>

    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}