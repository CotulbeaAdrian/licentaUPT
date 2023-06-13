package com.example.medbuddy

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Login : AppCompatActivity() {
    private val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.login)

        val newUser = findViewById<Button>(R.id.newUser)
        val logInEmail = findViewById<TextInputLayout>(R.id.emailLogIn)
        val logInPassword = findViewById<TextInputEditText>(R.id.passwordLogIn)
        val loginButton = findViewById<Button>(R.id.loginButton)

        newUser.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = logInEmail.editText?.text.toString()
            val password = logInPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    logInEmail.error = "Please enter a username"
                }
                if (password.isEmpty()) {
                    logInPassword.error = "Please enter your password"
                }
                Toast.makeText(this, "Please enter valid credentials", Toast.LENGTH_SHORT).show()
            } else if (!email.matches(emailRegex.toRegex())) {
                logInEmail.error = "Please enter a valid email address"
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT)
                    .show()
            } else if (password.length < 7) {
                logInPassword.error = "Please enter a password with more than 7 characters"
                Toast.makeText(
                    this, "Please enter a password with more than 7 characters", Toast.LENGTH_SHORT
                ).show()
            } else {

                val client = OkHttpClient()

                val requestBody = "email=$email&password=$password".trimIndent()

                val mediaType = "application/x-www-form-urlencoded".toMediaType()

                val request = Request.Builder()
                    .url("http://192.168.0.106:8080/login")
                    .post(requestBody.toRequestBody(mediaType))
                    .build()

                println(request.body.toString())
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // Handle the login failure
                        println("Login failed: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            // Process the successful login response
                            println("Login successful: $responseData")
                            responseData
                        } else {
                            // Handle the login failure
                            println("Login failed: ${response.code}")
                        }
                    }
                })

//                            if (task.isSuccessful) {
//                                val snapshot = task.result
//                                val role = snapshot.child("role").getValue(String::class.java)
//                                val fullName =
//                                    snapshot.child("fullName").getValue(String::class.java)
//                                val phoneNumber =
//                                    snapshot.child("phoneNumber").getValue(String::class.java)
//                                if (role.equals("Medic")) {
//                                    val intent = Intent(this, DoctorDashboard::class.java)
//                                    intent.putExtra("fullName", fullName)
//                                    intent.putExtra("phoneNumber", phoneNumber)
//                                    startActivity(intent)
//                                    Toast.makeText(this, "Log in as Medic", Toast.LENGTH_SHORT).show()
//                                } else {
//                                    val intent = Intent(this, PatientDashboard::class.java)
//                                    intent.putExtra("fullName", fullName)
//                                    intent.putExtra("phoneNumber", phoneNumber)
//                                    startActivity(intent)
//                                    Toast.makeText(this, "Log in as Patient ", Toast.LENGTH_SHORT).show()
//                                }
//                            } else {
//                                Log.d(
//                                    "TAG", task.exception!!.message!!
//                                ) //Don't ignore potential errors!
//                            }
//                        }
//                    } else {
//                        Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show()
//                    }
                }
            }
        }
}