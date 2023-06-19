package com.example.medbuddy

import SharedPrefUtil
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.api.ApiServiceBuilder
import com.example.medbuddy.data.MedicalRecord
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestsList : AppCompatActivity() {

    private lateinit var treatmentRecyclerView: RecyclerView
    private lateinit var requestsList: ArrayList<MedicalRecord>
    private lateinit var adapter: RequestTreatmentAdapter

    private lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userData = SharedPrefUtil.getUserData(applicationContext)
        val apiService = ApiServiceBuilder.apiService

        setContentView(R.layout.request_list)

        back = findViewById(R.id.backButton)
        back.setOnClickListener {
            val intent = Intent(this, DoctorDashboard::class.java)
            startActivity(intent)
        }

        requestsList = ArrayList()
        adapter = RequestTreatmentAdapter(this, requestsList)
        treatmentRecyclerView = findViewById(R.id.requestsRecyclerView)
        treatmentRecyclerView.layoutManager = LinearLayoutManager(this)
        treatmentRecyclerView.adapter = adapter

        val call = apiService.getMedicalRecordsAsDoctor(userData.id)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    val entries = responseBody?.split("&") ?: emptyList()

                    for (entry in entries) {
                        val lines = entry.split("\n")
                        val treatmentDataMap = mutableMapOf<String, String>()
                        for (line in lines) {
                            val keyValue = line.split("=")
                            if (keyValue.size == 2) {
                                val key = keyValue[0].trim()
                                val value = keyValue[1].trim()
                                treatmentDataMap[key] = value
                            }
                        }
                        // Extract the treatment data from the map
                        if(treatmentDataMap["id"]?.isNotBlank() == true) {
                            val id = treatmentDataMap["id"]
                            val active = treatmentDataMap["active"]
                            val accepted = treatmentDataMap["accepted"]
                            val patientID = treatmentDataMap["patientID"]
                            val doctorID = treatmentDataMap["doctorID"]
                            val symptom = treatmentDataMap["symptom"]
                            val diagnostic = treatmentDataMap["diagnostic"]
                            val medication = treatmentDataMap["medication"]
                            val specialty = treatmentDataMap["specialty"]

                            // Create MedicalRecord object
                            val auxTreatment = MedicalRecord(
                                id.orEmpty(),
                                active.orEmpty(),
                                accepted.orEmpty(),
                                patientID.orEmpty(),
                                doctorID.orEmpty(),
                                symptom.orEmpty(),
                                diagnostic.orEmpty(),
                                medication.orEmpty(),
                                specialty.orEmpty()
                            )

                            if(auxTreatment.accepted == "0" && auxTreatment.active == "1"
                                && intent.getStringExtra("specialty") == auxTreatment.specialty ){
                                requestsList.add(auxTreatment)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    println("Request failed. Response code: ${response.code()}")
                    Toast.makeText(applicationContext, "Data request failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                println("Data request failed. Error: ${t.message}")
                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}