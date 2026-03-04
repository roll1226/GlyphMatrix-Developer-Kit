package com.example.myapplication

interface GlyphMatrixDisplay {
    /** IntArray(625)、各値 0–2047 */
    fun setFrame(frame: IntArray)
    fun turnOff()
}
