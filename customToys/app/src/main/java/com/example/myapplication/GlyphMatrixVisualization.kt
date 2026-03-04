package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlyphMatrixVisualization(
    frame: IntArray?,
    isOff: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(Color(0xFF0A0A0A))
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(12.dp)
        ) {
            val cellSize = size.width / 25
            val ledSize = cellSize * 0.75f
            val offset = (cellSize - ledSize) / 2f

            for (row in 0..24) {
                for (col in 0..24) {
                    val raw = frame?.getOrElse(row * 25 + col) { 0 } ?: 0
                    val alpha = when {
                        isOff || frame == null || raw <= 0 -> 0.04f
                        else -> 0.04f + 0.96f * (raw / 2047f)
                    }
                    drawRoundRect(
                        color = Color.White,
                        alpha = alpha,
                        topLeft = Offset(col * cellSize + offset, row * cellSize + offset),
                        size = Size(ledSize, ledSize),
                        cornerRadius = CornerRadius(ledSize * 0.25f)
                    )
                }
            }
        }
    }
}
