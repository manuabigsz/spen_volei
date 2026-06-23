package com.spen.placar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Banco de dados Room do aplicativo. */
@Database(entities = [MatchEntity::class], version = 1, exportSchema = false)
abstract class PlacarDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: PlacarDatabase? = null

        fun get(context: Context): PlacarDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlacarDatabase::class.java,
                    "placar.db"
                ).build().also { INSTANCE = it }
            }
    }
}
