package com.example.thaislater

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity3 : AppCompatActivity() {

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
        setContentView(R.layout.activity_main3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val user_favorite_title = findViewById<TextView>(R.id.user_fav_header)
        val home_navigation = findViewById<ImageButton>(R.id.home_navigation)
        val user_setting = findViewById<ImageButton>(R.id.user_setting_navigation)
        val intent : Intent = getIntent()

        user_id = intent.getIntExtra("User_ID", 0)
        username = intent.getStringExtra("Username") ?: "Guest"
        date_created = intent.getStringExtra("Date_Created") ?: "No Date"
        time_created = intent.getStringExtra("Time_Created") ?: "No Time"

        user_favorite_title.text = "$username's Favorites"
        onTouch_navigation(user_setting)
        onTouch_navigation(home_navigation)
        checkuser_favorites(user_id)
    }

    fun checkuser_favorites(user_id: Int?) {

        val container = findViewById<LinearLayout>(R.id.containerFavorites)
        // Replace with your Flask API URL
        val url = "http://10.0.2.2:5000/get-favorite?user_id=$user_id"

        // Build request
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        // Execute request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("Error: ${response.code}")
                    return
                }

                val responseData = response.body?.string()
                if (responseData != null) {
                    val favoritesJson = JSONArray(responseData)

                    runOnUiThread {
                        for (i in 0 until favoritesJson.length()) {
                            val item = favoritesJson.getJSONObject(i)
                            val view = layoutInflater.inflate(R.layout.favorite_item, container, false)

                            val textFrameID = view.findViewById<TextView>(R.id.FrameID)
                            val textOriginal = view.findViewById<TextView>(R.id.Original)
                            val textOriginalThai = view.findViewById<TextView>(R.id.Original_Thai)
                            val textPronounciation = view.findViewById<TextView>(R.id.Pronounciation)
                            val textDate = view.findViewById<TextView>(R.id.Added_Date)
                            val textTime = view.findViewById<TextView>(R.id.Added_Time)
                            val favoriteButton = view.findViewById<ImageButton>(R.id.favoriteButton)

                            textFrameID.text = item.getString("id")
                            textOriginal.text = item.getString("original")
                            textOriginalThai.text = item.getString("original_thai")
                            textPronounciation.text = item.getString("pronounciation")
                            textDate.text = item.getString("date")
                            textTime.text = item.getString("time")

                            favoriteButton.setOnClickListener {
                                unfavorite_word(view, container)
                            }
                            container.addView(view)
                        }
                    }
                }
            }
        })
    }

    fun unfavorite_word(current_view: View, container: LinearLayout) {

        val current_frameID = current_view.findViewById<TextView>(R.id.FrameID)

        val jsonBody = JSONObject()
        jsonBody.put("current_frame_id", current_frameID.text.toString())

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
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("Error: ${response.code}")
                } else {
                    runOnUiThread {
                        container.removeView(current_view)
                    }
                }
            }

        })
    }
    fun onTouch_navigation(button : ImageButton) {
        button.setOnClickListener {
            val buttonContent = button.contentDescription.toString()

            val targetIntent = when (buttonContent) {
                "home" -> Intent(this@MainActivity3, MainActivity2::class.java)
                "user_setting" -> Intent(this@MainActivity3, MainActivity4::class.java)
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