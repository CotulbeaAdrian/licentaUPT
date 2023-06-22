package com.example.medbuddy.appointments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.R
import com.example.medbuddy.api.ApiServiceBuilder
import com.example.medbuddy.data.Appointment
import com.example.medbuddy.doctor.DoctorDashboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoctorAppointmentsAdapter(val context: Context, private val appointmentsList: ArrayList<Appointment>) :
    RecyclerView.Adapter<DoctorAppointmentsAdapter.UserViewHolder>() {

    private lateinit var mDialog: Dialog

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.list_element, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val appointment = appointmentsList[position]
        val apiService = ApiServiceBuilder.apiService
        val call = apiService.getName(appointment.patientID)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val aux = responseBody?.split("=")
                    val fullName = aux?.get(1)
                    holder.textName.text = fullName
                    holder.itemView.setOnClickListener {
                        mDialog = Dialog(context)
                        mDialog.setContentView(R.layout.pop_up_doctor_appointment)
                        mDialog.findViewById<TextView>(R.id.fullName).text = fullName
                        mDialog.findViewById<TextView>(R.id.location).text = appointment.location
                        mDialog.findViewById<TextView>(R.id.date).text = appointment.date
                        mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        mDialog.window!!.setLayout(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val delete = mDialog.findViewById<LinearLayout>(R.id.layoutDeleteAppointment)
                        delete.setOnClickListener {
                            val auxCall = apiService.deleteAppointment(appointment.id)
                            auxCall.enqueue(object : Callback<String> {
                                override fun onResponse(call: Call<String>, response: Response<String>) {
                                    if (response.isSuccessful) {
                                        Log.d("INFO","Appointment deleted.")
                                        Toast.makeText(context, "Appointment deleted", Toast.LENGTH_SHORT).show()
                                        notifyDataSetChanged()
                                        mDialog.dismiss()
                                    } else {
                                        Log.d("ERROR","Operation failed. Response code: ${response.code()}")
                                        Toast.makeText(context, "Appointment delete failed. Check the fields!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    Log.d("ERROR","Appointment delete failed. Error: ${t.message}")
                                    Toast.makeText(context, "Server error. Try again!", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        mDialog.show()
                    }
                } else {
                    Log.d("ERROR","Request failed. Response code: ${response.code()}")
                    Toast.makeText(context, "Patient ID not found!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("ERROR","Data request failed. Error: ${t.message}")
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