package com.inr.measurement.data.models

/**
 * Represents a motion curve extracted from video analysis
 * Used for both pipette motion and particle motion
 */
data class MotionCurve(
    /**
     * Type of motion curve (PIPETTE or PARTICLE)
     */
    val type: MotionCurveType,

    /**
     * Time points (frame numbers or timestamps)
     */
    val timePoints: List<Double>,

    /**
     * Motion values at each time point
     */
    val motionValues: List<Double>,

    /**
     * Detected knee point (inflection point indicating coagulation start/stop)
     */
    val kneePoint: Double? = null
) {
    enum class MotionCurveType {
        PIPETTE,    // For start time detection
        PARTICLE    // For stop time detection
    }

    /**
     * Get the knee point index in the data arrays
     */
    fun getKneePointIndex(): Int? {
        return kneePoint?.let { knee ->
            timePoints.indexOfFirst { it >= knee }
        }
    }

    /**
     * Get smoothed motion curve using moving average
     */
    fun getSmoothedCurve(windowSize: Int = 5): MotionCurve {
        if (motionValues.size < windowSize) return this

        val smoothed = mutableListOf<Double>()
        for (i in motionValues.indices) {
            val start = maxOf(0, i - windowSize / 2)
            val end = minOf(motionValues.size, i + windowSize / 2 + 1)
            val average = motionValues.subList(start, end).average()
            smoothed.add(average)
        }

        return copy(motionValues = smoothed)
    }
}
