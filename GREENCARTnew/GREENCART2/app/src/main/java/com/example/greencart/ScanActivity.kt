package com.example.greencart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var tvResult: TextView
    private lateinit var btnFlash: MaterialButton

    private var camera: Camera? = null
    private var analysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService

    // Cooldown to prevent duplicate rapid handling
    private var lastHandledAt = 0L
    private val handleIntervalMs = 1500L

    // ML Kit Image Labeler (on-device)
    private var labeler: ImageLabeler? = null

    private val positiveKeywords = listOf(
        "bamboo", "paper", "cardboard", "glass", "metal can", "aluminum can",
        "recycle", "recycling symbol", "recyclable", "cloth", "cotton", "jute",
        "wood", "wooden", "stainless steel"
    )
    private val negativeKeywords = listOf(
        "plastic", "plastic bottle", "styrofoam", "polystyrene", "single-use", "disposable"
    )

    private fun isEcoFriendly(labels: List<ImageLabel>): Boolean {
        var score = 0.0
        for (label in labels) {
            val name = label.text.lowercase()
            val conf = label.confidence.toDouble()
            if (conf < 0.6) continue
            if (positiveKeywords.any { name.contains(it) }) score += 1
            if (negativeKeywords.any { name.contains(it) }) score -= 1
        }
        return score >= 1.0
    }

    private fun signatureFrom(labels: List<ImageLabel>): String {
        return labels
            .sortedByDescending { it.confidence }
            .take(3)
            .joinToString("|") { it.text.lowercase() }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera() else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        previewView = findViewById(R.id.previewView)
        tvResult = findViewById(R.id.tvResult)
        btnFlash = findViewById(R.id.btnFlash)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize labeler safely
        try {
            labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        } catch (t: Throwable) {
            labeler = null
            Toast.makeText(this, "Image labeling unavailable.", Toast.LENGTH_LONG).show()
        }

        btnFlash.setOnClickListener {
            val cam = camera ?: return@setOnClickListener
            val hasFlash = cam.cameraInfo.hasFlashUnit()
            if (!hasFlash) return@setOnClickListener
            val torchOn = cam.cameraInfo.torchState.value == TorchState.ON
            cam.cameraControl.enableTorch(!torchOn)
            btnFlash.text = if (torchOn) getString(R.string.scan_flash_on) else getString(R.string.scan_flash_off)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (_: Exception) { }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processImageProxy(imageProxy: ImageProxy) {
        val activeLabeler = labeler
        if (activeLabeler == null) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)
        activeLabeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    if (now - lastHandledAt >= handleIntervalMs) {
                        lastHandledAt = now

                        val eco = isEcoFriendly(labels)
                        if (eco) {
                            val prefs = getSharedPreferences("eco_prefs", MODE_PRIVATE)
                            val signatures = (prefs.getStringSet("awarded_signatures", emptySet()) ?: emptySet()).toMutableSet()
                            val signature = signatureFrom(labels)
                            if (signature.isNotBlank() && signatures.add(signature)) {
                                val codesSize = prefs.getStringSet("awarded_codes", emptySet())?.size ?: 0
                                val newUniqueCount = codesSize + signatures.size
                                val points = prefs.getInt("points", 0) + 10
                                prefs.edit()
                                    .putStringSet("awarded_signatures", signatures)
                                    .putInt("scan_count", newUniqueCount)
                                    .putInt("points", points)
                                    .apply()
                                runOnUiThread {
                                    tvResult.text = "Eco-Friendly Product!\n+10 points"
                                    Toast.makeText(this, "You've earned 10 points!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                runOnUiThread {
                                    tvResult.text = "Eco-Friendly Product (already counted)"
                                    Toast.makeText(this, "Already rewarded for this product.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                tvResult.text = "Not an eco-friendly product."
                                Toast.makeText(this, "No points awarded.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { /* ignore one-off failures */ }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        analysis?.clearAnalyzer()
        cameraExecutor.shutdown()
    }
}