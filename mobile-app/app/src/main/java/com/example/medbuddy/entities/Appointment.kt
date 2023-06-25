package com.example.medbuddy.entities

data class Appointment(
    var id: String,
    var active: String,
    var accepted: String,
    var patientID: String,
    var doctorID: String,
    var date: String,
    var location: String,
    var specialty: String
) {
    override fun toString(): String {
        return "MedicalRecord(id='$id', doctorID='$doctorID', patientID='$patientID', " +
                "date='$date', location='$location', accepted=$accepted, " +
                "active=$active), specialty=$specialty"
    }
}