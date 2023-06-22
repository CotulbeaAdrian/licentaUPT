package com.example.medbuddy.doctor

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.R
import com.example.medbuddy.api.ApiServiceBuilder
import com.example.medbuddy.data.MedicalRecord
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoctorTreatmentAdapter(
    val context: Context,
    private val treatmentList: ArrayList<MedicalRecord>
) :
    RecyclerView.Adapter<DoctorTreatmentAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.list_element, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val treatment = treatmentList[position]
        val apiService = ApiServiceBuilder.apiService
        val call = apiService.getName(treatment.patientID)


        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val aux = responseBody?.split("=")
                    val fullName = aux?.get(1)
                    holder.textName.text = fullName + " - " + treatment.diagnostic

                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, DoctorInteraction::class.java)
                        intent.putExtra("recordID", treatment.id)
                        intent.putExtra("symptom", treatment.symptom)
                        intent.putExtra("diagnostic", treatment.diagnostic)
                        intent.putExtra("medication", treatment.medication)
                        intent.putExtra("patientID", treatment.patientID)
                        intent.putExtra("doctorID", treatment.doctorID)
                        intent.putExtra("patientFullName", fullName)
                        context.startActivity(intent)
                    }
                } else {
                    Log.d("ERROR", "Request failed. Response code: ${response.code()}")
                    Toast.makeText(context, "Doctor ID not found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("ERROR", "Data request failed. Error: ${t.message}")
                Toast.makeText(context, "Server error. Try again!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getItemCount(): Int {
        return treatmentList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.txt_name)
    }
}