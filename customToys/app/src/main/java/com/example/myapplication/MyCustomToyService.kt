package com.example.myapplication

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject
import com.nothing.ketchum.GlyphToy
import java.util.Calendar
import java.util.Locale

class MyCustomToyService : Service() {

    private var mGM: GlyphMatrixManager? = null
    private var mCallback: GlyphMatrixManager.Callback? = null

    /** true = 24時間表示 / false = AM/PM表示（デフォルト） */
    private var is24h: Boolean = false

    /** コロン点滅状態 */
    private var blinkColon: Boolean = true

    /** 毎秒更新用タイマー（コロン点滅 + 時刻更新） */
    private val tickHandler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            blinkColon = !blinkColon
            showClock()
            // 次の正秒まで待機して更新
            val now = System.currentTimeMillis()
            val delay = 1_000L - (now % 1_000L)
            tickHandler.postDelayed(this, delay)
        }
    }

    // -------------------------------------------------------
    // ライフサイクル
    // -------------------------------------------------------

    override fun onBind(intent: Intent): IBinder {
        init()
        return serviceMessenger.binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        tickHandler.removeCallbacks(tickRunnable)
        mGM?.turnOff()
        mGM?.unInit()
        mGM = null
        mCallback = null
        return false
    }

    // -------------------------------------------------------
    // 初期化
    // -------------------------------------------------------

    private fun init() {
        mGM = GlyphMatrixManager.getInstance(applicationContext)
        mCallback = object : GlyphMatrixManager.Callback {
            override fun onServiceConnected(name: ComponentName) {
                mGM?.register(Glyph.DEVICE_23112)
                // 即時表示 + 毎分更新開始
                tickRunnable.run()
            }
            override fun onServiceDisconnected(name: ComponentName) {}
        }
        mGM?.init(mCallback)
    }

    // -------------------------------------------------------
    // 時計表示
    // -------------------------------------------------------

    private fun showClock() {
        val cal = Calendar.getInstance()

        // --- 時刻テキスト（コロンは毎秒点滅）---
        val colon = if (blinkColon) ":" else " "
        val timeText = if (is24h) {
            // 24時間: "HH:MM"
            String.format(Locale.US, "%02d%s%02d", cal.get(Calendar.HOUR_OF_DAY), colon, cal.get(Calendar.MINUTE))
        } else {
            // AM/PMなし: "h:MM"
            val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
            String.format(Locale.US, "%02d%s%02d", hour12, colon, cal.get(Calendar.MINUTE))
        }

        // --- 日付テキスト: "MM:DD"（ゼロ埋めあり）---
        val dateText = String.format(
            Locale.US, "%02d:%02d",
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )

        // --- 曜日テキスト: 英語2文字 ---
        val dayText = arrayOf("Su","Mo","Tu","We","Th","Fr","Sa")[
            cal.get(Calendar.DAY_OF_WEEK) - 1
        ]

        // --- 各レイヤーに配置 ---
        // 25x25 マトリクス右下寄りに配置
        val timeObj = GlyphMatrixObject.Builder()
            .setText(timeText)
            .setScale(50)
            .setPosition(2, 5)    // 上段: 時刻
            .setBrightness(255)
            .build()

        val dateObj = GlyphMatrixObject.Builder()
            .setText(dateText)
            .setScale(50)
            .setPosition(2, 11)   // 中段: 日付
            .setBrightness(200)
            .build()

        val dayObj = GlyphMatrixObject.Builder()
            .setText(dayText)
            .setScale(50)
            .setPosition(8, 17)   // 下段: 曜日
            .setBrightness(180)
            .build()

        val frame = GlyphMatrixFrame.Builder()
            .addTop(timeObj)
            .addMid(dateObj)
            .addLow(dayObj)
            .build(this)

        mGM?.setMatrixFrame(frame.render())
    }

    // -------------------------------------------------------
    // Glyph Button イベント処理
    // -------------------------------------------------------

    private val serviceHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GlyphToy.MSG_GLYPH_TOY -> {
                    val event = msg.data?.getString(GlyphToy.MSG_GLYPH_TOY_DATA)
                    when (event) {
                        GlyphToy.EVENT_CHANGE -> onLongPress()
                        GlyphToy.EVENT_AOD    -> showClock()
                        else                  -> super.handleMessage(msg)
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val serviceMessenger = Messenger(serviceHandler)

    // -------------------------------------------------------
    // イベントハンドラ
    // -------------------------------------------------------

    /**
     * 長押しで AM/PM ⇄ 24時間表示 をトグル
     */
    private fun onLongPress() {
        is24h = !is24h
        showClock()
    }
}
