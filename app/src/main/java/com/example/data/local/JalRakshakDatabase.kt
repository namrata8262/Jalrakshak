package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecommendationEntity::class], version = 1, exportSchema = false)
abstract class JalRakshakDatabase : RoomDatabase() {
    abstract fun recommendationDao(): RecommendationDao

    companion object {
        @Volatile
        private var INSTANCE: JalRakshakDatabase? = null

        fun getDatabase(context: Context): JalRakshakDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JalRakshakDatabase::class.java,
                    "jalrakshak_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
