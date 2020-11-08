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

        insertDefault(db)
        // a thousand items takes < .5 secs to make/insert and is still instant to query and update... I think performance is not an issue.
        //insertStressTest(db,1000)
    }


    private fun insertDefault(db: SQLiteDatabase) { // generates and inserts the following default exercises
        val exercises = arrayOf("Sit ups","Push ups","Bicep Curls","Planks","Crunches")
        for (exercise in exercises) {
            val cv = ContentValues().apply {
                put(COL_NAME,exercise)
                put(COL_REPS, Random.nextInt(2,20)*5)
                put(COL_SETS, Random.nextInt(1,5))
                put(COL_WEIGHTS, Random.nextDouble(5.0,100.0))
                put(COL_NOTES,"Default generated exercise")
            }
            try {
                db.insertOrThrow(TABLE,null,cv)
            } catch (e : SQLiteException) {
                Log.e(TAG,"Insert default items failed: ${e.message}")
            }
        }
    }

    private fun insertStressTest(db : SQLiteDatabase, num : Int) {
        val startTime = System.nanoTime()
        for (i in 1..num) {
            val cv = ContentValues().apply {
                put(COL_NAME,"Stress-test Item #$i")
                put(COL_REPS, Random.nextInt(2,20)*5)
                put(COL_SETS, Random.nextInt(1,5))
                put(COL_WEIGHTS, Random.nextDouble(5.0,100.0))
                put(COL_NOTES,"Item $i/$num generated in ${(System.nanoTime()-startTime)/1e6}ms")
            }
            try {
                db.insertOrThrow(TABLE,null,cv)
            } catch (e : SQLiteException) {
                Log.e(TAG,"Insert stress-test failed: ${e.message}")
            }
        }
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
        const val TAG = "Project2-tislam20:ExerciseDbHandler"
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Exercises.db"

        const val TABLE = "exercise"
        const val COL_ID = "_id"
        const val COL_NAME = "name"
        const val COL_REPS = "reps"
        const val COL_SETS = "sets"
        const val COL_WEIGHTS = "weights"
        const val COL_NOTES = "notes"
        const val SQL_CREATE_ENTRIES = """
            CREATE TABLE $TABLE (
                    $COL_ID         INTEGER PRIMARY KEY NOT NULL,
                    $COL_NAME       TEXT  NOT NULL UNIQUE,
                    $COL_REPS       INTEGER DEFAULT 0,
                    $COL_SETS       INTEGER DEFAULT 0,
                    $COL_WEIGHTS    REAL DEFAULT 0.0,
                    $COL_NOTES      TEXT DEFAULT '')
            """
        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS exercise"
    }
}