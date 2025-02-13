package com.luxoft.virtualdisplay.chatGptExample

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luxoft.virtualdisplay.R

class ChatGptSecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_gpt_second)
        val width = intent.getIntExtra("WIDTH", 800)
        val height = intent.getIntExtra("HEIGHT", 600)

        // Use the width and height as needed
        Toast.makeText(this, "Second Activity Started with width: $width, height: $height", Toast.LENGTH_SHORT).show()

    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("ChatGptSecondActivity", "Activity destroyed")
    }
}

class MyPresentation(context: Context, display: Display) : Presentation(context, display) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_gpt_second)

        findViewById<Button>(R.id.btnClickMe).setOnClickListener {
            Toast.makeText(context, "Button Clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}