package mx.edu.utez.integrtadoranotes.utils


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LightSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _lightValue = MutableStateFlow(0f)
    val lightValue: StateFlow<Float> = _lightValue

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun start() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                _lightValue.value = event.values[0]

                // Transformar valor de luz (0-1000 lux) a factor de oscuridad (0-1)
                val darknessFactor = calculateDarknessFactor(event.values[0])
                _isDarkMode.value = darknessFactor > 0.5
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se necesita implementar
    }

    private fun calculateDarknessFactor(lightValue: Float): Float {
        // Normalizar el valor de luz entre 0 y 1
        // 0 lux = completamente oscuro (1)
        // 1000 lux = completamente claro (0)
        val normalized = (1000 - lightValue.coerceIn(0f, 1000f)) / 1000
        return normalized
    }
}