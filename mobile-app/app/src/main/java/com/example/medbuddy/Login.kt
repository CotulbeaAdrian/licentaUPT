package com.example.medbuddy

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medbuddy.api.ApiServiceBuilder
import com.example.medbuddy.api.data.LoginRequest
import com.example.medbuddy.api.data.LoginResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

                val apiService = ApiServiceBuilder.apiService
                // Make the login request
                println(email)
                println(password)
                val call = apiService.login(email,password)

                // Execute the request asynchronously
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            println("Login successful. Response: $responseBody")
                        } else {
                            println("Login failed. Response code: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        println("Login request failed. Error: ${t.message}")
                    }
                })
            }
        }
    }
}