package com.spen.placar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Banco de dados Room do aplicativo. */
@Database(
    entities = [MatchEntity::class, PlayerEntity::class, PlayerConstraintEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PlacarDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao
    abstract fun playerDao(): PlayerDao
    abstract fun constraintDao(): PlayerConstraintDao

    companion object {
        @Volatile
        private var INSTANCE: PlacarDatabase? = null

        // Migração que apenas adiciona a tabela de restrições, preservando os
        // jogadores e o histórico já cadastrados.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS player_constraints " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "aId INTEGER NOT NULL, bId INTEGER NOT NULL)"
                )
            }
        }

        fun get(context: Context): PlacarDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlacarDatabase::class.java,
                    "placar.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    // Segurança para versões não previstas.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
