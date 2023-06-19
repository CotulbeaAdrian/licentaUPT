package com.example.medbuddy

import SharedPrefUtil
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medbuddy.api.ApiServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestResponse : AppCompatActivity() {

    private lateinit var accept: LinearLayout
    private lateinit var decline: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = ApiServiceBuilder.apiService

        val userData = SharedPrefUtil.getUserData(applicationContext)

        setContentView(R.layout.request_response)

        findViewById<ImageView>(R.id.BackButton).setOnClickListener {
            val intent = Intent(this, DoctorDashboard::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.requestPatientName).text = intent.getStringExtra("patientFullName")
        findViewById<TextView>(R.id.patientWords).text = intent.getStringExtra("symptom")


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
                    println("Request decline failed. Response code: ${response.code()}")
                    Toast.makeText(applicationContext, "Request decline failed. Check the fields!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                println("Request decline failed serverside. Error: ${t.message}")
                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT).show()
            }
        })




        val requestID = intent.getStringExtra("requestID")

        accept = findViewById(R.id.layoutAcceptRequest)
        accept.setOnClickListener {
            val diagnostic = findViewById<EditText>(R.id.givenDiagnostic).text.toString()
            val medication = findViewById<EditText>(R.id.givenMedication).text.toString()
            if (diagnostic.isEmpty() || medication.isEmpty()) {
                Toast.makeText(this, "Please don't leave the fields empty!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val call = apiService.acceptRequest(requestID, diagnostic, medication, userData.id)
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val intent = Intent(applicationContext, DoctorDashboard::class.java)
                            startActivity(intent)
                            Toast.makeText(
                                applicationContext,
                                "Request accepted",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            println("Request respond failed. Response code: ${response.code()}")
                            Toast.makeText(
                                applicationContext,
                                "Request respond failed. Check the fields!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        println("Request respond failed serverside. Error: ${t.message}")
                        Toast.makeText(
                            applicationContext,
                            "Server error. Try again!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }

        decline = findViewById(R.id.layoutDeclineRequest)
        decline.setOnClickListener {
            val call = apiService.declineRequest(requestID)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val intent = Intent(applicationContext, DoctorDashboard::class.java)
                        startActivity(intent)
                        Toast.makeText(applicationContext, "Request declined", Toast.LENGTH_SHORT).show()
                    } else {
                        println("Request decline failed. Response code: ${response.code()}")
                        Toast.makeText(applicationContext, "Request decline failed. Check the fields!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    println("Request decline failed serverside. Error: ${t.message}")
                    Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}