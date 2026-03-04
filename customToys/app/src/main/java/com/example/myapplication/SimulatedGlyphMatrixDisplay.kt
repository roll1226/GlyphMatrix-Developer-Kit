package com.example.myapplication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SimulatedGlyphMatrixDisplay : GlyphMatrixDisplay {
    private val _frameState = MutableStateFlow<IntArray?>(null)
    val frameState: StateFlow<IntArray?> = _frameState.asStateFlow()

    private val _isOff = MutableStateFlow(false)
    val isOff: StateFlow<Boolean> = _isOff.asStateFlow()

    override fun setFrame(frame: IntArray) {
        _isOff.value = false
        _frameState.value = frame
    }

    override fun turnOff() {
        _isOff.value = true
        _frameState.value = IntArray(625) { 0 }
    }
}
