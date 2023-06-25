package com.example.medbuddy.appointments

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
import com.example.medbuddy.data.sharedpref.api.ApiServiceBuilder
import com.example.medbuddy.entities.Appointment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoctorNewAppointmentsAdapter(val context: Context, private val appointmentsList: ArrayList<Appointment>) :
    RecyclerView.Adapter<DoctorNewAppointmentsAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.list_element, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val appointment = appointmentsList[position]
        val apiBuilder = ApiServiceBuilder.apiService

        val call = apiBuilder.getName(appointment.patientID)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val aux = responseBody?.split("=")
                    val fullName = aux?.get(1)
                    holder.textName.text = fullName

                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, AppointmentResponseActivity::class.java)
                        intent.putExtra("requestID", appointment.id)
                        intent.putExtra("patientID", appointment.patientID)
                        intent.putExtra("patientFullName", fullName)
                        context.startActivity(intent)
                    }
                } else {
                    Log.d("ERROR","Request failed. Response code: ${response.code()}")
                    Toast.makeText(context, "Patient ID not found!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("ERROR","Name request failed. Error: ${t.message}")
                Toast.makeText(context, "Server error. Try again!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.txt_name)
    }
}