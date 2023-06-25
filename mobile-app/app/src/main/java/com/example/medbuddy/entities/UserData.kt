package com.example.medbuddy.entities

data class UserData(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String
) {
    override fun toString(): String {
        return "UserData(id='$id', fullName='$fullName', email='$email', phoneNumber='$phoneNumber', role='$role')"
    }
}