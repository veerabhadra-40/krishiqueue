package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FarmerProfileEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KrishiDatabase : RoomDatabase() {

    abstract fun krishiDao(): KrishiDao

    companion object {
        @Volatile
        private var INSTANCE: KrishiDatabase? = null

        fun getDatabase(context: Context): KrishiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KrishiDatabase::class.java,
                    "krishi_queue_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
