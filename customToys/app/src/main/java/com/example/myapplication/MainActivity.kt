package com.example.myapplication

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ToyScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ToyScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "My Custom Toy",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Glyph Matrixでカスタム表示を行うトイです。",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Glyph Toys マネージャーへ誘導するボタン
        Button(onClick = {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.nothing.thirdparty",
                    "com.nothing.thirdparty.matrix.toys.manager.ToysManagerActivity"
                )
            }
            context.startActivity(intent)
        }) {
            Text(text = "トイをアクティブ化する")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            context.startActivity(Intent(context, SimulatorActivity::class.java))
        }) {
            Text(text = "シミュレーターを開く")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ToyScreenPreview() {
    MyApplicationTheme {
        ToyScreen()
    }
}
