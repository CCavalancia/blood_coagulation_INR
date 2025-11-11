package com.inr.measurement.processing

import android.media.MediaMetadataRetriever
import android.net.Uri
import com.inr.measurement.data.models.VideoFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import android.graphics.Bitmap

/**
 * Extracts frames from video files for INR analysis
 * Android equivalent of extract.py
 */
class VideoFrameExtractor {

    /**
     * Extract all frames from a video file
     * @param videoUri URI of the video file
     * @param callback Progress callback (current frame, total frames)
     * @return List of VideoFrame objects
     */
    suspend fun extractFrames(
        videoUri: Uri,
        callback: ((Int, Int) -> Unit)? = null
    ): List<VideoFrame> = withContext(Dispatchers.IO) {
        val frames = mutableListOf<VideoFrame>()
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(videoUri.path)

            // Get video metadata
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toDoubleOrNull() ?: 30.0

            // Calculate total frames
            val totalFrames = ((duration / 1000.0) * frameRate).toInt()

            // Extract frames at regular intervals
            var frameNumber = 0
            var timestampUs = 0L
            val frameIntervalUs = (1_000_000.0 / frameRate).toLong()

            while (timestampUs < duration * 1000) {
                val bitmap = retriever.getFrameAtTime(timestampUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

                bitmap?.let {
                    // Convert bitmap to OpenCV Mat
                    val mat = Mat()
                    Utils.bitmapToMat(it, mat)

                    val frame = VideoFrame(
                        frameNumber = frameNumber,
                        timestampMs = timestampUs / 1000,
                        image = mat,
                        width = it.width,
                        height = it.height
                    )

                    frames.add(frame)
                    callback?.invoke(frameNumber, totalFrames)
                }

                frameNumber++
                timestampUs += frameIntervalUs
            }

        } finally {
            retriever.release()
        }

        frames
    }

    /**
     * Extract frames at specific time points
     */
    suspend fun extractFramesAtTimes(
        videoUri: Uri,
        timePointsMs: List<Long>
    ): List<VideoFrame> = withContext(Dispatchers.IO) {
        val frames = mutableListOf<VideoFrame>()
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(videoUri.path)

            timePointsMs.forEachIndexed { index, timestampMs ->
                val bitmap = retriever.getFrameAtTime(
                    timestampMs * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST
                )

                bitmap?.let {
                    val mat = Mat()
                    Utils.bitmapToMat(it, mat)

                    val frame = VideoFrame(
                        frameNumber = index,
                        timestampMs = timestampMs,
                        image = mat,
                        width = it.width,
                        height = it.height
                    )

                    frames.add(frame)
                }
            }

        } finally {
            retriever.release()
        }

        frames
    }
}
