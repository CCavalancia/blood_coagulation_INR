package com.inr.measurement.processing

import com.inr.measurement.data.models.MotionCurve
import com.inr.measurement.data.models.VideoFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Generates motion curves from video frames
 * Android equivalent of start_time.m and stop_time.m
 */
class MotionCurveGenerator {

    /**
     * Generate pipette motion curve (for start time detection)
     * Based on start_time.m
     */
    suspend fun generatePipetteMotionCurve(
        frames: List<VideoFrame>,
        roiRect: Rect? = null
    ): MotionCurve = withContext(Dispatchers.Default) {
        val timePoints = mutableListOf<Double>()
        val motionValues = mutableListOf<Double>()

        var previousFrame: Mat? = null

        frames.forEach { frame ->
            val currentFrame = frame.image.clone()

            // Apply ROI if specified
            val roi = roiRect?.let { Mat(currentFrame, it) } ?: currentFrame

            // Convert to grayscale
            val grayFrame = Mat()
            Imgproc.cvtColor(roi, grayFrame, Imgproc.COLOR_BGR2GRAY)

            // Calculate motion from previous frame
            previousFrame?.let { prev ->
                val diff = Mat()
                Core.absdiff(prev, grayFrame, diff)

                // Calculate mean difference as motion value
                val mean = Core.mean(diff)
                motionValues.add(mean.`val`[0])
                timePoints.add(frame.getTimestampSeconds())
            }

            previousFrame?.release()
            previousFrame = grayFrame.clone()

            grayFrame.release()
            roi.release()
            currentFrame.release()
        }

        previousFrame?.release()

        // Detect knee point
        val kneePoint = detectKneePoint(timePoints, motionValues)

        MotionCurve(
            type = MotionCurve.MotionCurveType.PIPETTE,
            timePoints = timePoints,
            motionValues = motionValues,
            kneePoint = kneePoint
        )
    }

    /**
     * Generate particle motion curve (for stop time detection)
     * Based on stop_time.m
     */
    suspend fun generateParticleMotionCurve(
        frames: List<VideoFrame>,
        centerPoint: Point,
        radius: Double = 50.0
    ): MotionCurve = withContext(Dispatchers.Default) {
        val timePoints = mutableListOf<Double>()
        val motionValues = mutableListOf<Double>()

        var previousCircularRegion: Mat? = null

        frames.forEach { frame ->
            // Extract circular region around particle
            val circularRegion = extractCircularRegion(frame.image, centerPoint, radius)

            // Convert to grayscale
            val grayRegion = Mat()
            Imgproc.cvtColor(circularRegion, grayRegion, Imgproc.COLOR_BGR2GRAY)

            // Calculate motion
            previousCircularRegion?.let { prev ->
                val diff = Mat()
                Core.absdiff(prev, grayRegion, diff)

                val mean = Core.mean(diff)
                motionValues.add(mean.`val`[0])
                timePoints.add(frame.getTimestampSeconds())

                diff.release()
            }

            previousCircularRegion?.release()
            previousCircularRegion = grayRegion.clone()

            grayRegion.release()
            circularRegion.release()
        }

        previousCircularRegion?.release()

        // Detect knee point
        val kneePoint = detectKneePoint(timePoints, motionValues)

        MotionCurve(
            type = MotionCurve.MotionCurveType.PARTICLE,
            timePoints = timePoints,
            motionValues = motionValues,
            kneePoint = kneePoint
        )
    }

    /**
     * Extract circular region from frame
     * Based on circlecrop.m and circlecropbw.m
     */
    private fun extractCircularRegion(
        frame: Mat,
        center: Point,
        radius: Double
    ): Mat {
        // Create a mask for circular region
        val mask = Mat.zeros(frame.size(), CvType.CV_8UC1)
        Imgproc.circle(mask, center, radius.toInt(), Scalar(255.0), -1)

        // Apply mask to frame
        val result = Mat()
        frame.copyTo(result, mask)

        mask.release()
        return result
    }

    /**
     * Detect knee point in motion curve
     * Based on knee_pt.m
     * Uses the "knee" detection algorithm to find inflection point
     */
    private fun detectKneePoint(
        timePoints: List<Double>,
        values: List<Double>
    ): Double? {
        if (values.size < 3) return null

        // Normalize values
        val minVal = values.minOrNull() ?: return null
        val maxVal = values.maxOrNull() ?: return null
        val range = maxVal - minVal

        if (range == 0.0) return null

        val normalized = values.map { (it - minVal) / range }

        // Find knee using perpendicular distance method
        var maxDistance = 0.0
        var kneeIndex = 0

        // Line from first to last point
        val x1 = 0.0
        val y1 = normalized.first()
        val x2 = (normalized.size - 1).toDouble()
        val y2 = normalized.last()

        for (i in 1 until normalized.size - 1) {
            val x0 = i.toDouble()
            val y0 = normalized[i]

            // Calculate perpendicular distance from point to line
            val numerator = kotlin.math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1)
            val denominator = kotlin.math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1))

            val distance = numerator / denominator

            if (distance > maxDistance) {
                maxDistance = distance
                kneeIndex = i
            }
        }

        return timePoints[kneeIndex]
    }
}
