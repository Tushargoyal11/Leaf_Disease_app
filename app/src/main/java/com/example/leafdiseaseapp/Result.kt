package com.example.leafdiseaseapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.leafdiseaseapp.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive data from MainActivity
        val diseaseName = intent.getStringExtra("disease_name") ?: "Unknown Disease"
        val confidence = intent.getFloatExtra("confidence", 0f)
        val diseaseInfo = intent.getStringExtra("disease_info") ?: getString(R.string.default_disease_info)
        val cureInfo = intent.getStringExtra("cure_info") ?: getString(R.string.default_cure_info)

        // Handle unknown disease case
        if (diseaseName == "Unknown Disease") {
            binding.tvConfidence.text = "Could not determine disease"
            binding.tvDiseaseInfo.text = "Unable to analyze the image. Please try again."
            binding.tvCureInfo.text = "Please consult an agricultural expert."
        } else {
            // Set normal UI elements for known diseases
            binding.tvDiseaseName.text = diseaseName
            binding.tvConfidence.text = "Confidence: ${(confidence * 100).toInt()}%"
            binding.tvDiseaseInfo.text = diseaseInfo
            binding.tvCureInfo.text = cureInfo
        }

        // Home button click handler
        binding.btnHome.setOnClickListener {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
            finish()
        }
    }
}