package com.example.leafdiseaseapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.leafdiseaseapp.databinding.ActivityMainBinding


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
        // Your image analysis logic here
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false

        // Simulate analysis (replace with actual logic)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Analysis complete", Toast.LENGTH_SHORT).show()
            binding.btnAnalyze.isEnabled = true
        }, 2000)
    }

}