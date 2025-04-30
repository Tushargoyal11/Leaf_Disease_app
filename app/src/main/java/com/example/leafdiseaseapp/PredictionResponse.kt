package com.example.leafdiseaseapp

data class PredictionResponse(
    val prediction: String,
    val confidence: Float,
    val error: String? = null
)