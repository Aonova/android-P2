package com.example.project2_tislam20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
        mainList.setOnItemLongClickListener {adpView,_,pos,id ->
            val cur = (adpView.adapter as CursorAdapter).cursor
            cur.moveToPosition(pos)
            val name = cur.getString(cur.getColumnIndex(db.COL_NAME))
            val alert = AlertDialog.Builder(this)
            with (alert) {
                setTitle("Delete '$name'?")
                setMessage("This cannot be undone")
                setPositiveButton(android.R.string.yes) { _, _ ->
                    removeItemAsync(id)
                    Toast.makeText(
                        this@ExercisesActivity,
                        "Deleted exercise '$name'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                setNegativeButton(android.R.string.no, null)
                show()
            }
            return@setOnItemLongClickListener true
        }
        // set up item tap functionality to edit exercise
        mainList.setOnItemClickListener { _,_,_,id ->
            val intent = Intent(this,EditActivity::class.java)
            with (intent) {
                putExtra("itemId", id)
                startActivityForResult(this,0)
            }
        }
        // set up add new exercise functionality
        addButton.setOnClickListener { _ ->
            val intent = Intent(this,EditActivity::class.java)
            with (intent) {
                startActivityForResult(this,0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) { // coming back from edit activity
            updateListAsync() // refresh the visible list
        }
    }
    // IO-thread coroutine for SQLite remove item execution
    private suspend fun removeItem(id : Long) = withContext(Dispatchers.IO) {
        return@withContext dbHelper.writableDatabase.delete(
            db.TABLE,
            "${db.COL_ID} = ?",
            arrayOf(id.toString())
        )
    }
    // IO-thread coroutine for SQLite full table query
    private suspend fun fullQuery(columns: Array<String>) = withContext(Dispatchers.IO) {
        return@withContext dbHelper.readableDatabase.query( // given columns of all rows
            db.TABLE,
            columns,
            null, null, null, null, null
        )
    }
    private fun removeItemAsync(id : Long) {
        // launches a main thread coroutine -- which blocks on suspended IO-thread
        launch {
            removeItem(id)
            val resultCursor = fullQuery(arrayOf(db.COL_ID,db.COL_NAME,db.COL_NOTES))
            with(mainList.adapter as SimpleCursorAdapter) {changeCursor(resultCursor)}
        }
    }
    private fun updateListAsync() {
        // launches a main thread coroutine -- which blocks on suspended IO-thread
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

    companion object {
        const val TAG = "Project2-tislam20:ExercisesActivity"
    }

}