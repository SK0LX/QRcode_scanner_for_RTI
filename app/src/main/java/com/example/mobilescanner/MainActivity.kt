package com.example.mobilescanner

import ScanTable
import android.os.Bundle
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
import com.example.mobilescanner.ui.theme.MobileScannerTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions



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
}

