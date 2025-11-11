package com.inr.measurement.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.inr.measurement.R
import com.inr.measurement.ui.history.HistoryActivity
import com.inr.measurement.ui.measurement.MeasurementActivity

/**
 * Main activity - Entry point for the INR Measurement app
 * Phase 2A - Blood Coagulation Testing
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btn_start_measurement)?.setOnClickListener {
            if (allPermissionsGranted()) {
                startMeasurement()
            } else {
                requestPermissions()
            }
        }

        findViewById<Button>(R.id.btn_view_history)?.setOnClickListener {
            viewHistory()
        }

        findViewById<Button>(R.id.btn_settings)?.setOnClickListener {
            showSettings()
        }
    }

    private fun startMeasurement() {
        val intent = Intent(this, MeasurementActivity::class.java)
        startActivity(intent)
    }

    private fun viewHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun showSettings() {
        Toast.makeText(this, "Settings - Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted. Some features may not work.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
