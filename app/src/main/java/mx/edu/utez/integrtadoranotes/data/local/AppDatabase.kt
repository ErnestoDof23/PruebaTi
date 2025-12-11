package mx.edu.utez.integrtadoranotes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.edu.utez.integrtadoranotes.data.local.dao.NoteDao
import mx.edu.utez.integrtadoranotes.data.local.dao.UserDao
import mx.edu.utez.integrtadoranotes.data.local.entities.NoteEntity
import mx.edu.utez.integrtadoranotes.data.local.entities.UserEntity

@Database(
    entities = [UserEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

