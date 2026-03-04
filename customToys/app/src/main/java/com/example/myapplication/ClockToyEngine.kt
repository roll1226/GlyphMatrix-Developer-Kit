package com.example.myapplication

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixObject
import com.nothing.ketchum.GlyphToy
import java.util.Calendar
import java.util.Locale

class ClockToyEngine(
    private val context: Context,
    private val display: GlyphMatrixDisplay
) {
    private var is24h = false
    private var blinkColon = true
    private val tickHandler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            blinkColon = !blinkColon
            showClock()
            val now = System.currentTimeMillis()
            tickHandler.postDelayed(this, 1_000L - (now % 1_000L))
        }
    }

    fun start() {
        blinkColon = true
        tickRunnable.run()
    }

    fun stop() {
        tickHandler.removeCallbacks(tickRunnable)
    }

    fun is24hMode() = is24h

    fun onButtonEvent(event: String) {
        when (event) {
            GlyphToy.EVENT_CHANGE -> { is24h = !is24h; showClock() }
            GlyphToy.EVENT_AOD   -> showClock()
        }
    }

    private fun showClock() {
        val cal = Calendar.getInstance()

        val colon = if (blinkColon) ":" else " "
        val timeText = if (is24h) {
            String.format(Locale.US, "%02d%s%02d", cal.get(Calendar.HOUR_OF_DAY), colon, cal.get(Calendar.MINUTE))
        } else {
            val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
            String.format(Locale.US, "%02d%s%02d", hour12, colon, cal.get(Calendar.MINUTE))
        }

        val dateText = String.format(
            Locale.US, "%02d.%02d",
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )

        val dayText = arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")[
            cal.get(Calendar.DAY_OF_WEEK) - 1
        ]

        val dateObj = GlyphMatrixObject.Builder()
            .setText(dateText)
            .setTextStyle("tall")
            .setPosition(centeredX(dateText), 4)
            .build()

        val timeObj = GlyphMatrixObject.Builder()
            .setText(timeText)
            .setTextStyle("tall")
            .setPosition(centeredXTall(timeText), 12)
            .build()

        val dayObj = GlyphMatrixObject.Builder()
            .setText(dayText)
            .setTextStyle("tall")
            .setPosition(centeredX(dayText), 19)
            .build()

        val frame = GlyphMatrixFrame.Builder()
            .addTop(dateObj)
            .addMid(timeObj)
            .addLow(dayObj)
            .build(context)

        display.setFrame(frame.render())
    }

    // -------------------------------------------------------
    // 文字幅計算ユーティリティ
    // -------------------------------------------------------

    companion object {
        private const val MATRIX_COLUMNS = 25
    }

    private fun glyphCharWidth(ch: Char): Int = when (ch.lowercaseChar()) {
        '1', 'i', 't'  -> 3
        ':', ' ', '.'  -> 1
        'm', 'w'       -> 5
        else           -> 4
    }

    private fun glyphTextWidth(text: String): Int {
        if (text.isEmpty()) return 0
        return text.sumOf { glyphCharWidth(it) } + (text.length - 1)
    }

    private fun centeredX(text: String): Int = maxOf(0, (MATRIX_COLUMNS - glyphTextWidth(text)) / 2)

    private fun centeredXTall(text: String): Int {
        if (text.isEmpty()) return 0
        val width: Int = text.sumOf { ch ->
            when (ch.lowercaseChar()) {
                ':', ' ', '.' -> 1
                'm', 'w'      -> 5
                else          -> 4
            }.toInt()
        } + (text.length - 1)
        return maxOf(0, (MATRIX_COLUMNS - width) / 2)
    }
}
