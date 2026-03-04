package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.nothing.ketchum.GlyphToy

class SimulatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Scaffold { innerPadding ->
                    SimulatorScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SimulatorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val simulatedDisplay = remember { SimulatedGlyphMatrixDisplay() }
    val engine = remember { ClockToyEngine(context.applicationContext, simulatedDisplay) }

    DisposableEffect(engine) {
        engine.start()
        onDispose {
            engine.stop()
            simulatedDisplay.turnOff()
        }
    }

    val frame by simulatedDisplay.frameState.collectAsState()
    val isOff by simulatedDisplay.isOff.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GlyphMatrix Simulator",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Format: ${if (engine.is24hMode()) "24h" else "12h"}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        GlyphMatrixVisualization(
            frame = frame,
            isOff = isOff,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { engine.onButtonEvent(GlyphToy.EVENT_CHANGE) }) {
                Text("Long Press\n(CHANGE)")
            }
            Button(onClick = { engine.onButtonEvent(GlyphToy.EVENT_AOD) }) {
                Text("AOD Event")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { simulatedDisplay.turnOff() }) {
            Text("Turn Off")
        }
    }
}
