package com.example.suikagame

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var leaderboardContainer: LinearLayout
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val prefs = getSharedPreferences("suika_prefs", MODE_PRIVATE)

        val backgroundColor = prefs.getInt("background_color", android.graphics.Color.parseColor("#c7a875"))

        Log.w("yogurt", "backgroundColor$backgroundColor")

        val rootView: View = findViewById(R.id.scrollView)
        rootView.setBackgroundColor(backgroundColor)

        leaderboardContainer = findViewById(R.id.leaderboardContainer)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        val ref = FirebaseDatabase.getInstance().getReference("leaderboard")
        ref.orderByChild("score").limitToLast(10).get().addOnSuccessListener { dataSnapshot ->
            val tempList = mutableListOf<Pair<String, Int>>()
            for (child in dataSnapshot.children) {
                val score = child.child("score").getValue(Int::class.java) ?: 0
                val username = child.child("username").getValue(String::class.java) ?: "Anonymous"
                tempList.add(username to score)
            }
            tempList.sortByDescending { it.second }

            tempList.forEachIndexed { index, (name, score) ->
                val row = TextView(this).apply {
                    text = "${index + 1}. $name - $score"
                    textSize = 18f
                    setPadding(0, 8, 0, 8)
                }
                leaderboardContainer.addView(row, leaderboardContainer.childCount - 1)
            }
        }.addOnFailureListener {
            Log.e("Leaderboard", "Failed to load leaderboard", it)
        }
    }
}
