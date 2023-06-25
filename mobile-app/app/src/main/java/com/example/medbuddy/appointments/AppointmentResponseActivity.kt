package com.example.medbuddy.appointments

import com.example.medbuddy.data.sharedpref.SharedPrefUtil
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medbuddy.R
import com.example.medbuddy.data.sharedpref.api.ApiServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentResponseActivity : AppCompatActivity() {

    private lateinit var accept: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = ApiServiceBuilder.apiService
        val userData = SharedPrefUtil.getUserData(applicationContext)
        setContentView(R.layout.appointment_response)

        findViewById<ImageView>(R.id.BackButton).setOnClickListener {
            val intent = Intent(this, DoctorNewAppointmentsActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.requestPatientName).text =
            intent.getStringExtra("patientFullName")


        val detailsCall = apiService.getPatientDetails(intent.getStringExtra("patientID"))
        detailsCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val lines = responseBody?.split("\n") ?: emptyList()
                    val detailsDataMap = mutableMapOf<String, String>()

                    for (line in lines) {
                        val keyValue = line.split("=")
                        if (keyValue.size == 2) {
                            val key = keyValue[0].trim()
                            val value = keyValue[1].trim()
                            detailsDataMap[key] = value
                        }
                    }
                    findViewById<TextView>(R.id.patientAgeData).text = detailsDataMap["age"]
                    findViewById<TextView>(R.id.patientGenderData).text = detailsDataMap["gender"]
                    findViewById<TextView>(R.id.patientWeightData).text = detailsDataMap["weight"]
                } else {
                    Log.d("ERROR", "Details request failed. Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("ERROR", "Details request failed serverside. Error: ${t.message}")
                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        val requestID = intent.getStringExtra("requestID")
        accept = findViewById(R.id.layoutAcceptRequest)
        accept.setOnClickListener {
            val date = findViewById<EditText>(R.id.givenDate).text.toString()
            val location = findViewById<EditText>(R.id.givenLocation).text.toString()
            if (date.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please don't leave the fields empty!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val call = apiService.acceptAppointment(requestID, date, location, userData.id)
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val intent =
                                Intent(applicationContext, DoctorAppointmentsActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(
                                applicationContext, "Appointment accepted", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.d(
                                "ERROR",
                                "Appointment respond failed. Response code: ${response.code()}"
                            )
                            Toast.makeText(
                                applicationContext,
                                "Appointment respond failed. Check the fields!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.d("ERROR", "Appointment respond failed serverside. Error: ${t.message}")
                        Toast.makeText(
                            applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
    }
}