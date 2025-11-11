package com.inr.measurement.processing

import com.inr.measurement.data.models.INRMeasurement
import com.inr.measurement.data.models.MotionCurve
import java.util.Date
import kotlin.math.pow

/**
 * Calculates PT and INR values from motion curves
 * Android equivalent of compute_pt.m
 */
class INRCalculator {

    companion object {
        // ISI (International Sensitivity Index) - typical value for PT reagents
        private const val DEFAULT_ISI = 1.0

        // Mean Normal Prothrombin Time (MNPT) in seconds
        private const val DEFAULT_MNPT = 12.0
    }

    /**
     * Calculate PT and INR from pipette and particle motion curves
     * @param pipetteCurve Motion curve from pipette (start time)
     * @param particleCurve Motion curve from particle (stop time)
     * @param videoPath Path to source video
     * @param isi International Sensitivity Index of the reagent
     * @param mnpt Mean Normal Prothrombin Time
     * @return INRMeasurement object with calculated values
     */
    fun calculateINR(
        pipetteCurve: MotionCurve,
        particleCurve: MotionCurve,
        videoPath: String,
        isi: Double = DEFAULT_ISI,
        mnpt: Double = DEFAULT_MNPT
    ): INRMeasurement {
        // Get start and stop times from knee points
        val startTime = pipetteCurve.kneePoint ?: 0.0
        val stopTime = particleCurve.kneePoint ?: 0.0

        // Calculate Prothrombin Time (PT)
        // PT is the time from start (pipette motion) to stop (particle coagulation)
        val ptValue = stopTime - startTime

        // Calculate INR using the formula: INR = (PT / MNPT) ^ ISI
        val inrValue = (ptValue / mnpt).pow(isi)

        // Calculate quality score based on curve characteristics
        val qualityScore = calculateQualityScore(pipetteCurve, particleCurve)

        return INRMeasurement(
            ptValue = ptValue,
            inrValue = inrValue,
            timestamp = Date(),
            videoPath = videoPath,
            startTime = startTime,
            stopTime = stopTime,
            qualityScore = qualityScore
        )
    }

    /**
     * Calculate quality score for the measurement
     * Based on curve smoothness and knee point confidence
     */
    private fun calculateQualityScore(
        pipetteCurve: MotionCurve,
        particleCurve: MotionCurve
    ): Double {
        var score = 100.0

        // Check if knee points were detected
        if (pipetteCurve.kneePoint == null) score -= 30.0
        if (particleCurve.kneePoint == null) score -= 30.0

        // Calculate curve smoothness for pipette
        val pipetteSmoothness = calculateCurveSmoothness(pipetteCurve.motionValues)
        score -= (1.0 - pipetteSmoothness) * 20.0

        // Calculate curve smoothness for particle
        val particleSmoothness = calculateCurveSmoothness(particleCurve.motionValues)
        score -= (1.0 - particleSmoothness) * 20.0

        return score.coerceIn(0.0, 100.0)
    }

    /**
     * Calculate smoothness of a curve (0.0 to 1.0, higher is smoother)
     */
    private fun calculateCurveSmoothness(values: List<Double>): Double {
        if (values.size < 3) return 0.0

        // Calculate average rate of change
        val changes = mutableListOf<Double>()
        for (i in 1 until values.size) {
            changes.add(kotlin.math.abs(values[i] - values[i - 1]))
        }

        val avgChange = changes.average()
        val maxChange = changes.maxOrNull() ?: 1.0

        // Smoothness is inverse of variability
        return if (maxChange > 0) {
            1.0 - (avgChange / maxChange).coerceIn(0.0, 1.0)
        } else {
            1.0
        }
    }

    /**
     * Validate measurement results
     */
    fun isValidMeasurement(measurement: INRMeasurement): Boolean {
        return measurement.ptValue > 0 &&
                measurement.ptValue < 120.0 &&  // Max 2 minutes
                measurement.inrValue > 0 &&
                measurement.inrValue < 10.0 &&  // Physiologically reasonable
                (measurement.qualityScore ?: 0.0) >= 50.0  // Minimum quality threshold
    }
}
