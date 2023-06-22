package com.example.medbuddy.appointments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.R
import com.example.medbuddy.api.ApiServiceBuilder
import com.example.medbuddy.data.Appointment
import com.example.medbuddy.sharedpref.SharedDoctorSpecialty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoctorNewAppointments : AppCompatActivity() {

    private lateinit var appointmentRecyclerView: RecyclerView
    private lateinit var appointmentsList: ArrayList<Appointment>
    private lateinit var adapter: DoctorNewAppointmentsAdapter
    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.doctor_new_appointments)

        back = findViewById(R.id.backButton)
        back.setOnClickListener {
            val intent = Intent(this, DoctorAppointments::class.java)
            startActivity(intent)
        }

        appointmentsList = ArrayList()
        adapter = DoctorNewAppointmentsAdapter(this, appointmentsList)
        appointmentRecyclerView = findViewById(R.id.requestsRecyclerView)
        appointmentRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentRecyclerView.adapter = adapter

        val apiService = ApiServiceBuilder.apiService
        val doctorSpecialty = SharedDoctorSpecialty.getSpecialty(applicationContext)
        val call = apiService.getAppointments()
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    val entries = responseBody?.split("&") ?: emptyList()

                    for (entry in entries) {
                        val lines = entry.split("\n")
                        val appointmentDataMap = mutableMapOf<String, String>()
                        for (line in lines) {
                            val keyValue = line.split("=")
                            if (keyValue.size == 2) {
                                val key = keyValue[0].trim()
                                val value = keyValue[1].trim()
                                appointmentDataMap[key] = value
                            }
                        }
                        // Extract the treatment data from the map
                        if (appointmentDataMap["id"]?.isNotBlank() == true) {
                            val id = appointmentDataMap["id"]
                            val active = appointmentDataMap["active"]
                            val accepted = appointmentDataMap["accepted"]
                            val patientID = appointmentDataMap["patientID"]
                            val doctorID = appointmentDataMap["doctorID"]
                            val date = appointmentDataMap["date"]
                            val location = appointmentDataMap["location"]
                            val specialty = appointmentDataMap["specialty"]

                            // Create MedicalRecord object
                            val auxAppointment = Appointment(
                                id.orEmpty(),
                                active.orEmpty(),
                                accepted.orEmpty(),
                                patientID.orEmpty(),
                                doctorID.orEmpty(),
                                date.orEmpty(),
                                location.orEmpty(),
                                specialty.orEmpty()
                            )

                            if (auxAppointment.accepted == "0" && auxAppointment.active == "1" && auxAppointment.specialty == doctorSpecialty)
                                appointmentsList.add(auxAppointment)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d(
                        "ERROR",
                        "New appointments request failed. Response code: ${response.code()}"
                    )
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("ERROR", "New appointments request failed. Error: ${t.message}")
                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}