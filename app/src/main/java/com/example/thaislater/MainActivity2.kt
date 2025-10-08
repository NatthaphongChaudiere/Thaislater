package com.example.thaislater

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.LottieAnimationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity2 : AppCompatActivity() {

    private lateinit var lottie_loading_activities: LottieAnimationView
    private var user_id: Int? = null
    private lateinit var username: String
    private lateinit var date_created: String
    private lateinit var time_created: String
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // wait longer to connect
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // wait longer for server response
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent: Intent = getIntent()
        user_id = intent.getIntExtra("User_ID", 0)
        username = intent.getStringExtra("Username")?: "Guest"
        date_created = intent.getStringExtra("Date_Created")?: "Unknown"
        time_created = intent.getStringExtra("Time_Created")?: "Unknown"

        Log.d("User Information", "$user_id")
        Log.d("User Information", "$username")
        Log.d("User Information", "$date_created")
        Log.d("User Information", "$time_created")

        lottie_loading_activities = findViewById(R.id.lottie_loading_activities)
        val generateButton = findViewById<LinearLayout>(R.id.generate_button)
        val favnavButton = findViewById<ImageButton>(R.id.favorite_navigation)
        val user_setting = findViewById<ImageButton>(R.id.user_setting_navigation)
        onTouch_generateButton(generateButton)
        onTouch_navigation(favnavButton)
        onTouch_navigation(user_setting)

    }
    @SuppressLint("ClickableViewAccessibility")
    fun onTouch_generateButton(button: LinearLayout) {
        button.setOnTouchListener { view, motionEvent ->
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
                        drawable.setColor("#9370DB".toColorInt()) // purple
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

        button.setOnClickListener {
            // UI Session
            val translate_pronounciation_layout = findViewById<LinearLayout>(R.id.translate_pronounciation_layout)
            val original_text = findViewById<TextView>(R.id.original_text)
            val original_thai_text = findViewById<TextView>(R.id.original_thai_text)
            val pronounciation = findViewById<TextView>(R.id.pronounciation)
            val daily_travel_slang = findViewById<LinearLayout>(R.id.daily_travel_slang)

            val thai_cultural_note_header = findViewById<TextView>(R.id.thai_cultural_note_header)

            val thai_cultural_note_layout = findViewById<LinearLayout>(R.id.thai_cultural_note_layout)
            val cultural_text = findViewById<TextView>(R.id.cultural)

            val favoriteButton = findViewById<ImageButton>(R.id.favoriteButton)
            var favorite_state = false

            var current_favorite_id = 0


            val prompt = findViewById<EditText>(R.id.prompt_field).text.toString()
            if (prompt.isNullOrBlank()) {
                Toast.makeText(this, "Please enter prompt...", Toast.LENGTH_SHORT).show()
            }
            else {

                translate_pronounciation_layout.visibility = View.GONE
                thai_cultural_note_header.visibility = View.GONE
                thai_cultural_note_layout.visibility = View.GONE

                favoriteButton.setBackgroundResource(R.drawable.unfavourite)
                favoriteButton.visibility = View.GONE

                daily_travel_slang.visibility = View.GONE


                val jsonBody = JSONObject()
                jsonBody.put("phrase", prompt)

                val requestBody = RequestBody.create(
                    "application/json; charset=utf-8".toMediaType(),
                    jsonBody.toString()
                )

                val request = Request.Builder()
                    .url("http://10.0.2.2:5000/cultural_note")
                    .post(requestBody)
                    .build()

                lottie_loading_activities.visibility = View.VISIBLE

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            lottie_loading_activities.visibility = View.GONE
                            Log.e("FlaskAPI_FailedReport", "Failed: ${e.message}")
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {
                            lottie_loading_activities.visibility = View.GONE
                            val resString = response.body?.string()
                            Log.d("FlaskAPI_EncodeJSON", "$resString")
                            enableUI(resString)
                        }
                    }

                    fun enableUI(resString: String?) {
                        val response = JSONObject(resString)
                        original_text.text = "Original Text: " + response.getString("Original")
                        original_thai_text.text = "Original Thai Text: " + response.getString("Original-Thai")
                        pronounciation.text = "Pronounciation: " + response.getString("Pronounciation")

                        cultural_text.text = response.getString("Thai Cultural Note")

                        translate_pronounciation_layout.visibility = View.VISIBLE
                        thai_cultural_note_header.visibility = View.VISIBLE
                        thai_cultural_note_layout.visibility = View.VISIBLE
                        favoriteButton.visibility = View.VISIBLE

                        favoriteButton.setOnClickListener {
                            favoriteCurrent(response)
                        }
                    }

                    fun favoriteCurrent(response: JSONObject?){
                        if (!favorite_state) {
                            favoriteButton.setBackgroundResource(R.drawable.favourite)
                            favorite_state = true

                            val jsonBody = JSONObject()
                            jsonBody.put("user_id", user_id)
                            jsonBody.put("original_text", response?.getString("Original"))
                            jsonBody.put("original_thai_text", response?.getString("Original-Thai"))
                            jsonBody.put("pronounciation", response?.getString("Pronounciation"))
                            jsonBody.put("cultural_text", response?.getString("Thai Cultural Note"))

                            val requestBody = RequestBody.create(
                                "application/json; charset=utf-8".toMediaType(),
                                jsonBody.toString()
                            )

                            val request = Request.Builder()
                                .url("http://10.0.2.2:5000/create-favorite")
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
                                        val response = JSONObject(resString)
                                        current_favorite_id = response.getInt("id")
                                        Log.d("FlaskAPI_EncodeJSON", "$resString")
                                    }
                                }
                            })
                        }
                        else {
                            favoriteButton.setBackgroundResource(R.drawable.unfavourite)
                            favorite_state = false

                            val jsonBody = JSONObject()
                            jsonBody.put("current_frame_id", current_favorite_id)

                            val requestBody = RequestBody.create(
                                "application/json; charset=utf-8".toMediaType(),
                                jsonBody.toString()
                            )

                            val request = Request.Builder()
                                .url("http://10.0.2.2:5000/delete-favorite")
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
                })
            }
        }
    }
    fun onTouch_navigation(button : ImageButton) {
        button.setOnClickListener {
            val buttonContent = button.contentDescription.toString()

            val targetIntent = when (buttonContent) {
                "favorite" -> Intent(this@MainActivity2, MainActivity3::class.java)
                "user_setting" -> Intent(this@MainActivity2, MainActivity4::class.java)
                else -> null
            }

            targetIntent?.let {
                it.putExtra("User_ID", user_id)
                it.putExtra("Username", username)
                it.putExtra("Date_Created", date_created)
                it.putExtra("Time_Created", time_created)

                startActivity(it)
            }
        }
    }
}