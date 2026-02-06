package com.inr.measurement

import android.app.Application
import org.opencv.android.OpenCVLoader

/**
 * Main application class for INR Measurement app
 * Phase 2A - Blood Coagulation Testing
 */
class INRMeasurementApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize OpenCV
        if (!OpenCVLoader.initDebug()) {
            android.util.Log.e(TAG, "OpenCV initialization failed")
        } else {
            android.util.Log.d(TAG, "OpenCV initialized successfully")
        }

        // Initialize app instance
        instance = this
    }

    companion object {
        private const val TAG = "INRMeasurementApp"
        private lateinit var instance: INRMeasurementApplication

        fun getInstance(): INRMeasurementApplication = instance
    }
}
