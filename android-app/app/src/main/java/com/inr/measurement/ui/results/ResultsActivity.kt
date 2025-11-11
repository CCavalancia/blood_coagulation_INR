package com.inr.measurement.ui.results

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inr.measurement.R
import com.inr.measurement.data.models.INRMeasurement

/**
 * Activity for displaying PT and INR measurement results
 */
class ResultsActivity : AppCompatActivity() {

    private var measurement: INRMeasurement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        loadMeasurementData()
        setupUI()
    }

    private fun loadMeasurementData() {
        // TODO: Load measurement from intent or database
        val videoPath = intent.getStringExtra("video_path")

        // For now, show placeholder
        // In full implementation, this would load the actual measurement
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btn_save_results)?.setOnClickListener {
            saveMeasurement()
        }

        findViewById<Button>(R.id.btn_share_results)?.setOnClickListener {
            shareResults()
        }

        findViewById<Button>(R.id.btn_new_measurement)?.setOnClickListener {
            finish()
        }

        displayResults()
    }

    private fun displayResults() {
        measurement?.let { m ->
            findViewById<TextView>(R.id.tv_pt_value)?.text = m.getFormattedPT()
            findViewById<TextView>(R.id.tv_inr_value)?.text = m.getFormattedINR()
            findViewById<TextView>(R.id.tv_classification)?.text = m.getINRClassification()
        }
    }

    private fun saveMeasurement() {
        // TODO: Save to database
        Toast.makeText(this, "Results saved", Toast.LENGTH_SHORT).show()
    }

    private fun shareResults() {
        // TODO: Implement sharing
        Toast.makeText(this, "Share functionality coming soon", Toast.LENGTH_SHORT).show()
    }
}
