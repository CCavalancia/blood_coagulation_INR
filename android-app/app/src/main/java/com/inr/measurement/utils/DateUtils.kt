package com.inr.measurement.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date/time formatting
 */
object DateUtils {

    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US)
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    /**
     * Format date and time
     */
    fun formatDateTime(date: Date): String {
        return dateTimeFormat.format(date)
    }

    /**
     * Format date only
     */
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    /**
     * Format time only
     */
    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }

    /**
     * Format duration in seconds to mm:ss
     */
    fun formatDuration(seconds: Double): String {
        val totalSeconds = seconds.toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
    }
}
