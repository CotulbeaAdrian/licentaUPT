package com.example.medbuddy.presentation.patient

import com.example.medbuddy.data.sharedpref.SharedPrefUtil
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.main.LoginActivity
import com.example.medbuddy.R
import com.example.medbuddy.data.sharedpref.api.ApiServiceBuilder
import com.example.medbuddy.appointments.PatientAppointmentsActivity
import com.example.medbuddy.entities.MedicalRecord
import com.example.medbuddy.virtualrequests.RequestCreateActivity
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientDashboardActivity : AppCompatActivity() {
    private lateinit var treatmentRecyclerView: RecyclerView
    private lateinit var treatmentList: ArrayList<MedicalRecord>
    private lateinit var adapter: PatientTreatmentAdapter

    private lateinit var pDialog: Dialog
    private lateinit var pnDialog: Dialog
    private lateinit var title: TextView
    private lateinit var needDoctor: LinearLayout
    private lateinit var appointment: LinearLayout
    private lateinit var history: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = ApiServiceBuilder.apiService

        setContentView(R.layout.patient_dashboard)

        title = findViewById(R.id.patientDashboardTitle)
        val userData = SharedPrefUtil.getUserData(applicationContext)
        title.text = userData.fullName

        history = findViewById(R.id.layoutTreatmentHistory)
        history.setOnClickListener {
            val intent = Intent(this, PatientHistoryActivity::class.java)
            startActivity(intent)
        }

        needDoctor = findViewById(R.id.layoutNeedDoctor)
        needDoctor.setOnClickListener {
            val intent = Intent(this, RequestCreateActivity::class.java)
            startActivity(intent)
        }

        appointment = findViewById(R.id.layoutAppointments)
        appointment.setOnClickListener {
            val intent = Intent(this, PatientAppointmentsActivity::class.java)
            startActivity(intent)
        }

        treatmentList = ArrayList()
        adapter = PatientTreatmentAdapter(this, treatmentList)
        treatmentRecyclerView = findViewById(R.id.treatmentRecyclerView)
        treatmentRecyclerView.layoutManager = LinearLayoutManager(this)
        treatmentRecyclerView.adapter = adapter

        val settingsButton = findViewById<ImageView>(R.id.settingsPatient)
        settingsButton.setOnClickListener {
            pDialog = Dialog(this)
            pDialog.setContentView(R.layout.pop_up_settings)
            pDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pDialog.window!!.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val logoutButton = pDialog.findViewById<Button>(R.id.btnLogout)
            logoutButton.setOnClickListener {
                SharedPrefUtil.clearUserData(applicationContext)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                pDialog.dismiss()
            }
            val editProfileButton = pDialog.findViewById<Button>(R.id.btnEditProfile)
            editProfileButton.setOnClickListener {
                pDialog.dismiss()

                pnDialog = Dialog(this)
                pnDialog.setContentView(R.layout.patient_edit_profile)
                pnDialog.setTitle("Pop-up Window")
                pnDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                pnDialog.window!!.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val fullName = pnDialog.findViewById<TextInputLayout>(R.id.pfullName)
                val phoneNumber = pnDialog.findViewById<TextInputLayout>(R.id.pphoneNumber)
                val age = pnDialog.findViewById<TextInputLayout>(R.id.editAge)
                val weight = pnDialog.findViewById<TextInputLayout>(R.id.editWeight)
                val editData = pnDialog.findViewById<Button>(R.id.peditData)
                val spinner = pnDialog.findViewById<Spinner>(R.id.genderspinner)
                val adapter = ArrayAdapter.createFromResource(
                    this, R.array.gender_type, android.R.layout.simple_spinner_item
                )

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        // Do nothing
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }

                editData.setOnClickListener {
                    val sdFullName = fullName.editText?.text.toString()
                    val sdGender = spinner.selectedItem.toString()
                    val sdPhoneNumber = phoneNumber.editText?.text.toString()
                    val sdAge = age.editText?.text.toString()
                    val sdWeight = weight.editText?.text.toString()
                    if (sdFullName.isEmpty() || sdPhoneNumber.isEmpty() || sdAge.isEmpty() || sdWeight.isEmpty()) {
                        if (sdPhoneNumber.isEmpty()) {
                            phoneNumber.error = "Please enter your phone number"
                        }
                        if (sdFullName.isEmpty()) {
                            fullName.error = "Please enter your phone number"
                        }
                        if (sdAge.isEmpty()) {
                            age.error = "Please enter your age"
                        }
                        if (sdWeight.isEmpty()) {
                            weight.error = "Please enter your weight"
                        }
                        Toast.makeText(this, "Please fill every field.", Toast.LENGTH_SHORT).show()
                    } else if (sdPhoneNumber.length != 10) {
                        phoneNumber.error = "Phone number must have exactly 10 digits."
                    } else if (sdAge.length > 2) {
                        phoneNumber.error = "Please introduce an age between 18 and 99 "
                    } else if (sdWeight.length > 4) {
                        phoneNumber.error = "Please enter you weight!"
                    } else {
                        // Make the register request
                        val call = apiService.updateProfile(
                            userData.id,
                            sdFullName,
                            sdPhoneNumber,
                            sdAge,
                            sdWeight,
                            sdGender
                        )

                        call.enqueue(object : Callback<String> {
                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {
                                if (response.isSuccessful) {
                                    Log.d("INFO", "Profile updated for user $sdFullName.")
                                    Toast.makeText(
                                        applicationContext,
                                        "Profile Updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Log.d(
                                        "ERROR",
                                        "Operation failed. Response code: ${response.code()}"
                                    )
                                    Toast.makeText(
                                        applicationContext,
                                        "Profile update failed. Check the fields!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.d("ERROR", "Profile update failed. Error: ${t.message}")
                                Toast.makeText(
                                    applicationContext,
                                    "Server error. Try again!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                        pnDialog.dismiss()
                    }
                }
                pnDialog.show()
            }
            pDialog.show()
        }


        treatmentList.clear()
        val call = apiService.getMedicalRecordsAsPatient(userData.id)

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
                        if (treatmentDataMap["id"]?.isNotBlank() == true) {
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

                            if (auxTreatment.accepted == "1" && auxTreatment.active == "1") {
                                treatmentList.add(auxTreatment)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d("ERROR", "Request failed. Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("ERROR", "Data request failed. Error: ${t.message}")
                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}