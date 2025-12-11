package com.example.notasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.example.notasapp.ui.theme.NotasAppTheme
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.utez.integrtadoranotes.ui.nav.AppNavigation
import mx.edu.utez.integrtadoranotes.utils.LightSensorManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var lightSensorManager: LightSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lightSensorManager = LightSensorManager(this)

        setContent {
            val isDarkMode by lightSensorManager.isDarkMode.collectAsState()

            NotasAppTheme(isDarkMode = isDarkMode) {
                Surface {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        lightSensorManager.stop()
    }
}