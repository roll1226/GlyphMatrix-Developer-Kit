package com.example.myapplication

import android.util.Log
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphMatrixManager

class RealGlyphMatrixDisplay(private val manager: GlyphMatrixManager) : GlyphMatrixDisplay {

    companion object {
        private const val TAG = "RealGlyphMatrixDisplay"
    }

    override fun setFrame(frame: IntArray) {
        try {
            manager.setMatrixFrame(frame)
        } catch (e: GlyphException) {
            Log.e(TAG, e.message ?: "setMatrixFrame failed")
        }
    }

    override fun turnOff() {
        manager.turnOff()
    }
}
