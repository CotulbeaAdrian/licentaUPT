package com.example.medbuddy.data

data class MedicalRecord(
    var id: String,
    var active: String,
    var accepted: String,
    var patientID: String,
    var doctorID: String,
    var symptom: String,
    var diagnostic: String,
    var medication: String,
    var specialty: String
) {
    override fun toString(): String {
        return "MedicalRecord(id='$id', doctorID='$doctorID', patientID='$patientID', " +
                "symptom='$symptom', diagnostic='$diagnostic', medication='$medication', accepted=$accepted, " +
                "active=$active), specialty=$specialty"
    }
}