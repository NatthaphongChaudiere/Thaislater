package com.example.thaislater

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.MotionEvent
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.graphics.toColorInt
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // wait longer to connect
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // wait longer for server response
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val startButton = findViewById<LinearLayout>(R.id.start_button)

        onTouch_startButton(startButton)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun onTouch_startButton(button: LinearLayout) {
        button.setOnTouchListener { view, motionEvent ->
            // Access the shape drawable from background
            val drawable = view.background
            if (drawable is GradientDrawable) {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Scale up
                        view.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(100)
                            .start()

                        // Set background color to green
                        drawable.setColor("#4CAF50".toColorInt()) // green
                    }

                    MotionEvent.ACTION_UP -> {
                        // Scale down
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()

                        // Reset background color to white
                        drawable.setColor(Color.WHITE)

                        // For accessibility
                        view.performClick()
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        // Restore original size and color
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()

                        drawable.setColor(Color.WHITE)
                    }
                }
            }

            true
        }

        // Optional: Define what happens on click
        button.setOnClickListener {
            val username = findViewById<EditText>(R.id.username_field).text.toString()
            if (username.isNullOrBlank()) {
                Toast.makeText(this, "Please enter username...", Toast.LENGTH_SHORT).show()
            }
            else {
                val jsonBody = JSONObject()
                jsonBody.put("username", username)

                val requestBody = RequestBody.create(
                    "application/json; charset=utf-8".toMediaType(),
                    jsonBody.toString()
                )

                val request = Request.Builder()
                    .url("http://10.0.2.2:5000/create-user")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Log.e("FlaskAPI_FailedReport", "Failed: ${e.message}")
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {
                            val resString = response.body?.string()
                            Log.d("FlaskAPI_EncodeJSON", "$resString")
                        }
                    }
                })
            }
        }
    }

}