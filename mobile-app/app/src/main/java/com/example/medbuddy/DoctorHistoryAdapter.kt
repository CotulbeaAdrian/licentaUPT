package com.example.medbuddy

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

class DoctorHistoryAdapter(val context: Context, private val treatmentList: ArrayList<MedicalRecord>) :
    RecyclerView.Adapter<DoctorHistoryAdapter.UserViewHolder>() {

    private lateinit var mDialog: Dialog

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
                        mDialog = Dialog(context)
                        mDialog.setContentView(R.layout.pop_up_doctor_history)
                        mDialog.setTitle("Pop-up Window")
                        mDialog.findViewById<TextView>(R.id.fullName).text = fullName
                        mDialog.findViewById<TextView>(R.id.diagnostic).text = treatment.diagnostic
                        mDialog.findViewById<TextView>(R.id.medication).text = treatment.medication
                        mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        mDialog.window!!.setLayout(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        mDialog.show()
                    }
                } else {
                    println("Request failed. Response code: ${response.code()}")
                    Toast.makeText(context, "Doctor ID not found!", Toast.LENGTH_SHORT).show()
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