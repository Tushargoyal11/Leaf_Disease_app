package com.example.leafdiseaseapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowInsetsAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.leafdiseaseapp.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val GALLERY_REQUEST_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private var imageUri: Uri? = null
    private val PERMISSION_REQUEST_CODE = 200
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        setupClickListeners()
    }
    private fun checkPermissions() {
        if (REQUIRED_PERMISSIONS.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    private fun resetMainActivity() {
        binding.ivLeafImage.setImageDrawable(null)
        binding.tvPlaceholder.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false
    }
    private fun setupClickListeners() {
        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnAnalyze.setOnClickListener { analyzeImage() }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted - you can proceed with camera/gallery operations
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                // Optionally disable camera/gallery buttons if permissions are denied
            }
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    imageUri = data?.data
                    loadImage()
                }
                CAMERA_REQUEST_CODE -> {
                    loadImage()
                }
            }
        }
    }

    private fun loadImage() {
        imageUri?.let { uri ->
            binding.ivLeafImage.setImageURI(uri)
            binding.tvPlaceholder.visibility = View.GONE
            binding.btnAnalyze.isEnabled = true
        }
    }
    private fun analyzeImage() {
        // Check if image is selected
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false

        try {
            Log.d("API_DEBUG", "Starting analysis for URI: $imageUri")
            val path = getRealPathFromURI(imageUri!!) ?: run {
                Log.e("API_DEBUG", "Failed to get path from URI")
                return
            }
            Log.d("API_DEBUG", "Resolved path: $path")

            val file = File(path).also {
                Log.d("API_DEBUG", "File exists: ${it.exists()}, size: ${it.length()} bytes")
            }
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Make API call
            Log.d("API_DEBUG", "Sending request to API...")
            ApiClient.plantDiseaseService.analyzeImage(filePart).enqueue(
                object : Callback<PredictionResponse> {
                    override fun onResponse(
                        call: Call<PredictionResponse>,
                        response: Response<PredictionResponse>
                    ) {
                        Log.d("API_DEBUG", "Response received. Code: ${response.code()}")

                        if (!response.isSuccessful) {
                            Log.e("API_DEBUG", "Unsuccessful response: ${response.errorBody()?.string()}")
                        }
                        binding.progressBar.visibility = View.GONE

                        if (response.isSuccessful) {
                            response.body()?.let { result ->
                                if (result.error != null) {
                                    Toast.makeText(this@MainActivity, result.error, Toast.LENGTH_LONG).show()
                                } else {
                                    // Start ResultActivity with prediction data
                                    Intent(this@MainActivity, ResultActivity::class.java).apply {
                                        putExtra("disease_name", result.prediction)
                                        putExtra("confidence", result.confidence)
                                        putExtra("disease_info", getDiseaseInfo(result.prediction))
                                        putExtra("cure_info", getCureInfo(result.prediction))
                                        startActivity(this)
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Analysis failed: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                        binding.btnAnalyze.isEnabled = true
                    }

                    override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                        Log.e("API_DEBUG", "API call failed", t)
                        binding.progressBar.visibility = View.GONE
                        binding.btnAnalyze.isEnabled = true
                        Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("API_DEBUG", "Analysis failed", e)
            binding.progressBar.visibility = View.GONE
            binding.btnAnalyze.isEnabled = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    // Helper function to get disease info based on prediction
    private fun getDiseaseInfo(diseaseName: String): String {
        return when(diseaseName) {
            "Early Blight" -> getString(R.string.early_blight_info)
            "Fungal Diseases" -> getString(R.string.fungal_diseases_info)
            "Healthy" -> getString(R.string.healthy_info)
            "Late Blight" -> getString(R.string.late_blight_info)
            "Plant Pests" -> getString(R.string.plant_pests_info)
            "Potato Cyst Nematode" -> getString(R.string.potato_cyst_nematode_info)
            "Potato Virus" -> getString(R.string.potato_virus_info)
            else -> getString(R.string.default_disease_info)
        }
    }

    // Helper function to get cure info based on prediction
    private fun getCureInfo(diseaseName: String): String {
        return when(diseaseName) {
            "Early Blight" -> getString(R.string.early_blight_cure)
            "Fungal Diseases" -> getString(R.string.fungal_diseases_cure)
            "Healthy" -> getString(R.string.healthy_cure)
            "Late Blight" -> getString(R.string.late_blight_cure)
            "Plant Pests" -> getString(R.string.plant_pests_cure)
            "Potato Cyst Nematode" -> getString(R.string.potato_cyst_nematode_cure)
            "Potato Virus" -> getString(R.string.potato_virus_cure)
            else -> getString(R.string.default_cure_info)
        }
    }

    // Helper function to get real path from URI
    private fun getRealPathFromURI(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                tempFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e("FILE_ERROR", "Failed to get file path", e)
            null
        }
    }

}