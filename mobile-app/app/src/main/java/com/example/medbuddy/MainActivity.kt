package com.example.medbuddy

import android.os.Bundle
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)

        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.splash)

        val image = findViewById<ImageView>(R.id.medbuddy_image)
        val title = findViewById<TextView>(R.id.medbuddy_title)
        val tagline = findViewById<TextView>(R.id.medbuddy_tagline)

        val topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val botAnim = AnimationUtils.loadAnimation(this, R.anim.bot_animation)

        image.animation = topAnim
        title.animation = botAnim
        tagline.animation = botAnim

    }
}