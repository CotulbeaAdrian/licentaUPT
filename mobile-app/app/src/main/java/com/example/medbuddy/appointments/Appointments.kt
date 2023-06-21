package com.example.medbuddy.appointments

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medbuddy.R
import com.example.medbuddy.doctor.DoctorDashboard

class Appointments :AppCompatActivity() {

    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.appointments_list)
        Toast.makeText(this, "To be implemented, stay soon!", Toast.LENGTH_SHORT).show()
        back = findViewById(R.id.backButton)
        back.setOnClickListener {
            val intent = Intent(this, DoctorDashboard::class.java)
            startActivity(intent)
        }
    }
}