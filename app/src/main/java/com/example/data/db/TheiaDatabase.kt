package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.TheiaDao
import com.example.data.model.LocalPattern
import com.example.data.model.TheiaLog
import com.example.data.model.TheiaVaultNote

@Database(
    entities = [TheiaVaultNote::class, LocalPattern::class, TheiaLog::class],
    version = 1,
    exportSchema = false
)
abstract class TheiaDatabase : RoomDatabase() {

    abstract fun theiaDao(): TheiaDao

    companion object {
        @Volatile
        private var INSTANCE: TheiaDatabase? = null

        fun getDatabase(context: Context): TheiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TheiaDatabase::class.java,
                    "theia_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
