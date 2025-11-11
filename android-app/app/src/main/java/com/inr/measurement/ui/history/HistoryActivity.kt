package com.inr.measurement.ui.history

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inr.measurement.R

/**
 * Activity for displaying measurement history
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setupRecyclerView()
        loadHistory()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // TODO: Setup adapter
    }

    private fun loadHistory() {
        // TODO: Load measurements from database
        Toast.makeText(this, "Loading history...", Toast.LENGTH_SHORT).show()
    }
}
