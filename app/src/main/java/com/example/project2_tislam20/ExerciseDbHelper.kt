package com.example.project2_tislam20

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlin.random.Random

class ExerciseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)

        fun insertDefault(db: SQLiteDatabase) { // generates and inserts the following default exercises
            val exercises = arrayOf("Situps","Pushups","Bicep Curls")
            for (exercise in exercises) {
                val cv = ContentValues()
                with (cv) {
                    put(COL.NAME,exercise);
                    put(COL.REPS, Random.nextInt(2,20)*5)
                    put(COL.SETS, Random.nextInt(1,5))
                    put(COL.WEIGHTS, Random.nextDouble(5.0,100.0))
                    put(COL.NOTES,"Default generated exercise")
                }
                try {
                    db.insertOrThrow(TABLE_EXERCISES,null,cv)
                } catch (e : SQLiteException) {
                    Log.e(TAG,"Insert default items failed: ${e.message}")
                }
            }
        }
        insertDefault(db)
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
        const val TAG = "Project2-tislam20:ExeciseDbHandler"
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Exercises.db"
        const val TABLE_EXERCISES = "exercise"
        object COL { // contains all the column names for the exercise table
            const val ID = "_id"
            const val NAME = "name"
            const val REPS = "reps"
            const val SETS = "sets"
            const val WEIGHTS = "weights"
            const val NOTES = "notes"
        }
        const val SQL_CREATE_ENTRIES = """
            CREATE TABLE $TABLE_EXERCISES (
                    ${COL.ID}         INTEGER PRIMARY KEY NOT NULL,
                    ${COL.NAME}       TEXT UNIQUE NOT NULL,
                    ${COL.REPS}       INTEGER,
                    ${COL.SETS}       INTEGER,
                    ${COL.WEIGHTS}    REAL,
                    ${COL.NOTES}      TEXT)
            """
        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS exercise"
    }
}