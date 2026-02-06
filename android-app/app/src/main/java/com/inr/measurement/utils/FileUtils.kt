package com.inr.measurement.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for file management
 */
object FileUtils {

    /**
     * Get directory for storing videos
     */
    fun getVideosDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "Videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Get directory for storing results
     */
    fun getResultsDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "Results")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Generate unique filename for video
     */
    fun generateVideoFilename(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timestamp = dateFormat.format(Date())
        return "INR_$timestamp.mp4"
    }

    /**
     * Generate unique filename for results JSON
     */
    fun generateResultsFilename(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timestamp = dateFormat.format(Date())
        return "INR_Results_$timestamp.json"
    }

    /**
     * Delete file if it exists
     */
    fun deleteFile(file: File): Boolean {
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * Get file size in MB
     */
    fun getFileSizeMB(file: File): Double {
        return file.length() / (1024.0 * 1024.0)
    }
}
