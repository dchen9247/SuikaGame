package com.example.suikagame

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import com.google.android.gms.ads.*

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var detector: GestureDetector
    private var hasInitialized : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !hasInitialized) {
            buildViewByCode()
            hasInitialized = true
        }
    }

    private fun buildViewByCode() {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        val statusBarRect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(statusBarRect)
        val statusBarHeight = statusBarRect.top

        val rootLayout = FrameLayout(this)

        val adHeightPx = AdSize.BANNER.getHeightInPixels(this)
        val extraPadding = (36 * resources.displayMetrics.density).toInt()  // 32dp padding
        val topOffset = adHeightPx + extraPadding
        gameView = GameView(this, width, height - statusBarHeight, topOffset)
        rootLayout.addView(gameView)

        // create ad
        val adView = AdView(this)
        val adSize : AdSize = AdSize( AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT )
        adView.setAdSize(adSize)
        val adUnitId : String = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId
        adView.loadAd(AdRequest.Builder().build())

        val adParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 0
        }

        rootLayout.addView(adView, adParams)

        setContentView(rootLayout)

        val timer = Timer()
        val task = GameTimerTask(this)
        timer.schedule(task, 0, 30)
    }

    fun updateModel() {
        val game = gameView.getGame()
        game.update()

        if (game.isGameOver()) {
            val prefs = getSharedPreferences("suika_prefs", MODE_PRIVATE)
            prefs.edit().putInt("last_score", game.getScore()).apply()
            val intent = Intent(this, GameOverActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun updateView() {
        gameView.invalidate()
    }
}


