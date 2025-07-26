package com.example.suika

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.Log
import com.example.suikagame.R
import kotlin.math.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class Game(private val context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    data class Fruit(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var type: Int,
        var dropped: Boolean = false
    ) {
        fun getRect(): RectF {
            return RectF(x - radius, y - radius, x + radius, y + radius)
        }
    }

    private val gravity = 0.8f
    private val fruits = CopyOnWriteArrayList<Fruit>()
    private val bitmaps = mutableMapOf<Int, Bitmap>()
    private var dropX = screenWidth / 2f
    private val baseSize = screenWidth / 4f
    private val random = Random()
    private var score = 0
    private val prefs = context.getSharedPreferences("suika_prefs", Context.MODE_PRIVATE)

    // Drop cooldown
    private var lastDropTime = 0L
    private val dropCooldown: Long
        get() = prefs.getInt("drop_cooldown", 800).toLong()

    private val fruitScaleFactor: Float
        get() = prefs.getInt("fruit_scale", 100) / 100f

    private var nextFruitType = 1
    fun getNextFruitType(): Int = nextFruitType

    private val jarInset = screenWidth * 0.02f
    private val jarLeft = jarInset
    private val jarRight = screenWidth - jarInset
    private val jarBottom = screenHeight.toFloat() * 0.98f
    private val jarTopThreshold = screenHeight * 0.5f

    private var gameOver = false

    init {
        bitmaps[1] = BitmapFactory.decodeResource(context.resources, R.drawable.cherry_1)
        bitmaps[2] = BitmapFactory.decodeResource(context.resources, R.drawable.strawberry_2)
        bitmaps[3] = BitmapFactory.decodeResource(context.resources, R.drawable.grape_3)
        bitmaps[4] = BitmapFactory.decodeResource(context.resources, R.drawable.dekopon_4)
        bitmaps[5] = BitmapFactory.decodeResource(context.resources, R.drawable.orange_5)
        bitmaps[6] = BitmapFactory.decodeResource(context.resources, R.drawable.apple_6)
        bitmaps[7] = BitmapFactory.decodeResource(context.resources, R.drawable.pear_7)
        bitmaps[8] = BitmapFactory.decodeResource(context.resources, R.drawable.peach_8)
        bitmaps[9] = BitmapFactory.decodeResource(context.resources, R.drawable.pineapple_9)
        bitmaps[10] = BitmapFactory.decodeResource(context.resources, R.drawable.melon_10)
        bitmaps[11] = BitmapFactory.decodeResource(context.resources, R.drawable.watermelon_11)
        nextFruitType = random.nextInt(3) + 1
    }

    fun isGameOver(): Boolean = gameOver
    fun getScore(): Int = score
    fun getBestScore(): Int = prefs.getInt("best_score", 0)

    fun setDropX(x: Int) {
        dropX = x.toFloat().coerceIn(jarLeft + baseSize, jarRight - baseSize)
    }

    fun spawnFruit() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDropTime < dropCooldown) {
            Log.d("yogurt", "Drop blocked: cooldown in effect")
            return
        }

        lastDropTime = currentTime

        val type = nextFruitType
        val radius = baseSize * fruitScaleFactor * 0.4f
        val yStart = (screenHeight * 0.5f) - radius * 2
        fruits.add(Fruit(dropX, yStart, 0f, 0f, radius, type, dropped = false))
        nextFruitType = random.nextInt(3) + 1
    }

    fun getFruits(): List<Fruit> = fruits
    fun getBitmap(type: Int): Bitmap? = bitmaps[type]

    private fun isInJar(fruit: Fruit): Boolean {
        return fruit.y - fruit.radius >= jarTopThreshold
    }

    fun update() {
        if (gameOver) return

        for (fruit in fruits) {
            fruit.vy += gravity
            fruit.x += fruit.vx
            fruit.y += fruit.vy

            if (fruit.x - fruit.radius < jarLeft) {
                fruit.x = jarLeft + fruit.radius
                fruit.vx = 0f
            } else if (fruit.x + fruit.radius > jarRight) {
                fruit.x = jarRight - fruit.radius
                fruit.vx = 0f
            }

            if (fruit.y + fruit.radius >= jarBottom) {
                fruit.y = jarBottom - fruit.radius
                fruit.vy = 0f
                fruit.vx = 0f
                if (!fruit.dropped) fruit.dropped = true
            }

            if (!fruit.dropped && isInJar(fruit)) {
                for (other in fruits) {
                    if (fruit === other || !isInJar(other)) continue
                    if (areColliding(fruit, other)) {
                        fruit.dropped = true
                        break
                    }
                }
            }

            if (fruit.dropped && fruit.y - fruit.radius < jarTopThreshold) {
                gameOver = true
                Log.d("yogurt", "Game Over Triggered - Dropped fruit above threshold")
                return
            }
        }

        handleMerges()
        resolveCollisions()
    }

    private fun handleMerges() {
        val toMerge = mutableListOf<Triple<Fruit, Fruit, Int>>()
        val merged = mutableSetOf<Fruit>()

        for (i in fruits.indices) {
            for (j in i + 1 until fruits.size) {
                val f1 = fruits[i]
                val f2 = fruits[j]
                if (!isInJar(f1) || !isInJar(f2)) continue
                if (f1.type == f2.type && f1.type < 11 && areColliding(f1, f2)) {
                    if (f1 !in merged && f2 !in merged) {
                        toMerge.add(Triple(f1, f2, f1.type + 1))
                        merged.add(f1)
                        merged.add(f2)
                    }
                }
            }
        }

        for ((f1, f2, newType) in toMerge) {
            fruits.remove(f1)
            fruits.remove(f2)

            if (newType <= 11 && bitmaps.containsKey(newType)) {
                val scale = (1.23f).pow((newType - 1).toFloat()) * fruitScaleFactor
                val newRadius = baseSize * 0.4f * scale
                val cx = (f1.x + f2.x) / 2
                val cy = (f1.y + f2.y) / 2 - newRadius
                fruits.add(Fruit(cx, cy, 0f, -3f, newRadius, newType))

                score += 2.0.pow(f1.type.toDouble()).toInt()
                if (score > getBestScore()) {
                    prefs.edit().putInt("best_score", score).apply()
                }
            }
        }
    }

    private fun resolveCollisions() {
        for (i in fruits.indices) {
            for (j in i + 1 until fruits.size) {
                val f1 = fruits[i]
                val f2 = fruits[j]
                if (!isInJar(f1) || !isInJar(f2)) continue

                val dx = f2.x - f1.x
                val dy = f2.y - f1.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDist = f1.radius + f2.radius
                if (dist < minDist && dist != 0f) {
                    val overlap = 0.5f * (minDist - dist)
                    val ox = overlap * (dx / dist)
                    val oy = overlap * (dy / dist)
                    f1.x -= ox
                    f1.y -= oy
                    f2.x += ox
                    f2.y += oy
                }
            }
        }
    }

    private fun areColliding(f1: Fruit, f2: Fruit): Boolean {
        val dx = f1.x - f2.x
        val dy = f1.y - f2.y
        val distanceSq = dx * dx + dy * dy
        val radiusSum = f1.radius + f2.radius
        return distanceSq < radiusSum * radiusSum
    }
}
