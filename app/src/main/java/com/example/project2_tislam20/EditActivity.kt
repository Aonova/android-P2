package com.example.project2_tislam20

import android.content.DialogInterface
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class EditActivity : AppCompatActivity() {

    private val itemId : Long by lazy {
        intent.getLongExtra("itemId",0L)
    }
    private val isNewItem : Boolean by lazy { itemId == 0L }

    private val titleText : TextView by lazy {findViewById(R.id.actEdit_text_main)}
    private val saveBut : Button by lazy {findViewById(R.id.actEdit_but_save)}

    private val nameInput : EditText by lazy {findViewById(R.id.actEdit_input_name)}
    private val repsInput : EditText by lazy {findViewById(R.id.actEdit_input_reps)}
    private val setsInput : EditText by lazy {findViewById(R.id.actEdit_input_sets)}
    private val weightsInput : EditText by lazy {findViewById(R.id.actEdit_input_weights)}
    private val notesInput : EditText by lazy {findViewById(R.id.actEdit_input_notes)}

    private val dbHelper : ExerciseDbHelper by lazy { ExerciseDbHelper(baseContext) }
    private val db = ExerciseDbHelper.Companion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        setUpViews()
        val inputs = arrayOf(nameInput,repsInput,setsInput,weightsInput,notesInput)
        val cols = arrayOf(db.COL_NAME,db.COL_REPS,db.COL_SETS,db.COL_WEIGHTS,db.COL_NOTES)
        saveBut.setOnClickListener {
            val flags = BooleanArray(5) {false}

            val setCols = ArrayList<String>(); val setVals = ArrayList<String>()
            for (i in inputs.indices){
                if (inputs[i].text.isNotEmpty()) {
                    setCols.add(cols[i])
                    setVals.add(inputs[i].text.toString())
                    
                }
            }
        }
    }

    private fun setUpViews() {
        if (isNewItem) {
            titleText.text = "New Exercise"
            saveBut.text = "Add Exercise"
            with (nameInput) {
                hint = "Enter name of exercise (required)"
                text.clear()
                isEnabled = true
            }
            repsInput.hint = "Optional"
            setsInput.hint = "Optional"
            weightsInput.hint = "Optional"
            notesInput.hint = "Add optional notes"

        } else {
            titleText.text = "Edit Exercise"
            saveBut.text = "Save Changes"
            // get the current row of the item we are editing
            val item = getItem(itemId)
            if (item == null) {Log.e(TAG,"Retrieved item is null"); return}
            with (nameInput) {
                hint = item.getString(0)
                text.clear()
                isEnabled = false
            }
            repsInput.hint = item.getInt(1).toString()
            setsInput.hint = item.getInt(2).toString()
            weightsInput.hint = item.getFloat(3).toString()
            notesInput.hint = item.getString(4)

            item.close()
        }
    }

    private fun getItem(id:Long) : Cursor? {
        val cursor = dbHelper.writableDatabase.query(
            db.TABLE_EXERCISES,
            arrayOf(db.COL_NAME, db.COL_REPS, db.COL_SETS, db.COL_WEIGHTS, db.COL_NOTES),
            "${db.COL_ID} = $id",
            null,null, null, null
        )
        if (cursor.count==0) {Log.e(TAG,"Failed to get item with row#$id"); return null}
        cursor.moveToFirst()
        return cursor
    }

    override fun onBackPressed() {
        val alert = AlertDialog.Builder(this)
        with (alert) {
            setTitle("Go back?")
            setMessage("Unsaved changes will be lost")
            setPositiveButton(
                android.R.string.yes,
                DialogInterface.OnClickListener { _,_ -> super.onBackPressed() })
            setNegativeButton(android.R.string.no, null)
            show()
        }
    }

    companion object {
        const val TAG = "Project2-tislam20:EditActivity"
    }
}