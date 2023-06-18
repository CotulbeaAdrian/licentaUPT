package com.example.medbuddy

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.api.ApiServiceBuilder
import com.example.medbuddy.data.MedicalRecord
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientTreatmentAdapter(val context: Context, private val treatmentList: ArrayList<MedicalRecord>) :
    RecyclerView.Adapter<PatientTreatmentAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.list_element, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val treatment = treatmentList[position]
        val apiService = ApiServiceBuilder.apiService
        val call = apiService.getName(treatment.doctorID)


        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val aux = responseBody?.split("=")
                    var fullName = aux?.get(2)
                    holder.textName.text = fullName + " - " + treatment.diagnostic

                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, PatientInteraction::class.java)
                        intent.putExtra("recordID", treatment.id)
                        context.startActivity(intent)
                    }
                } else {
                    println("Request failed. Response code: ${response.code()}")
                    Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                println("Data request failed. Error: ${t.message}")
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