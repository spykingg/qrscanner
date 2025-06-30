package com.example.qrscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class MainActivity : AppCompatActivity() {
    private var barcodeView: DecoratedBarcodeView? = null
    private var btnFlash: ImageButton? = null
    private var isFlashOn = false
    private var isScanningPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupFlashButton()
        checkCameraPermission()
    }

    private fun initViews() {
        barcodeView = findViewById(R.id.barcode_scanner)
        btnFlash = findViewById<ImageButton>(R.id.btn_flash)
    }

    private fun setupFlashButton() {
        btnFlash!!.setOnClickListener { v: View? -> toggleFlash() }
    }

    private fun toggleFlash() {
        if (isFlashOn) {
            barcodeView?.setTorchOff()
            btnFlash!!.setImageResource(R.drawable.ic_flash_off)
            isFlashOn = false
        } else {
            barcodeView?.setTorchOn()
            btnFlash!!.setImageResource(R.drawable.ic_flash_on)
            isFlashOn = true
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            startScanning()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning()
            } else {
                Toast.makeText(
                    this, "Camera permission is required to scan QR codes",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun startScanning() {
        barcodeView?.decodeContinuous(object : BarcodeCallback {
            @RequiresPermission(Manifest.permission.VIBRATE)
            override fun barcodeResult(result: BarcodeResult) {
                if (!isScanningPaused) {
                    handleScanResult(result.getText())
                }
            }

            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint?>?) {
                // Optional: Handle possible result points for UI feedback
            }
        })
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleScanResult(scanResult: String) {
        isScanningPaused = true


        // Vibrate to provide feedback
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            // Vibration not available
        }

        // Pass result to ResultActivity
        val intent = Intent(
            this,
            ResultActivity::class.java
        )
        intent.putExtra("scan_result", scanResult)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (barcodeView != null) {
            barcodeView!!.resume()
            isScanningPaused = false
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView?.pause()
    }

    override fun onBackPressed() {
        if (barcodeView != null && barcodeView!!.onKeyDown(
                KeyEvent.KEYCODE_BACK,
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_BACK
                )
            )
        ) {
            return
        }
        super.onBackPressed()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 200
    }
}