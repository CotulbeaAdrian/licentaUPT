package com.example.medbuddy

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.data.MedicalRecord

class RequestTreatmentAdapter(val context: Context, private val treatmentList: ArrayList<MedicalRecord>) :
    RecyclerView.Adapter<RequestTreatmentAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.list_element, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val patient = treatmentList[position]
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/")
        patient.patientUID?.let {
            databaseRef.child(it).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fullName = snapshot.child("fullName").getValue(String::class.java)
                    holder.textName.text = fullName
                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, RequestResponse::class.java)
                        intent.putExtra("age", patient.age)
                        intent.putExtra("weight", patient.weight)
                        intent.putExtra("gender", patient.gender)
                        intent.putExtra("symptom", patient.symptom)
                        intent.putExtra("patientFullName", fullName)
                        intent.putExtra("uid", patient.uid)
                        context.startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Do nothing
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return treatmentList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.txt_name)
    }
}