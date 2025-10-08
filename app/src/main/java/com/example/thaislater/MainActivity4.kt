package com.example.thaislater

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class MainActivity4 : AppCompatActivity() {

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

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main4)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val home_navigation = findViewById<ImageButton>(R.id.home_navigation)
        val favorite_nav = findViewById<ImageButton>(R.id.favorite_navigation)
        val account_delete_button = findViewById<Button>(R.id.delete_account)
        lottie_loading_activities = findViewById(R.id.lottie_loading_activities)

        user_id = intent.getIntExtra("User_ID", 0)
        username = intent.getStringExtra("Username") ?: "Guest"
        date_created = intent.getStringExtra("Date_Created") ?: "No Date"
        time_created = intent.getStringExtra("Time_Created") ?: "No Time"

        settingup_profile()
        delete_account(account_delete_button)
        onTouch_navigation(home_navigation)
        onTouch_navigation(favorite_nav)

    }
    fun onTouch_navigation(button : ImageButton) {
        button.setOnClickListener {
            val buttonContent = button.contentDescription.toString()

            val targetIntent = when (buttonContent) {
                "home" -> Intent(this@MainActivity4, MainActivity2::class.java)
                "favorite" -> Intent(this@MainActivity4, MainActivity3::class.java)
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

    fun settingup_profile() {
        val username_profile = findViewById<TextView>(R.id.username)
        val account_created_profile = findViewById<TextView>(R.id.account_created)

        username_profile.text = "@${username}"
        account_created_profile.text = "Account created: ${date_created}"
    }

    fun delete_account(button : Button) {
        button.setOnClickListener {
            val jsonBody = JSONObject()
            jsonBody.put("user_id", user_id)

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaType(),
                jsonBody.toString()
            )

            val request = Request.Builder()
                .url("http://10.0.2.2:5000/delete-account")
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
                    }
                    val intent : Intent = Intent(this@MainActivity4, MainActivity::class.java)
                    startActivity(intent)
                }
            })

        }
    }
}