package com.gestiontienda.android.data.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface BarcodeScannerService {
    fun startScanning()
    fun stopScanning()
    fun getScanResults(): Flow<ScanResult>
    suspend fun scanSingle(): ScanResult
    fun isSupported(): Boolean
    fun hasPermissions(): Boolean
    fun requestPermissions()
    fun processCameraImage(image: InputImage)
}

@Singleton
class BarcodeScannerServiceImpl @Inject constructor(
    private val context: Context,
) : BarcodeScannerService {
    private val scanResults = MutableStateFlow<ScanResult>(ScanResult.Idle)
    private var barcodeScanner: BarcodeScanner? = null

    init {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }

    override fun startScanning() {
        if (!hasPermissions()) {
            scanResults.value = ScanResult.Error("Camera permission not granted")
            return
        }
        scanResults.value = ScanResult.Scanning
        // Camera preview will be handled by CameraX in the UI layer
    }

    override fun stopScanning() {
        scanResults.value = ScanResult.Idle
        // Camera preview stop will be handled by CameraX in the UI layer
    }

    override fun getScanResults(): Flow<ScanResult> = scanResults

    override suspend fun scanSingle(): ScanResult {
        if (!hasPermissions()) {
            return ScanResult.Error("Camera permission not granted")
        }
        // This will be triggered by a button in the UI to capture a single frame
        return ScanResult.Scanning
    }

    override fun isSupported(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    override fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermissions() {
        // Permissions will be requested in the UI layer using Activity/Fragment
    }

    override fun processCameraImage(image: InputImage) {
        barcodeScanner?.process(image)
            ?.addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes[0]
                    scanResults.value = ScanResult.Success(
                        barcode = barcode.rawValue ?: "",
                        format = when (barcode.format) {
                            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
                            Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
                            Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
                            Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
                            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
                            else -> BarcodeFormat.UNKNOWN
                        }
                    )
                }
            }
            ?.addOnFailureListener { e ->
                scanResults.value = ScanResult.Error(e.message ?: "Unknown error")
            }
    }

    companion object {
        // Placeholder for future hardware scanner configuration
        private const val HARDWARE_SCANNER_ENABLED = false
    }
}

sealed class ScanResult {
    object Idle : ScanResult()
    object Scanning : ScanResult()
    data class Success(
        val barcode: String,
        val format: BarcodeFormat = BarcodeFormat.UNKNOWN,
    ) : ScanResult()

    data class Error(val message: String) : ScanResult()
}

enum class BarcodeFormat {
    EAN_13,
    EAN_8,
    CODE_39,
    CODE_128,
    QR_CODE,
    UNKNOWN
} 