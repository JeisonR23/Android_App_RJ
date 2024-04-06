package com.example.taskapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.loginButton)
        btn.setOnClickListener {
            var intent: Intent = Intent(this, Pantalla2::class.java)
            startActivity(intent)
        }

    }
}

