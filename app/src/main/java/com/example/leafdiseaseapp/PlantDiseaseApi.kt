package com.example.leafdiseaseapp

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PlantDiseaseApi {
    @Multipart
    @POST("predict")  // Matches your Flask endpoint
    fun analyzeImage(
        @Part file: MultipartBody.Part
    ): Call<PredictionResponse>
}