package com.inr.measurement.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data model for INR measurement results
 * Represents a single PT/INR test result
 */
@Entity(tableName = "measurements")
data class INRMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Prothrombin Time in seconds
     */
    val ptValue: Double,

    /**
     * International Normalized Ratio
     */
    val inrValue: Double,

    /**
     * Timestamp of measurement
     */
    val timestamp: Date,

    /**
     * Path to video file
     */
    val videoPath: String,

    /**
     * Start time of coagulation (in seconds from video start)
     */
    val startTime: Double,

    /**
     * Stop time of coagulation (in seconds from video start)
     */
    val stopTime: Double,

    /**
     * Optional notes
     */
    val notes: String? = null,

    /**
     * Quality metrics
     */
    val qualityScore: Double? = null
) {
    /**
     * Get formatted PT value
     */
    fun getFormattedPT(): String = String.format("%.1f", ptValue)

    /**
     * Get formatted INR value
     */
    fun getFormattedINR(): String = String.format("%.1f", inrValue)

    /**
     * Get INR classification
     */
    fun getINRClassification(): String {
        return when {
            inrValue < 0.8 -> "Below Normal"
            inrValue in 0.8..1.2 -> "Normal"
            inrValue in 1.2..2.0 -> "Slightly Elevated"
            inrValue in 2.0..3.0 -> "Therapeutic (Moderate)"
            inrValue in 3.0..4.0 -> "Therapeutic (High)"
            else -> "Critical - Above Therapeutic Range"
        }
    }
}
