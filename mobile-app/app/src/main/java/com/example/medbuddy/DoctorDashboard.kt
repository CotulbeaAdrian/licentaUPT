package com.example.medbuddy

import SharedDoctorSpecialty
import SharedPrefUtil
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.api.ApiServiceBuilder
import com.example.medbuddy.data.MedicalRecord
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class DoctorDashboard : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var treatmentList: ArrayList<MedicalRecord>
    private lateinit var adapter: DoctorTreatmentAdapter

    private lateinit var mDialog: Dialog
    private lateinit var mnDialog: Dialog

    private lateinit var title: TextView
    private lateinit var appointments: LinearLayout
    private lateinit var requests: LinearLayout
    private lateinit var patientsHistory: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.doctor_dashboard)

        val apiService = ApiServiceBuilder.apiService
        val userData = SharedPrefUtil.getUserData(applicationContext)

        title = findViewById(R.id.doctorDashboardTitle)
        title.text = "Dr. " + userData.fullName

        val specialtyCall = apiService.getSpecialty(userData.id)
        specialtyCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val aux = responseBody?.split("=")
                    SharedDoctorSpecialty.saveSpecialty(applicationContext,aux?.get(1).toString())
                } else {
                    Toast.makeText(applicationContext, "Choose a specialty in Settings -> Edit Profile!!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                println("Specialty request failed. Error: ${t.message}")
                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT).show()
            }
        })

        val specialty = SharedDoctorSpecialty.getSpecialty(applicationContext)


        appointments = findViewById(R.id.layoutAppointments)
        appointments.setOnClickListener {
            val intent = Intent(this, DoctorAppointments::class.java)
            startActivity(intent)
        }

        requests = findViewById(R.id.layoutNeedDoctor)
        requests.setOnClickListener {
            val intent = Intent(this, RequestsList::class.java)
            intent.putExtra("specialty", specialty)
            startActivity(intent)
        }

        patientsHistory = findViewById(R.id.layoutPatientsHistory)
        patientsHistory.setOnClickListener {
            val intent = Intent(this, DoctorHistory::class.java)
            startActivity(intent)
        }

        treatmentList = ArrayList()
        adapter = DoctorTreatmentAdapter(this, treatmentList)
        userRecyclerView = findViewById(R.id.treatmentRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter
        treatmentList.clear()
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
                            val specialtyAux = treatmentDataMap["specialty"]

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
                                specialtyAux.orEmpty()
                            )

                            if(auxTreatment.accepted == "1" && auxTreatment.active == "1"){
                                treatmentList.add(auxTreatment)
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

        val settingsButton = findViewById<ImageView>(R.id.settingsDoctor)
        settingsButton.setOnClickListener {
            mDialog = Dialog(this)
            mDialog.setContentView(R.layout.pop_up_settings)
            mDialog.setTitle("Pop-up Window")
            mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mDialog.window!!.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val logoutButton = mDialog.findViewById<Button>(R.id.btnLogout)
            logoutButton.setOnClickListener {
                SharedPrefUtil.clearUserData(applicationContext)
                SharedDoctorSpecialty.clearSpecialtyMedic(applicationContext)
                startActivity(Intent(this, Login::class.java))
                finish()
                mDialog.dismiss()
            }
            val editProfileButton = mDialog.findViewById<Button>(R.id.btnEditProfile)
            editProfileButton.setOnClickListener {
                mDialog.dismiss()

                mnDialog = Dialog(this)
                mnDialog.setContentView(R.layout.doctor_edit_profile)
                mnDialog.setTitle("Pop-up Window")
                mnDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mnDialog.window!!.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val fullName = mnDialog.findViewById<TextInputLayout>(R.id.editfullName)
                val phoneNumber = mnDialog.findViewById<TextInputLayout>(R.id.editphoneNumber)
                val editData = mnDialog.findViewById<Button>(R.id.editData)
                val spinner = mnDialog.findViewById<Spinner>(R.id.editspinner)
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
                editData.setOnClickListener {
                    val sdFullName = fullName.editText?.text.toString()
                    val sdSpeciality = spinner.selectedItem.toString()
                    val sdPhoneNumber = phoneNumber.editText?.text.toString()
                    if (sdFullName.isEmpty() || sdPhoneNumber.isEmpty()) {
                        if (sdFullName.isEmpty()) {
                            fullName.error = "Please enter your Full Name"
                        }
                        if (sdPhoneNumber.isEmpty()) {
                            phoneNumber.error = "Please enter your phone number"
                        }
                        Toast.makeText(this, "Please check your fields", Toast.LENGTH_SHORT).show()
                    } else if (sdPhoneNumber.length != 10) {
                        phoneNumber.error = "Please enter 10 digits"
                        Toast.makeText(this, "Please enter 10 digits ", Toast.LENGTH_SHORT).show()
                    } else {
                        val updateCall = apiService.updateDoctor(userData.id, sdFullName, sdPhoneNumber, sdSpeciality)
                        updateCall.enqueue(object : Callback<String> {
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(applicationContext, "Your data has been changed", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(applicationContext, "Update failed", Toast.LENGTH_SHORT).show()
                                }
                                mnDialog.dismiss()
                            }
                            override fun onFailure(call: Call<String>, t: Throwable) {
                                println("Update request failed. Error: ${t.message}")
                                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT).show()
                            }
                        })

                    }
                }
                mnDialog.show()
            }
            mDialog.show()
        }
    }
}