package com.example.project2_tislam20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // "Edit Exercises" button
        findViewById<Button>(R.id.actMain_but_edit).setOnClickListener {
            val intent = Intent(this,ExercisesActivity::class.java)
            startActivity(intent)
        }

        // "Workout" button
        findViewById<Button>(R.id.actMain_but_start).setOnClickListener {
            val intent = Intent(this,WorkoutActivity::class.java)
            startActivity(intent)
        }
    }
}