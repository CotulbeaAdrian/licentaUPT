package com.example.medbuddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException

class Register : AppCompatActivity() {

    private val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val username = findViewById<TextInputLayout>(R.id.usernameRegister)
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
            val sdUsername = username.editText?.text.toString()
            val sdPassword = password.editText?.text.toString()
            val sdFullName = fullName.editText?.text.toString()
            val sdPhoneNumber = phoneNumber.editText?.text.toString()
            val sdEmail = email.editText?.text.toString()
            val sdConfirmPassword = confirmPassword.editText?.text.toString()
            val sdRole = spinner.selectedItem.toString()

            if (sdUsername.isEmpty() || sdEmail.isEmpty() || sdPassword.isEmpty() ||
                sdFullName.isEmpty() || sdPhoneNumber.isEmpty() || sdConfirmPassword.isEmpty())
            {
                if (username.editText?.text.toString().isEmpty()) {
                    username.error = "Please enter your username!"
                }
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

//                val client = OkHttpClient()
//
//                val requestBody = ("email=$sdEmail&password=$sdPassword&" +
//                        "username=$sdUsername&phoneNumber=$sdPhoneNumber&" +
//                        "fullName=$sdFullName&role=$sdRole").trimIndent()
//
//                val mediaType = "application/x-www-form-urlencoded".toMediaType()
//
//                val request = Request.Builder()
//                    .url("http://192.168.0.106:8080/register")
//                    .post(requestBody.toRequestBody(mediaType))
//                    .build()

//                println(request.body.toString())
//                client.newCall(request).enqueue(object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        // Handle the login failure
//                        println("Registration failed: ${e.message}")
//                    }

//                    override fun onResponse(call: Call, response: Response) {
//                        if (response.isSuccessful) {
//                            val intent = Intent(, Login::javaClass)
//                            startActivity(intent)
//                            Toast.makeText(this, "Registered successfully.", Toast.LENGTH_SHORT).show()
//                        } else {
//                            // Handle the login failure
//                            println("Registration failed failed: ${response.code}")
//                        }
//                    }
//                })
            }
        }
    }
}