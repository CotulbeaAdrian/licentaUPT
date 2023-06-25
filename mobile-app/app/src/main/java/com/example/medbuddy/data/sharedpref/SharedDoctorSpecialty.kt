package com.example.medbuddy.data.sharedpref

import android.content.Context
import androidx.preference.PreferenceManager

object SharedDoctorSpecialty {
    private const val KEY_SPECIALTY = "specialty"

    fun saveSpecialty(context: Context, specialty: String) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        val auxSpecialty = specialty.replace("\n", "")
        editor.putString(KEY_SPECIALTY, auxSpecialty)
        editor.apply()
    }

    fun getSpecialty(context: Context): String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getString(KEY_SPECIALTY, "") ?: "";
    }

    fun clearSpecialtyMedic(context: Context) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.remove(KEY_SPECIALTY)
        editor.apply()
    }
}