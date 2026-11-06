package com.bgmi.senslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bgmi.senslab.engine.CombatSimulationEngine
import com.bgmi.senslab.engine.SensitivityProfile
import com.bgmi.senslab.sensor.SensorTelemetryManager
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorTelemetryManager
    private val engine = CombatSimulationEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = SensorTelemetryManager(this)

        setContent {
            SensLabApp(sensorManager, engine)
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
    }
}

@Composable
fun SensLabApp(sensorManager: SensorTelemetryManager, engine: CombatSimulationEngine) {
    var currentStep by remember { mutableIntStateOf(1) }
    var isFiring by remember { mutableStateOf(false) }
    var bulletCount by remember { mutableIntStateOf(0) }
    var crosshairOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var resultProfile by remember { mutableStateOf<SensitivityProfile?>(null) }

    val gyroData by sensorManager.telemetry.collectAsState()

    LaunchedEffect(isFiring) {
        if (isFiring) {
            bulletCount = 0
            crosshairOffset = Offset(0f, 0f)
            while (bulletCount < 30) {
                delay(80)
                bulletCount++

                val climb = if (bulletCount < 8) 12f else 22f
                val bloom = (Random.nextFloat() - 0.5f) * 10f
                val gyroAssist = gyroData.pitchDegSec * 0.85f

                crosshairOffset = Offset(
                    x = crosshairOffset.x + bloom - (gyroData.rollDegSec * 0.5f),
                    y = crosshairOffset.y - climb + gyroAssist
                )

                if (abs(gyroData.pitchDegSec) > engine.maxWristTiltDeg) {
                    engine.maxWristTiltDeg = abs(gyroData.pitchDegSec)
                }
            }
            isFiring = false
            engine.verticalRecoilDeficit = -crosshairOffset.y
            resultProfile = engine.calculateBGMISensitivities()
            currentStep = 2
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        if (currentStep == 1) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            crosshairOffset = Offset(
                                x = crosshairOffset.x + dragAmount.x,
                                y = crosshairOffset.y + dragAmount.y
                            )
                            engine.totalSwipePixels += dragAmount.y
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = Color(0xFFFF4655), radius = 42f, center = center)
                val reticlePos = center + crosshairOffset
                drawLine(Color(0xFF00FFCC), Offset(reticlePos.x - 28, reticlePos.y), Offset(reticlePos.x + 28, reticlePos.y), strokeWidth = 4f)
                drawLine(Color(0xFF00FFCC), Offset(reticlePos.x, reticlePos.y - 28), Offset(reticlePos.x, reticlePos.y + 28), strokeWidth = 4f)
                drawCircle(Color(0xFF00FFCC), radius = 6f, center = reticlePos)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (gyroData.isAvailable) "HARDWARE GYRO: ACTIVE (P: ${gyroData.pitchDegSec.toInt()}°)" else "GYRO: SENSOR NOT FOUND",
                    color = if (gyroData.isAvailable) Color(0xFF00FFCC) else Color.Red,
                    fontSize = 13.sp
                )
                Text("ROUNDS: $bulletCount / 30", color = Color.White, fontSize = 13.sp)
            }

            Button(
                onClick = { isFiring = true },
                enabled = !isFiring,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .width(240.dp)
                    .height(52.dp)
            ) {
                Text(if (isFiring) "COMPENSATING RECOIL..." else "START M416 SPRAY DRILL", color = Color.White)
            }
        } else {
            resultProfile?.let { p ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("CALIBRATION SUCCESSFUL", color = Color(0xFF00FFCC), fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("CAMERA: TPP: ${p.tppNoScopeCam}% | Red Dot: ${p.redDotCam}% | 3x: ${p.scope3xCam}% | 4x: ${p.scope4xCam}%", color = Color.White)
                    Text("ADS: TPP: ${p.tppNoScopeAds}% | Red Dot: ${p.redDotAds}% | 3x: ${p.scope3xAds}% | 4x: ${p.scope4xAds}%", color = Color.White)
                    Text("GYROSCOPE: TPP: ${p.tppGyro}% | Red Dot: ${p.redDotGyro}% | 3x: ${p.scope3xGyro}% | 4x: ${p.scope4xGyro}%", color = Color(0xFF00FFCC))
                    Text("ADS GYRO: TPP: ${p.tppAdsGyro}% | Red Dot: ${p.redDotAdsGyro}% | 3x: ${p.scope3xAdsGyro}% | 4x: ${p.scope4xAdsGyro}%", color = Color(0xFF00FFCC))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { currentStep = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Text("RETRY DRILL", color = Color.White)
                    }
                }
            }
        }
    }
}
