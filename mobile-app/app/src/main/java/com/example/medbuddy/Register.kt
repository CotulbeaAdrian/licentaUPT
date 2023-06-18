package com.example.medbuddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.medbuddy.api.ApiServiceBuilder
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class Register : AppCompatActivity() {

    private val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val password = findViewById<TextInputLayout>(R.id.passwordRegister)
        val fullName = findViewById<TextInputLayout>(R.id.fullName)
        val phoneNumber = findViewById<TextInputLayout>(R.id.phoneNumber)
        val email = findViewById<TextInputLayout>(R.id.Email)
        val confirmPassword = findViewById<TextInputLayout>(R.id.confirmPassword)
        val saveData = findViewById<Button>(R.id.saveData)
        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.spinner_items, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {}

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        saveData.setOnClickListener {
            val sdPassword = password.editText?.text.toString()
            val sdFullName = fullName.editText?.text.toString()
            val sdPhoneNumber = phoneNumber.editText?.text.toString()
            val sdEmail = email.editText?.text.toString()
            val sdConfirmPassword = confirmPassword.editText?.text.toString()
            val sdRole = spinner.selectedItem.toString()

            if (sdEmail.isEmpty() || sdPassword.isEmpty() ||
                sdFullName.isEmpty() || sdPhoneNumber.isEmpty() || sdConfirmPassword.isEmpty())
            {
                if (sdEmail.isEmpty()) {
                    email.error = "Please enter your email address!"
                }
                if (sdPassword.isEmpty()) {
                    password.error = "Please enter your password!"
                }
                if (sdFullName.isEmpty()) {
                    fullName.error = "Please enter your full name!"
                }
                if (sdPhoneNumber.isEmpty()) {
                    phoneNumber.error = "Please enter your phone number!"
                }
                if (sdConfirmPassword.isEmpty()) {
                    confirmPassword.error = "Please confirm your password!"
                }
                Toast.makeText(this, "Please check your fields!", Toast.LENGTH_SHORT).show()

            } else if (!sdEmail.matches(emailRegex.toRegex())) {
                email.error = "Email address must be valid!"
            } else if (sdPhoneNumber.length != 10) {
                phoneNumber.error = "Phone number must have 10 digits!"
            } else if (sdPassword.length < 7) {
                password.error = "Password must have least 7 characters!"
            } else if (sdPassword != sdConfirmPassword) {
                confirmPassword.error = "Passwords don't match!"
            } else {

                val apiService = ApiServiceBuilder.apiService
                // Make the register request
                val call = apiService.register(sdFullName,sdEmail, sdPhoneNumber, sdPassword, sdRole)

                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            println("Registration success.")

                            val intent = Intent(applicationContext, Login::class.java)
                            startActivity(intent);
                            Toast.makeText(applicationContext, "Account created successfully", Toast.LENGTH_SHORT).show()

                        } else {
                            println("Register failed. Response code: ${response.code()}")
                            Toast.makeText(applicationContext, "Registration failed. Check the fields!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        println("Register request failed. Error: ${t.message}")
                        Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}