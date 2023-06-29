package com.example.medbuddy.presentation.patient

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.main.MainActivity
import com.example.medbuddy.presentation.chat.MessageAdapter
import com.example.medbuddy.R
import com.example.medbuddy.data.sharedpref.api.ApiServiceBuilder
import com.example.medbuddy.entities.Message
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PatientInteractionActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    private lateinit var doctorFullName: TextView
    private lateinit var symptomTV: TextView
    private lateinit var diagnosticTV: TextView
    private lateinit var medicationTV: TextView
    private lateinit var mDialog: Dialog
    private lateinit var mnDialog: Dialog

    private companion object {
        private const val CHANNEL_ID = "channel01"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.patient_interaction)
        findViewById<ImageView>(R.id.BackButton).setOnClickListener {
            val intent = Intent(this, PatientDashboardActivity::class.java)
            startActivity(intent)
        }

        val interactionID = intent.getStringExtra("recordID")
        val symptom = intent.getStringExtra("symptom")
        val diagnostic = intent.getStringExtra("diagnostic")
        val medication = intent.getStringExtra("medication")
        val patientID = intent.getStringExtra("patientID")
        val doctorID = intent.getStringExtra("doctorID")

        val apiService = ApiServiceBuilder.apiService

        doctorFullName = findViewById(R.id.doctorPatientTitle)
        doctorFullName.text = intent.getStringExtra("doctorFullName")

        diagnosticTV = findViewById(R.id.diagnostic)
        diagnosticTV.text = diagnostic

        medicationTV = findViewById(R.id.medication)
        medicationTV.text = medication

        symptomTV = findViewById(R.id.symptom)
        symptomTV.text = symptom



        findViewById<LinearLayout>(R.id.layoutGiveReview).setOnClickListener {
            Toast.makeText(
                this,
                "Yet to be implemented.\nEverybody gets 5/5 for effort.",
                Toast.LENGTH_SHORT
            ).show()
        }

        val reminderButton = findViewById<LinearLayout>(R.id.layoutReminder)
        reminderButton.setOnClickListener {
            mDialog = Dialog(this)
            mDialog.setContentView(R.layout.pop_up_reminder_set)
            mDialog.setTitle("Pop-up Window")
            mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mDialog.window!!.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val setFixedReminder = mDialog.findViewById<Button>(R.id.setFixedReminder)
            setFixedReminder.setOnClickListener {
                showNotification(0)
                mDialog.dismiss()
            }
            val setRepetitiveReminder = mDialog.findViewById<Button>(R.id.setRepetitiveReminder)
            setRepetitiveReminder.setOnClickListener {
                mDialog.dismiss()
                mnDialog = Dialog(this)
                mnDialog.setContentView(R.layout.pop_up_reminder_hour)
                mnDialog.setTitle("Pop-up Window")
                mnDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mnDialog.window!!.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val saveReminder = mnDialog.findViewById<Button>(R.id.saveReminder)
                val hoursTextInput = mnDialog.findViewById<TextInputLayout>(R.id.bookingHours)
                saveReminder.setOnClickListener {

                    val hoursString = hoursTextInput.editText!!.text.toString()
                    if (hoursString.isNotEmpty()) {
                        val hours = hoursString.toInt()
                        try {
                            showNotification(hours)
                            mnDialog.dismiss()
                        } catch (e: NumberFormatException) {
                            // Display an error message to the user if the string cannot be converted to an integer
                            Toast.makeText(
                                this,
                                "Please enter a valid number of hours",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Display an error message to the user if the string is empty
                        Toast.makeText(this, "Please enter a number of hours", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                mnDialog.show()
            }
            mDialog.show()
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        val auxCall = apiService.getMessages(interactionID)
        auxCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val entries = responseBody?.split("&") ?: emptyList()
                    for (entry in entries) {
                        val lines = entry.split("\n")
                        val messageDataMap = mutableMapOf<String, String>()
                        for (line in lines) {
                            val keyValue = line.split("=")
                            if (keyValue.size == 2) {
                                val key = keyValue[0].trim()
                                val value = keyValue[1].trim()
                                messageDataMap[key] = value
                            }
                        }
                        if (messageDataMap["roomID"]?.isNotBlank() == true) {
                            val id = messageDataMap["roomID"]
                            val message = messageDataMap["message"]
                            val senderID = messageDataMap["senderID"]
                            val receiverID = messageDataMap["receiverID"]
                            // Create message object
                            val auxMessage = Message(
                                message.orEmpty(),
                                senderID.orEmpty(),
                                receiverID.orEmpty(),
                                id.orEmpty()
                            )
                            messageList.add(auxMessage)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                } else {
                    Log.d("ERROR", "Receive messages failed. Response code: ${response.code()}")
                    Toast.makeText(
                        applicationContext,
                        "Receive messages failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("ERROR", "Request failed. Error: ${t.message}")
                Toast.makeText(applicationContext, "Server error. Try again!", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val aux2Call = apiService.sendMessage(interactionID, patientID, doctorID, message)
            aux2Call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val auxMessage = Message(
                            message,
                            patientID.orEmpty(),
                            doctorID.orEmpty(),
                            interactionID.orEmpty()
                        )
                        messageList.add(auxMessage)
                        messageAdapter.notifyDataSetChanged()
                    } else {
                        Log.d(
                            "ERROR",
                            "Message failed to be sent. Response code: ${response.code()}"
                        )
                        Toast.makeText(
                            applicationContext,
                            "Message failed to be sent.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("ERROR", "Message failed to be sent serverside. Error: ${t.message}")
                    Toast.makeText(
                        applicationContext,
                        "Server error. Try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            messageBox.setText("")
        }
    }


    private fun showNotification(hours: Int) {
        createNotificationChannel(hours)
        val date = Date()
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.US).format(date).toInt()
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.ic_medication)
        notificationBuilder.setContentTitle("Medication")
        notificationBuilder.setContentText("Short description")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(hours: Int) {
        val name: CharSequence = "My medication"
        val description = "Medication channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
        notificationChannel.description = description
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val currentTime = Calendar.getInstance().timeInMillis
        val triggerTime =
            currentTime + (hours * 60 * 60 * 1000) // hours * minutes * seconds * milliseconds
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    }
}