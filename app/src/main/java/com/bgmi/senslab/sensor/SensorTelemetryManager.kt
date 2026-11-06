package com.bgmi.senslab.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI

data class GyroData(
    val pitchDegSec: Float = 0f,
    val rollDegSec: Float = 0f,
    val isAvailable: Boolean = false
)

class SensorTelemetryManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _telemetry = MutableStateFlow(GyroData(isAvailable = gyroSensor != null))
    val telemetry: StateFlow<GyroData> = _telemetry

    fun startListening() {
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            val radToDeg = (180.0 / PI).toFloat()
            val pitchDeg = event.values[0] * radToDeg
            val rollDeg = event.values[1] * radToDeg

            _telemetry.value = GyroData(
                pitchDegSec = pitchDeg,
                rollDegSec = rollDeg,
                isAvailable = true
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
