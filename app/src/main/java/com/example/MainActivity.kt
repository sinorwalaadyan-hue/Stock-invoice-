package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.StockVisionApp
import com.example.ui.StockVisionViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StockVisionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockVisionApp(viewModel = viewModel)
        }
    }
}
