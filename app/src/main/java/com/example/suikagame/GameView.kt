package com.example.suikagame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.suika.Game

class GameView: View {
    private lateinit var paint: Paint
    private lateinit var jarBitmap: Bitmap
    private lateinit var bubbleBitmap: Bitmap
    private lateinit var settingsBitmap : Bitmap
    private lateinit var settingsRect : Rect
    private lateinit var fruits: Array<Bitmap>
    private lateinit var jarRect: Rect
    private lateinit var fruitBubbleRect: Rect
    private lateinit var game : Game
    private lateinit var scoreBubbleRect: Rect
    private var prefs : SharedPreferences = context.getSharedPreferences("suika_prefs", Context.MODE_PRIVATE)
    private var currentBackgroundColor: Int = prefs.getInt("background_color", Color.parseColor("#c7a875"))
    constructor (context : Context) : super(context) {

    }

    constructor(context: Context, screenWidth: Int, screenHeight: Int, topOffset: Int) : super(context) {        paint = Paint()
        paint.strokeWidth = 20f
        paint.isAntiAlias = true

        jarBitmap = BitmapFactory.decodeResource(resources, R.drawable.jar)
        bubbleBitmap = BitmapFactory.decodeResource(resources, R.drawable.bubble)
        settingsBitmap = BitmapFactory.decodeResource(resources, R.drawable.settings)

        fruits = arrayOf(
            BitmapFactory.decodeResource(resources, R.drawable.cherry_1),
            BitmapFactory.decodeResource(resources, R.drawable.strawberry_2),
            BitmapFactory.decodeResource(resources, R.drawable.grape_3),
            BitmapFactory.decodeResource(resources, R.drawable.dekopon_4),
            BitmapFactory.decodeResource(resources, R.drawable.orange_5),
            BitmapFactory.decodeResource(resources, R.drawable.apple_6),
            BitmapFactory.decodeResource(resources, R.drawable.pear_7),
            BitmapFactory.decodeResource(resources, R.drawable.peach_8),
            BitmapFactory.decodeResource(resources, R.drawable.pineapple_9),
            BitmapFactory.decodeResource(resources, R.drawable.melon_10),
            BitmapFactory.decodeResource(resources, R.drawable.watermelon_11)
        )


        val bubbleBitmapRatio = bubbleBitmap.height.toFloat() / bubbleBitmap.width

        val fruitBubbleWidth = (screenWidth / 3.5)  // smaller than score
        val fruitBubbleHeight = (fruitBubbleWidth * bubbleBitmapRatio).toInt()
        val fruitBubbleLeft = (screenWidth - fruitBubbleWidth - 16).toInt()
        val fruitBubbleTop = 320 + topOffset
        fruitBubbleRect = Rect(
            fruitBubbleLeft,
            fruitBubbleTop,
            (fruitBubbleLeft + fruitBubbleWidth).toInt(),
            fruitBubbleTop + fruitBubbleHeight
        )
        val scoreBubbleWidth = (screenWidth / 2.7)
        val scoreBubbleHeight = (scoreBubbleWidth * bubbleBitmapRatio).toInt()
        val scoreBubbleLeft = 16
        val scoreBubbleTop = 16 + topOffset
        scoreBubbleRect = Rect(
            scoreBubbleLeft,
            scoreBubbleTop,
            scoreBubbleLeft + scoreBubbleWidth.toInt(),
            scoreBubbleTop + scoreBubbleHeight
        )
        val jarAspectRatio = jarBitmap.height.toFloat() / jarBitmap.width
        val jarWidth = screenWidth
        val jarHeight = (jarWidth * jarAspectRatio).toInt()
        val jarTop = screenHeight - jarHeight
        val jarBottom = screenHeight
        jarRect = Rect(0, jarTop, jarWidth, jarBottom)


        game = Game(context, screenWidth, screenHeight)
        setBackgroundColor(currentBackgroundColor)
        val iconSize = 96
        settingsRect = Rect(
            screenWidth - iconSize - 20,
            20 + topOffset,
            screenWidth - 20,
            20 + iconSize + topOffset
        )    }

    fun getGame(): Game  {
        return game
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val newColor = prefs.getInt("background_color", Color.parseColor("#c7a875"))
        if (newColor != currentBackgroundColor) {
            currentBackgroundColor = newColor
            setBackgroundColor(currentBackgroundColor)
        }

        canvas.drawBitmap(bubbleBitmap, null, scoreBubbleRect, null)

        val scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val currentScoreText = "Score: ${game.getScore()}"
        val bestScoreText = "Best: ${game.getBestScore()}"


        val currentScoreBounds = Rect()
        scorePaint.getTextBounds(currentScoreText, 0, currentScoreText.length, currentScoreBounds)
        val currentScoreX = scoreBubbleRect.centerX().toFloat()
        val currentScoreY = scoreBubbleRect.centerY().toFloat() - 20f
        canvas.drawText(currentScoreText, currentScoreX, currentScoreY, scorePaint)


        val bestScoreBounds = Rect()
        scorePaint.getTextBounds(bestScoreText, 0, bestScoreText.length, bestScoreBounds)
        val bestScoreX = scoreBubbleRect.centerX().toFloat()
        val bestScoreY = scoreBubbleRect.centerY().toFloat() + 30f
        canvas.drawText(bestScoreText, bestScoreX, bestScoreY, scorePaint)


        canvas.drawBitmap(jarBitmap, null, jarRect, paint)
        canvas.drawBitmap(bubbleBitmap, null, fruitBubbleRect, paint)

        val nextFruit = game.getNextFruitType()
        val nextBitmap = game.getBitmap(nextFruit)
        nextBitmap?.let {
            val centerX = fruitBubbleRect.centerX()
            val centerY = fruitBubbleRect.centerY()
            val radius = (fruitBubbleRect.width() * 0.3f).toInt()
            val fruitRect = Rect(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )
            canvas.drawBitmap(it, null, fruitRect, null)
        }

        for (fruit in game.getFruits()) {
            val bmp = game.getBitmap(fruit.type)
            bmp?.let {
                canvas.drawBitmap(it, null, fruit.getRect(), paint)
            }
        }

        canvas.drawBitmap(settingsBitmap, null, settingsRect, null)
    }


    fun clickedSettingsIcon(x: Int, y: Int): Boolean {
        return ::settingsRect.isInitialized && settingsRect.contains(x, y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt()
            val y = event.y.toInt()

            if (clickedSettingsIcon(x, y)) {
                Log.d("GameView", "Settings icon touched")
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
                return true
            }

            game.setDropX(x)
            game.spawnFruit()
            invalidate()
        }

        return true
    }


}