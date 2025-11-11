package com.inr.measurement.data.models

import org.opencv.core.Mat

/**
 * Represents a single video frame for processing
 */
data class VideoFrame(
    /**
     * Frame number in the video sequence
     */
    val frameNumber: Int,

    /**
     * Timestamp in milliseconds from video start
     */
    val timestampMs: Long,

    /**
     * OpenCV Mat representing the frame image
     */
    val image: Mat,

    /**
     * Frame width in pixels
     */
    val width: Int,

    /**
     * Frame height in pixels
     */
    val height: Int
) {
    /**
     * Get timestamp in seconds
     */
    fun getTimestampSeconds(): Double = timestampMs / 1000.0

    /**
     * Release OpenCV resources
     */
    fun release() {
        image.release()
    }
}
