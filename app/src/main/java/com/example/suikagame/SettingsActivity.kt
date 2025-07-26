package com.example.suikagame

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var seekBarCooldown: SeekBar
    private lateinit var ratingBar: RatingBar
    private lateinit var colorSpinner: Spinner
    private lateinit var fruitSizeSeekBar: SeekBar
    private lateinit var backButton : Button
    private lateinit var resetButton : Button
    private val prefs by lazy {
        getSharedPreferences("suika_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        seekBarCooldown = findViewById(R.id.seekBarCooldown)
        ratingBar = findViewById(R.id.ratingBar)
        colorSpinner = findViewById(R.id.colorSpinner)
        fruitSizeSeekBar = findViewById(R.id.seekBarFruitSize)
        backButton = findViewById(R.id.buttonBack)
        resetButton = findViewById(R.id.buttonResetPrefs)

        // Load values
        seekBarCooldown.progress = prefs.getInt("drop_cooldown", 800)
        ratingBar.rating = prefs.getFloat("user_rating", 0f)
        fruitSizeSeekBar.progress = prefs.getInt("fruit_scale", 100)

        val colors = listOf("Gray", "Green", "Brown", "Blue")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = adapter

        // Preselect current color
        val currentColor = prefs.getInt("background_color", Color.parseColor("#c7a875"))
        colorSpinner.setSelection(colors.indexOf(colorNameFromValue(currentColor)))

        // Listeners
        seekBarCooldown.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("drop_cooldown", progress).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        fruitSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("fruit_scale", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            prefs.edit().putFloat("user_rating", rating).apply()
        }

        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedColor = when (colors[position]) {
                    "Gray" -> Color.parseColor("#D6CFC7")
                    "Green" -> Color.parseColor("#bde3c0")
                    "Brown" -> Color.parseColor("#c7a875")
                    "Blue" -> Color.parseColor("#b4c5e4")
                    else -> Color.parseColor("#c7a875")
                }
                prefs.edit().putInt("background_color", selectedColor).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        backButton.setOnClickListener {
            finish()
        }

        resetButton.setOnClickListener {
            prefs.edit().clear().apply()
        }
    }

    private fun colorNameFromValue(color: Int): String {
        return when (color) {
            Color.parseColor("#D6CFC7") -> "Gray"
            Color.parseColor("#bde3c0") -> "Green"
            Color.parseColor("#c7a875") -> "Brown"
            Color.parseColor("#b4c5e4") -> "Blue"
            else -> "Brown"
        }
    }
}
