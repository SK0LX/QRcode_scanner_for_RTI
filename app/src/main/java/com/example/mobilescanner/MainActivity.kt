package com.example.mobilescanner

import ScanTable
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.mobilescanner.ui.theme.MobileScannerTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var scanTable: ScanTable
    private var lastCount: String = "Введите число"
    private var lastType: String = "Кол-во"
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Не сохранено", Toast.LENGTH_SHORT).show()
        } else {
            scanTable.add(result.contents)
            lastScannedCode = result.contents
            lastCount = scanTable.takeLastCount()
            lastType = scanTable.takeLastType()
        }
    }

    private var lastScannedCode by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanTable = ScanTable(this)
        enableEdgeToEdge()
        setContent {
            MobileScannerTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        var quantity by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Поле для отображения последнего отсканированного QR-кода
            OutlinedTextField(
                value = lastScannedCode,
                onValueChange = {},
                label = { Text("Последний QR-код") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp)
            )

            // Кнопка для сканирования
            Button(onClick = { scan() }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(text = "Сканировать код")
            }

            // Поле для ввода количества
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                                    },
                    modifier = Modifier.weight(1f),
                    label = { Text(lastCount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(text = lastType, modifier = Modifier.padding(start = 8.dp))
            }

            // Кнопка для сохранения с проверкой на пустое значение
            Button(
                onClick = {
                    if (quantity.isNotEmpty() && quantity.all { it.isDigit() }) {
                        scanTable.saveChangesFromUser(quantity.toInt())
                    } else {
                        Toast.makeText(this@MainActivity, "Введите корректное число", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(text = "Сохранить")
            }

            // Кнопка для отправки
            Button(onClick = { share() }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(text = "Отправить")
            }
        }
    }
    private fun scan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Сканирую в QRCode")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)

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