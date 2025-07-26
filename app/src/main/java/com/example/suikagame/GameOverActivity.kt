package com.example.suikagame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class GameOverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val prefs = getSharedPreferences("suika_prefs", MODE_PRIVATE)
        val score = prefs.getInt("last_score", 0)

        val backgroundColor = prefs.getInt("background_color", android.graphics.Color.parseColor("#c7a875"))

        Log.w("yogurt", "backgroundColor$backgroundColor")


        val rootView: View = findViewById(R.id.rootLayout)
        rootView.setBackgroundColor(backgroundColor)

        val scoreText: TextView = findViewById(R.id.textScore)
        scoreText.text = "Your Score: $score"

        val restartButton: Button = findViewById(R.id.playAgainButton)
        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val leaderboardButton: Button = findViewById(R.id.buttonLeaderboard)
        leaderboardButton.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }


        val editUsername = findViewById<EditText>(R.id.editUsername)
        val submitButton = findViewById<Button>(R.id.buttonSubmitScore)

        val savedUsername = prefs.getString("username", "")
        editUsername.setText(savedUsername)

        submitButton.setOnClickListener {
            val username = editUsername.text.toString().ifBlank { "Anonymous" }

            prefs.edit().putString("username", username).apply()

            submitScoreToFirebase(score, username)
            Toast.makeText(this, "Score submitted!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun submitScoreToFirebase(score: Int, username: String = "Anonymous") {
        val database = FirebaseDatabase.getInstance()
        val leaderboardRef = database.getReference("leaderboard")

        val entry = mapOf(
            "username" to username,
            "score" to score,
            "timestamp" to System.currentTimeMillis()
        )
        leaderboardRef.push().setValue(entry)
    }

}