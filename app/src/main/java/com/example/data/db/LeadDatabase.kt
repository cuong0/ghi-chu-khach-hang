package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Lead

@Database(entities = [Lead::class], version = 1, exportSchema = false)
abstract class LeadDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao

    companion object {
        @Volatile
        private var INSTANCE: LeadDatabase? = null

        fun getDatabase(context: Context): LeadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LeadDatabase::class.java,
                    "lead_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
