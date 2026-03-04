package com.example.myapplication

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphToy

class MyCustomToyService : Service() {

    private var mGM: GlyphMatrixManager? = null
    private var mCallback: GlyphMatrixManager.Callback? = null
    private var engine: ClockToyEngine? = null

    // -------------------------------------------------------
    // ライフサイクル
    // -------------------------------------------------------

    override fun onBind(intent: Intent): IBinder {
        init()
        return serviceMessenger.binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        engine?.stop()
        engine = null
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
                val display = RealGlyphMatrixDisplay(mGM!!)
                engine = ClockToyEngine(applicationContext, display)
                engine?.start()
            }
            override fun onServiceDisconnected(name: ComponentName) {}
        }
        mGM?.init(mCallback)
    }

    // -------------------------------------------------------
    // Glyph Button イベント処理
    // -------------------------------------------------------

    private val serviceHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GlyphToy.MSG_GLYPH_TOY -> {
                    val event = msg.data?.getString(GlyphToy.MSG_GLYPH_TOY_DATA)
                    if (event != null) {
                        engine?.onButtonEvent(event)
                    } else {
                        super.handleMessage(msg)
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val serviceMessenger = Messenger(serviceHandler)
}
