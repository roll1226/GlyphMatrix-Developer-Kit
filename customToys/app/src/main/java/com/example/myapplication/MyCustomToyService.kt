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
    // 文字幅計算ユーティリティ
    // -------------------------------------------------------

    companion object {
        /** GlyphMatrixの列数（25x25固定） */
        private const val MATRIX_COLUMNS = 25
    }

    /**
     * SDKのレター定義に基づく1文字あたりのピクセル幅。
     * SDKは大文字を小文字に変換してリソースを参照するため、lowercaseChar()で統一する。
     * 幅が異なる文字: '1'/'i'/'t' = 3px, ':'/' ' = 1px, 'm'/'w' = 5px, その他 = 4px
     */
    private fun glyphCharWidth(ch: Char): Int = when (ch.lowercaseChar()) {
        '1', 'i', 't'  -> 3
        ':', ' ', '.'  -> 1
        'm', 'w'       -> 5
        else           -> 4
    }

    /** テキスト全体のピクセル幅（文字幅の合計 + 文字間スペース1px）*/
    private fun glyphTextWidth(text: String): Int {
        if (text.isEmpty()) return 0
        return text.sumOf { glyphCharWidth(it) } + (text.length - 1)
    }

    /** マトリクス上でテキストを中央寄せするX座標 */
    private fun centeredX(text: String): Int = maxOf(0, (MATRIX_COLUMNS - glyphTextWidth(text)) / 2)

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
            // 12時間: "hh:MM"（0埋めあり）
            val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
            String.format(Locale.US, "%02d%s%02d", hour12, colon, cal.get(Calendar.MINUTE))
        }

        // --- 日付テキスト: "MM:DD"（ゼロ埋めあり）---
        val dateText = String.format(
            Locale.US, "%02d.%02d",
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )

        // --- 曜日テキスト: 英語2文字 ---
        val dayText = arrayOf("Su","Mo","Tu","We","Th","Fr","Sa")[
            cal.get(Calendar.DAY_OF_WEEK) - 1
        ]

        // --- 各レイヤーに配置 ---
        // 25x25 マトリクス中央寄りに配置（文字幅に応じてX座標を調整）
        val timeObj = GlyphMatrixObject.Builder()
            .setText(timeText)
            .setScale(50)
            .setPosition(centeredX(timeText), 5)    // 上段: 時刻
            .setBrightness(255)
            .build()

        val dateObj = GlyphMatrixObject.Builder()
            .setText(dateText)
            .setScale(50)
            .setPosition(centeredX(dateText), 11)   // 中段: 日付
            .setBrightness(200)
            .build()

        val dayObj = GlyphMatrixObject.Builder()
            .setText(dayText)
            .setScale(50)
            .setPosition(centeredX(dayText), 17)    // 下段: 曜日
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
