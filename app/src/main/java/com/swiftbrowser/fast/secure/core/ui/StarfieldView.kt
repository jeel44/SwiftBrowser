package com.swiftbrowser.fast.secure.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class StarfieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val handler = Handler(Looper.getMainLooper())
    private var tick = 0f
    private var initialized = false

    data class Star(
        val x: Float,
        val y: Float,
        val radius: Float,
        val twinkleOffset: Float,
        val speed: Float,
        val type: Int // 0=white, 1=light purple, 2=purple
    )

    data class Glow(
        val x: Float,
        val y: Float,
        val radius: Float,
        val twinkleOffset: Float,
        val speed: Float,
        val gradient: RadialGradient
    )

    private val stars = mutableListOf<Star>()
    private val glows = mutableListOf<Glow>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val runnable = object : Runnable {
        override fun run() {
            tick += 0.032f
            invalidate()
            handler.postDelayed(this, 32)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!initialized && w > 0 && h > 0) {
            initialized = true
            generateStars(w, h)
        }
    }

    private fun generateStars(w: Int, h: Int) {
        stars.clear()
        glows.clear()

        repeat(60) {
            stars.add(
                Star(
                    x = Random.nextFloat() * w,
                    y = Random.nextFloat() * h,
                    radius = Random.nextFloat() * 1.4f + 0.2f,
                    twinkleOffset = Random.nextFloat() * (2f * Math.PI.toFloat()),
                    speed = Random.nextFloat() * 0.008f + 0.003f,
                    type = when {
                        Random.nextFloat() > 0.85f -> 2
                        Random.nextFloat() > 0.6f -> 1
                        else -> 0
                    }
                )
            )
        }

        repeat(4) {
            val x = Random.nextFloat() * w
            val y = Random.nextFloat() * h * 0.75f
            val radius = Random.nextFloat() * 2f + 1.5f
            val glowRadius = radius * 3f
            glows.add(
                Glow(
                    x = x,
                    y = y,
                    radius = radius,
                    twinkleOffset = Random.nextFloat() * (2f * Math.PI.toFloat()),
                    speed = Random.nextFloat() * 0.005f + 0.002f,
                    gradient = RadialGradient(
                        x, y, glowRadius,
                        android.graphics.Color.argb(255, 167, 139, 250),
                        android.graphics.Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        stars.forEach { star ->
            val pulse = sin((tick * star.speed * 60f + star.twinkleOffset).toDouble()).toFloat()
            val alpha = (0.3f + (pulse + 1f) / 2f * 0.65f).coerceIn(0f, 1f)
            val alphaInt = (alpha * 255).toInt()

            paint.style = Paint.Style.FILL
            paint.shader = null
            paint.color = when (star.type) {
                2 -> android.graphics.Color.argb(alphaInt, 167, 139, 250)
                1 -> android.graphics.Color.argb(alphaInt, 196, 181, 253)
                else -> android.graphics.Color.argb(alphaInt, 255, 255, 255)
            }
            canvas.drawCircle(star.x, star.y, star.radius, paint)
        }

        glows.forEach { glow ->
            val pulse = sin((tick * glow.speed * 60f + glow.twinkleOffset).toDouble()).toFloat()
            val alpha = (0.4f + (pulse + 1f) / 2f * 0.5f).coerceIn(0f, 1f)
            val alphaInt = (alpha * 255).toInt()

            paint.shader = glow.gradient
            paint.alpha = alphaInt
            paint.style = Paint.Style.FILL
            canvas.drawCircle(glow.x, glow.y, glow.radius * 3f, paint)

            paint.shader = null
            paint.color = android.graphics.Color.argb(alphaInt, 220, 210, 255)
            canvas.drawCircle(glow.x, glow.y, glow.radius, paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(runnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(runnable)
    }
}
