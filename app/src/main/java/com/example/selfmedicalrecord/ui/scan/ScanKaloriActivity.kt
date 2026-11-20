package com.example.selfmedicalrecord.ui.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.TorchState
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityScanKaloriBinding
import com.example.selfmedicalrecord.ui.result.HasilKaloriActivity
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Layar penuh untuk memotret makanan (atau memilih foto dari galeri), lalu membuka
 * [HasilKaloriActivity]. Dibuat sebagai Activity sendiri, bukan fragment, supaya bottom nav
 * hilang dan kamera tidak ikut siklus hidup NavController.
 */
class ScanKaloriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanKaloriBinding
    private lateinit var cameraController: LifecycleCameraController

    private var cameraBound = false
    private var isCapturing = false

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) showCamera() else showPermissionNeeded()
        }

    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) openResult(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Latar kamera selalu gelap, jadi ikon status bar dan nav bar dipaksa terang.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        binding = ActivityScanKaloriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.overlay.applySystemBarInsetsAsPadding()

        cameraController = LifecycleCameraController(this)
        binding.previewView.controller = cameraController
        cameraController.torchState.observe(this) { state ->
            binding.btnFlash.setImageResource(
                if (state == TorchState.ON) R.drawable.ic_flash_on else R.drawable.ic_flash_off
            )
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnGallery.setOnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnShutter.setOnClickListener { takePhoto() }
        binding.btnFlash.setOnClickListener { toggleFlash() }
        binding.btnPermission.setOnClickListener { onPermissionButtonClicked() }

        if (!hasCameraPermission()) {
            setCameraControlsEnabled(false)
            if (savedInstanceState == null) {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            } else {
                showPermissionNeeded()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Kembali dari halaman hasil atau dari Pengaturan (izin mungkin baru diberikan).
        isCapturing = false
        if (hasCameraPermission()) showCamera()
        clearOldCaptures()
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun showCamera() {
        binding.layoutPermission.isVisible = false
        setCameraControlsEnabled(true)
        if (!cameraBound) {
            cameraController.bindToLifecycle(this)
            cameraBound = true
        }
    }

    private fun showPermissionNeeded() {
        setCameraControlsEnabled(false)
        binding.layoutPermission.isVisible = true
        // Sudah pernah ditolak sekali: sistem masih mau menampilkan dialog. Kalau tidak, arahkan ke Pengaturan.
        binding.btnPermission.setText(
            if (canAskPermissionAgain()) R.string.scan_permission_grant else R.string.scan_permission_settings
        )
    }

    private fun canAskPermissionAgain() =
        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)

    private fun onPermissionButtonClicked() {
        if (canAskPermissionAgain()) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            )
        }
    }

    private fun setCameraControlsEnabled(enabled: Boolean) {
        binding.btnShutter.isEnabled = enabled
        binding.btnFlash.isEnabled = enabled
        val alpha = if (enabled) 1f else 0.4f
        binding.btnShutter.alpha = alpha
        binding.btnFlash.alpha = alpha
    }

    private fun toggleFlash() {
        if (cameraController.cameraInfo?.hasFlashUnit() != true) {
            Toast.makeText(this, R.string.scan_error_no_flash, Toast.LENGTH_SHORT).show()
            return
        }
        cameraController.enableTorch(cameraController.torchState.value != TorchState.ON)
    }

    private fun takePhoto() {
        if (isCapturing || !cameraBound) return
        isCapturing = true
        binding.btnShutter.isEnabled = false

        val dir = File(cacheDir, CAPTURE_DIR).apply { mkdirs() }
        val file = File(dir, "scan_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(file).build()

        try {
            cameraController.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        openResult(Uri.fromFile(file))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onCaptureFailed()
                    }
                }
            )
        } catch (e: IllegalStateException) {
            onCaptureFailed() // kamera belum siap
        }
    }

    private fun onCaptureFailed() {
        isCapturing = false
        binding.btnShutter.isEnabled = true
        Toast.makeText(this, R.string.scan_error_capture, Toast.LENGTH_SHORT).show()
    }

    private fun openResult(photoUri: Uri) {
        startActivity(HasilKaloriActivity.newIntent(this, photoUri))
    }

    /** Foto sementara hanya dibutuhkan sampai halaman hasil ditutup; bersihkan saat kamera kembali aktif. */
    private fun clearOldCaptures() {
        val dir = File(cacheDir, CAPTURE_DIR)
        lifecycleScope.launch(Dispatchers.IO) {
            dir.listFiles()?.forEach { it.delete() }
        }
    }

    private companion object {
        const val CAPTURE_DIR = "scan"
    }
}
