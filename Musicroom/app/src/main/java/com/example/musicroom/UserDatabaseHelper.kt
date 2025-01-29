package com.example.musicroom

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class UserDatabaseHelper(private val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "musicroom.db"
        private const val DATABASE_VERSION = 4
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Ostaviti prazno ako se bazu kreira iz fajla.
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Ostaviti prazno ako se upgrade obavlja drugačije.
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Ostaviti prazno za downgrade logiku.
    }

    fun copyDatabase() {
        val databasePath: File = context.getDatabasePath(DATABASE_NAME)

        if (!databasePath.exists()) {
            context.assets.open(DATABASE_NAME).use { inputStream ->
                FileOutputStream(databasePath).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}
