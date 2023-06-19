package com.example.medbuddy

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
import com.example.medbuddy.data.MedicalRecord
import com.google.android.material.textfield.TextInputLayout
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

        title = findViewById(R.id.doctorDashboardTitle)
        if (intent.getStringExtra("fullName") != null) {
            title.text = "Dr. " + intent.getStringExtra("fullName")
        } else {
            title.text = ""
        }

        appointments = findViewById(R.id.layoutAppointments)
        appointments.setOnClickListener {
            val intent = Intent(this, DoctorAppointments::class.java)
            startActivity(intent)
        }

        requests = findViewById(R.id.layoutNeedDoctor)
        requests.setOnClickListener {
            val userRef = auth.currentUser?.let { it1 -> database.reference.child("Users/").child(it1.uid) }
            userRef?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    val specialty = snapshot.child("specialty").getValue(String::class.java)
                    val intent = Intent(this, RequestsList::class.java)
                    intent.putExtra("specialty", specialty)
                    startActivity(intent)
                }
            }
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
        mDbRef.child("Treatment").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                treatmentList.clear()
                for (postSnapshot in snapshot.children) {
                    val treatment = postSnapshot.getValue(Treatment::class.java)
                    if (treatment != null) {
                        if ((mAuth.currentUser?.uid == treatment.doctorUID) &&
                            (treatment.accepted == true) && (treatment.active == true)
                        ) {
                            treatmentList.add(treatment)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Do nothing
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
                        val userReference =
                            database.reference.child("Users").child(auth.currentUser!!.uid)
                        val updates = HashMap<String, Any>()
                        updates["fullName"] = sdFullName
                        updates["phoneNumber"] = sdPhoneNumber
                        updates["specialty"] = sdSpeciality
                        userReference.updateChildren(updates)
                        mnDialog.dismiss()
                        Toast.makeText(this, "Your data has been changed", Toast.LENGTH_SHORT).show()
                    }
                }
                mnDialog.show()
            }
            mDialog.show()
        }
    }
}