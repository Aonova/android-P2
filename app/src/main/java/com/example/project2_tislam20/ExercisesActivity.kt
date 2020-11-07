package com.example.project2_tislam20

import android.database.AbstractCursor
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import kotlinx.coroutines.*

class ExercisesActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val mainList : ListView by lazy { findViewById(R.id.actExercises_list_exercises) }
    private val addButton : Button by lazy { findViewById(R.id.actExercises_but_new)}
    private val dbHelper : ExerciseDbHelper by lazy { ExerciseDbHelper(baseContext) }
    private val db = ExerciseDbHelper.Companion // reference to static fields in the DB helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)
        // set up main list with current exercises
        updateListAsync()
        // set up item click hold functionality to remove exercise
        mainList.setOnItemLongClickListener {_,_,_,id ->
            removeItemAsync(id)
            Toast.makeText(this,"Removed exercise #$id",Toast.LENGTH_SHORT).show()
            return@setOnItemLongClickListener true
        }
        // set up item tap functionality to edit exercise
        mainList.setOnItemClickListener { _,_,_,id ->

        }
    }

    // IO-thread coroutine for SQLite remove item excution
    private suspend fun removeItem(id : Long) = withContext(Dispatchers.IO) {
        return@withContext dbHelper.writableDatabase.delete(
            db.TABLE_EXERCISES,
            "${db.COL_ID} = ?",
            arrayOf(id.toString())
        )
    }
    // IO-thread coroutine for SQLite full table query
    private suspend fun fullQuery(columns: Array<String>) = withContext(Dispatchers.IO) {
        return@withContext dbHelper.readableDatabase.query( // given columns of all rows
            db.TABLE_EXERCISES,
            columns,
            null, null, null, null, null
        )
    }
    private fun removeItemAsync(id : Long) {
        // launches a separate coroutine but on main thread -- coroutine will block on suspend
        launch {
            removeItem(id)
            val resultCursor = fullQuery(arrayOf(db.COL_ID,db.COL_NAME,db.COL_NOTES))
            with(mainList.adapter as SimpleCursorAdapter) {changeCursor(resultCursor)}
        }
    }
    private fun updateListAsync() {
        // launches a separate coroutine but on main thread -- coroutine will block on suspend
        launch {
            // full query will suspend coroutine
            val resultCursor = fullQuery(arrayOf(db.COL_ID, db.COL_NAME, db.COL_NOTES))
            if (mainList.adapter == null) {
                // create the adapter if needed
                mainList.adapter = SimpleCursorAdapter(baseContext,
                    R.layout.list_item,
                    resultCursor,
                    arrayOf(db.COL_NAME,db.COL_NOTES),
                    arrayOf(R.id.li_name,R.id.li_notes).toIntArray(),0)
            } else {
                // simply change the cursor if the adapter is already created
                with(mainList.adapter as SimpleCursorAdapter) {changeCursor(resultCursor)}
            }
        }

    }



}