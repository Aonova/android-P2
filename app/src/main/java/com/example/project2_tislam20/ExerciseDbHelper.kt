package com.example.project2_tislam20

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExerciseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // upgrade policy: simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Exercises.db"
        const val SQL_CREATE_ENTRIES = """
            CREATE TABLE exercise (
                    _id        INTEGER PRIMARY KEY,
                    name       TEXT UNIQUE NOT NULL,
                    reps       INTEGER,
                    sets       INTEGER,
                    weights    REAL,
                    notes      TEXT)
            """
        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS exercise"
    }
}