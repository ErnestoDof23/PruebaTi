package mx.edu.utez.integrtadoranotes.data.repository


import mx.edu.utez.integrtadoranotes.data.model.LoginRequest
import mx.edu.utez.integrtadoranotes.data.model.NoteRequest
import mx.edu.utez.integrtadoranotes.data.remote.RetrofitInstance
import retrofit2.Response

object NoteRepository {

    suspend fun login(email: String, password: String) =
        RetrofitInstance.api.login(LoginRequest(email, password))

    suspend fun getNotes(token: String) =
        RetrofitInstance.api.getNotes("Bearer $token")

    suspend fun getNote(token: String, id: String) =
        RetrofitInstance.api.getNote("Bearer $token", id)

    suspend fun createNote(token: String, note: NoteRequest) =
        RetrofitInstance.api.createNote("Bearer $token", note)

    suspend fun updateNote(token: String, id: String, note: NoteRequest) =
        RetrofitInstance.api.updateNote("Bearer $token", id, note)

    suspend fun deleteNote(token: String, id: String) =
        RetrofitInstance.api.deleteNote("Bearer $token", id)
}