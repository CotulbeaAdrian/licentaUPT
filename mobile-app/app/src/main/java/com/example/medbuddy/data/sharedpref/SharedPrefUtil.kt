package com.example.medbuddy.data.sharedpref

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.medbuddy.entities.UserData

object SharedPrefUtil {
    private const val KEY_ID = "id"
    private const val KEY_FULL_NAME = "fullName"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE_NUMBER = "phoneNumber"
    private const val KEY_ROLE = "role"

    fun saveUserData(context: Context, userData: UserData) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.putString(KEY_ID, userData.id)
        editor.putString(KEY_FULL_NAME, userData.fullName)
        editor.putString(KEY_EMAIL, userData.email)
        editor.putString(KEY_PHONE_NUMBER, userData.phoneNumber)
        editor.putString(KEY_ROLE, userData.role)
        editor.apply()
    }

    fun getUserData(context: Context): UserData {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val id = sharedPrefs.getString(KEY_ID, "") ?: ""
        val fullName = sharedPrefs.getString(KEY_FULL_NAME, "") ?: ""
        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""
        val phoneNumber = sharedPrefs.getString(KEY_PHONE_NUMBER, "") ?: ""
        val role = sharedPrefs.getString(KEY_ROLE, "") ?: ""
        return UserData(id, fullName, email, phoneNumber, role)
    }

    fun clearUserData(context: Context) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.remove(KEY_ID)
        editor.remove(KEY_FULL_NAME)
        editor.remove(KEY_EMAIL)
        editor.remove(KEY_PHONE_NUMBER)
        editor.remove(KEY_ROLE)
        editor.apply()
    }

    fun toString(context: Context) {
        val userData = getUserData(context)
        println(userData.id + " " + userData.fullName + " " + userData.role)
    }
}