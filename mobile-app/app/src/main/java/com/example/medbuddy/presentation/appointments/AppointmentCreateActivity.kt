package com.example.medbuddy.appointments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.medbuddy.presentation.patient.PatientDashboardActivity
import com.example.medbuddy.R
import com.example.medbuddy.data.sharedpref.api.ApiServiceBuilder
import com.example.medbuddy.data.sharedpref.SharedPrefUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentCreateActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appointment_create)

        back = findViewById(R.id.BackButton)
        back.setOnClickListener {
            val intent = Intent(this, PatientDashboardActivity::class.java)
            startActivity(intent)
        }

        spinner = findViewById(R.id.spinner_specialties)
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.medical_specialties, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        val userData = SharedPrefUtil.getUserData(applicationContext)

        val sendRequest = findViewById<LinearLayout>(R.id.sendRequestLayout)
        sendRequest.setOnClickListener {
            val specialty = spinner.selectedItem.toString()

            val apiService = ApiServiceBuilder.apiService
            val call = apiService.createAppointment(userData.id, specialty)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val intent = Intent(applicationContext, PatientAppointmentsActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(
                            applicationContext,
                            "Appointment request has been sent",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Log.d(
                            "ERROR",
                            "Appointment request failed. Response code: ${response.code()}"
                        )
                        Toast.makeText(
                            applicationContext,
                            "Appointment request failed. Check the fields!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("ERROR", "Create appointment request failed. Error: ${t.message}")
                    Toast.makeText(
                        applicationContext,
                        "Server error. Try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}