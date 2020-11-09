package com.example.project2_tislam20

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.lang.StringBuilder
import kotlin.coroutines.coroutineContext

class WorkoutActivity : AppCompatActivity(){
    private val mainList : ListView by lazy { findViewById(R.id.actWorkout_list_main) }
    private val dbHelper : ExerciseDbHelper by lazy { ExerciseDbHelper(baseContext) }
    private val db = ExerciseDbHelper.Companion // reference to static fields in the DB helper
    private var doneFlags : BooleanArray = BooleanArray(0) // used to save state on which exercises were cleared
    private var activeSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState!=null) {
            doneFlags = savedInstanceState.getBooleanArray(DONE_FLAG)!!
        }
        setContentView(R.layout.activity_workout)
        // get full list of exercises from db.
        // async is not needed since we are only doing it once onCreate and no other user input in this activity
        val cur = fullQuery(arrayOf(db.COL_ID,db.COL_NAME,db.COL_SETS,db.COL_REPS,db.COL_WEIGHTS,db.COL_NOTES))
        // exerciseList is the array of exercises which are displayed in the UI
        val exerciseList: ArrayList<Exercise>
        with (cur) {
            if (count == 0) {
                // handle edge case of no exercises on launch
                AlertDialog.Builder(this@WorkoutActivity).apply {
                    setTitle("No exercises found")
                    setMessage("Add exercises before starting a workout")
                    setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
                    setCancelable(false)
                    show()
                }
                close()
                return
            }
            // initialize done flags on first call, stored/retrieved in saved state bundle afterwards
            if (doneFlags.isEmpty()) doneFlags = BooleanArray(cur.count) { false }

            exerciseList = ArrayList()
            moveToFirst()
            val colId = getColumnIndex(db.COL_ID)
            val colName = getColumnIndex(db.COL_NAME)
            val colReps = getColumnIndex(db.COL_REPS)
            val colSets = getColumnIndex(db.COL_SETS)
            val colWeights = getColumnIndex(db.COL_WEIGHTS)
            val colNotes = getColumnIndex(db.COL_NOTES)
            for (i in doneFlags.indices) {
                if (!doneFlags[i]) {
                    exerciseList.add(
                        Exercise(
                            getInt(colId),
                            getString(colName),
                            getInt(colReps),
                            getInt(colSets),
                            getFloat(colWeights),
                            getString(colNotes)
                        )
                    )
                }
                if (isLast) break
                moveToNext()
            }
            close()
        }
        mainList.adapter = ExerciseAdapter(this,R.layout.list_item,R.id.li_name,R.id.li_notes,exerciseList)
        mainList.onItemLongClickListener = AdapterView.OnItemLongClickListener {adpView,_,pos,_ ->
            val adapter = adpView.adapter as ExerciseAdapter
            val toRemove = adapter.getItem(pos)!!
            adapter.remove(toRemove)
            doneFlags[toRemove.id-1] = true
            if (activeSnackbar != null && !activeSnackbar!!.isShown) activeSnackbar!!.dismiss()
            Snackbar.make(
                mainList,
                "Finished '${toRemove.name}'",
                Snackbar.LENGTH_SHORT
                ).show()
            if (adapter.count == 0) { super.onBackPressed() }
            return@OnItemLongClickListener true
        }
        mainList.onItemClickListener = AdapterView.OnItemClickListener { adpView,_,pos,_ ->
            val exercise = (adpView.adapter as ExerciseAdapter).getItem(pos)!!
            if (activeSnackbar != null && !activeSnackbar!!.isShown) activeSnackbar!!.dismiss()
            val msg = StringBuilder()
            with (msg) {
                if (exercise.reps>0) append("Reps: ${exercise.reps}  |  ")
                if (exercise.sets>0) append("Sets: ${exercise.sets}  |  ")
                if (exercise.weights>0) append("Weights: ${exercise.weights}  |  ")
                if (exercise.notes.isNotBlank()) append("\n${exercise.notes}")
            }
            activeSnackbar = Snackbar.make(
                mainList,
                msg.toString(),
                Snackbar.LENGTH_INDEFINITE
            )
            activeSnackbar!!.show()
        }
    }

    private class Exercise(val id: Int, val name: String, val reps: Int, val sets: Int, val weights: Float, val notes: String)

    private class ExerciseAdapter(val c: Context, val layoutId: Int, val mainTextId: Int, val subTextId: Int, exercises: ArrayList<Exercise>)
        : ArrayAdapter<Exercise>(c, layoutId, exercises) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row: View =
                convertView ?: (c as Activity).layoutInflater.inflate(layoutId, parent, false)
            val exercise = getItem(position) as Exercise
            row.findViewById<TextView>(mainTextId).text = exercise.name
            row.findViewById<TextView>(subTextId).text = exercise.notes
            return row
        }
    }

    // main-thread SQLite full table query - only used once in onCreate, so main-thread is OK
    // also, being a non-writable db object makes it highly optimized
    private fun fullQuery(columns: Array<String>): Cursor {
        return dbHelper.readableDatabase.query( // given columns of all rows
            db.TABLE,
            columns,
            null, null, null, null, null
        )
    }
    /*private fun updateListAsync() {
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

    }*/
    // shows confirmation dialogue when pressing back
    override fun onBackPressed() {
        val alert = AlertDialog.Builder(this)
        with (alert) {
            setTitle("End Workout?")
            setMessage("Progress will be reset")
            setPositiveButton(
                android.R.string.yes,
                DialogInterface.OnClickListener { _, _ -> super.onBackPressed() })
            setNegativeButton(android.R.string.no, null)
            show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(DONE_FLAG,doneFlags)
    }
    companion object {
        const val TAG = "Project2-tislam20:WorkoutActivity"
        const val DONE_FLAG = "com.example.project2_tislam20.doneFlags"
    }
}