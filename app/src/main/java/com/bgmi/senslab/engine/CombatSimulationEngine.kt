package com.bgmi.senslab.engine

import kotlin.math.roundToInt

data class SensitivityProfile(
    val tppNoScopeCam: Int,
    val redDotCam: Int,
    val scope3xCam: Int,
    val scope4xCam: Int,
    val scope6xCam: Int,

    val tppNoScopeAds: Int,
    val redDotAds: Int,
    val scope3xAds: Int,
    val scope4xAds: Int,
    val scope6xAds: Int,

    val tppGyro: Int,
    val redDotGyro: Int,
    val scope3xGyro: Int,
    val scope4xGyro: Int,
    val scope6xGyro: Int,

    val tppAdsGyro: Int,
    val redDotAdsGyro: Int,
    val scope3xAdsGyro: Int,
    val scope4xAdsGyro: Int,
    val scope6xAdsGyro: Int
)

class CombatSimulationEngine {
    var totalSwipePixels: Float = 0f
    var swipeOvershootRatio: Float = 1.0f
    var maxWristTiltDeg: Float = 30.0f
    var verticalRecoilDeficit: Float = 0f

    fun calculateBGMISensitivities(): SensitivityProfile {
        val baseTppCam = ((120.0f / swipeOvershootRatio.coerceIn(0.7f, 1.5f))).roundToInt().coerceIn(80, 160)

        val redDotCam = (baseTppCam * 0.48f).roundToInt()
        val scope3xCam = (baseTppCam * 0.24f).roundToInt()
        val scope4xCam = (baseTppCam * 0.16f).roundToInt()
        val scope6xCam = (baseTppCam * 0.11f).roundToInt()

        val adsMultiplier = if (verticalRecoilDeficit > 15f) 1.15f else 1.0f
        val tppAds = (baseTppCam * adsMultiplier).roundToInt().coerceIn(80, 170)
        val redDotAds = (redDotCam * adsMultiplier).roundToInt()
        val scope3xAds = (scope3xCam * adsMultiplier).roundToInt()
        val scope4xAds = (scope4xCam * adsMultiplier).roundToInt()
        val scope6xAds = (scope6xCam * adsMultiplier).roundToInt()

        val baseGyro = ((180.0f / maxWristTiltDeg.coerceIn(15.0f, 55.0f)) * 55.0f).roundToInt().coerceIn(240, 400)
        val redDotGyro = (baseGyro * 0.92f).roundToInt().coerceIn(200, 400)
        val scope3xGyro = (baseGyro * 0.68f).roundToInt().coerceIn(160, 320)
        val scope4xGyro = (baseGyro * 0.52f).roundToInt().coerceIn(120, 260)
        val scope6xGyro = (baseGyro * 0.32f).roundToInt().coerceIn(70, 160)

        val baseAdsGyro = (baseGyro * 0.95f).roundToInt().coerceIn(220, 400)
        val redDotAdsGyro = (redDotGyro * 0.95f).roundToInt()
        val scope3xAdsGyro = (scope3xGyro * 0.96f).roundToInt()
        val scope4xAdsGyro = (scope4xGyro * 0.96f).roundToInt()
        val scope6xAdsGyro = (scope6xGyro * 0.96f).roundToInt()

        return SensitivityProfile(
            tppNoScopeCam = baseTppCam, redDotCam = redDotCam, scope3xCam = scope3xCam, scope4xCam = scope4xCam, scope6xCam = scope6xCam,
            tppNoScopeAds = tppAds, redDotAds = redDotAds, scope3xAds = scope3xAds, scope4xAds = scope4xAds, scope6xAds = scope6xAds,
            tppGyro = baseGyro, redDotGyro = redDotGyro, scope3xGyro = scope3xGyro, scope4xGyro = scope4xGyro, scope6xGyro = scope6xGyro,
            tppAdsGyro = baseAdsGyro, redDotAdsGyro = redDotAdsGyro, scope3xAdsGyro = scope3xAdsGyro, scope4xAdsGyro = scope4xAdsGyro, scope6xAdsGyro = scope6xAdsGyro
        )
    }
}
