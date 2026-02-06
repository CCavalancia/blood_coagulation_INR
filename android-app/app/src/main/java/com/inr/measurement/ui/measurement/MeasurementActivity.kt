package com.inr.measurement.ui.measurement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inr.measurement.R
import com.inr.measurement.ui.results.ResultsActivity

/**
 * Activity for recording and analyzing blood coagulation measurements
 */
class MeasurementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurement)

        setupCamera()
        setupRecordingControls()
    }

    private fun setupCamera() {
        // TODO: Initialize CameraX
        // This will be implemented with camera preview and video recording
    }

    private fun setupRecordingControls() {
        // TODO: Setup recording controls
        // - Record button
        // - Stop button
        // - Analyze button
    }

    private fun startRecording() {
        // TODO: Start video recording
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        // TODO: Stop video recording
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
    }

    private fun analyzeRecording(videoPath: String) {
        // TODO: Launch analysis
        // This will:
        // 1. Extract frames from video
        // 2. Generate motion curves
        // 3. Calculate PT and INR
        // 4. Navigate to results

        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("video_path", videoPath)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: Release camera resources
    }
}
