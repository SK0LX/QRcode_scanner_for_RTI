package com.example.mobilescanner

import ScanTable
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import com.example.mobilescanner.ui.theme.MobileScannerTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File


class MainActivity : ComponentActivity() {
    private lateinit var scanTable: ScanTable
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            // Обработка ошибки сканирования
        } else {
            scanTable.add(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanTable = ScanTable(this)
        enableEdgeToEdge()
        setContent {
            MobileScannerTheme {
                Column(modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Button(onClick = { scan() }) {
                        Text(text = "Scan")
                    }
                    Button(onClick = { share() }) {
                        Text(text = "Share")
                    }
                }
            }
        }
    }

    private fun scan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan a QRCode")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        options.setBeepEnabled(false)

        scanLauncher.launch(options)
    }

    private fun share() {
        val fileToShare = File(getExternalFilesDir(null), "scan_records.xls")
        Log.d("ShareFile", "Path: ${fileToShare.absolutePath}")

        if (fileToShare.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    this,
                    "$packageName.fileProvider",
                    fileToShare
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.ms-excel"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(intent, "Share using"))
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка при попытке поделиться файлом: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Файл не найден!", Toast.LENGTH_SHORT).show()
        }
    }
}

