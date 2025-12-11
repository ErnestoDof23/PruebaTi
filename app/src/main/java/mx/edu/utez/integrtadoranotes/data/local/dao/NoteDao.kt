package mx.edu.utez.integrtadoranotes.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.integrtadoranotes.data.local.entities.NoteEntity

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotesByUser(userId: Long): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId AND userId = :userId LIMIT 1")
    suspend fun getNoteById(noteId: String, userId: Long): NoteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE id = :noteId AND userId = :userId")
    suspend fun deleteNoteById(noteId: String, userId: Long)
    
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId")
    suspend fun getNoteCount(userId: Long): Int
}

