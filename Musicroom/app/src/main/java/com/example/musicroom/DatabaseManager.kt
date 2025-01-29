package com.example.musicroom

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class DatabaseManager private constructor() {

    private var database: SQLiteDatabase? = null
    private var databaseHelper: UserDatabaseHelper? = null

    fun initialize(context: Context) {
        if (databaseHelper == null) {
            databaseHelper = UserDatabaseHelper(context)
        }
    }

    fun open(): SQLiteDatabase {
        if (database == null || !database!!.isOpen) {
            database = databaseHelper?.writableDatabase
        }
        requireNotNull(database) { "Database is not initialized properly." }
        return database as SQLiteDatabase
    }

    fun close() {
        database?.close()
        database = null
    }

    companion object {
        val INSTANCE: DatabaseManager by lazy { DatabaseManager() }
    }
}
