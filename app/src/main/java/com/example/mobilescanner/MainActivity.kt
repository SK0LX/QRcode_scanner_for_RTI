package com.example.mobilescanner

import ScanTable
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.text.isDigitsOnly
import com.example.mobilescanner.ui.theme.MobileScannerTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var scanTable: ScanTable
    private var lastCount: String = ""
    private var lastType: String = "Кол-во"
    private var quantity by mutableStateOf("")
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Не сохранено", Toast.LENGTH_SHORT).show()
        } else {
            scanTable.add(result.contents)
            lastScannedCode = result.contents
            lastCount = scanTable.takeLastCount()

            lastType = scanTable.takeLastType()
            if (lastType == "ШТ"){
                lastCount = lastCount.split(".")[0]
            }
            quantity = lastCount

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

    private fun scan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Сканирую в QRCode")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        options.setTorchEnabled(true)

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

                startActivity(Intent.createChooser(intent, "Share using")) // Открываем диалог для отправки файла
                clearUI()
                scanTable.clear()
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка при попытке поделиться файлом: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Файл не найден!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun clearUI(){
        lastScannedCode = ""
        lastType = "Кол-во"
        lastCount = ""
        quantity = ""
    }

    @Preview
    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        var showError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize().padding(16.dp)
        ) {
            // Верхняя часть с изображением и текстом
            Box(
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_rti),
                        contentDescription = "Изображение",
                        modifier = Modifier
                            .size(30.dp)
                    )

                    Column {
                        Text(
                            text = "ПАО Уральский завод РТИ",
                            fontSize = 15.sp,
                            color = Color(0xFF800080)
                        )
                    }
                }
            }


            // Поле для отображения последнего отсканированного QR-кода
            OutlinedTextField(
                value = lastScannedCode,
                onValueChange = {},
                label = { Text("Последний QR-код") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .padding(bottom = 16.dp, top = 5.dp)
            )

            // Кнопка для сканирования
            Button(
                onClick = { scan() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(shape = RoundedCornerShape(6.dp))
            ) {
                Text(text = "Сканировать код")
            }

            // Поле для ввода количества
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.width(120.dp).height(70.dp),
                    contentAlignment = Alignment.Center // Центрируем содержимое внутри Box
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            val regex = if (lastType == "ШТ") {
                                Regex("\\d{1,4}") // Целые числа до 4 знаков
                            } else {
                                Regex("\\d{1,4}+(\\.\\d{0,3})?") // Нечеткие числа с 3 знаками после запятой
                            }

                            // Проверяем, соответствует ли ввод регулярному выражению
                            if (it.isEmpty() || regex.matches(it)) {
                                quantity = it
                            }
                        },
                        label = { Text(lastCount) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth() // Используем fillMaxWidth для растягивания поля
                    )
                }

                // Используем Box для центрирования текста
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                        .height(60.dp), // Используем weight для равномерного распределения пространства
                    contentAlignment = Alignment.Center // Центрируем содержимое внутри Box
                ) {
                    Text(
                        text = lastType,
                        textAlign = TextAlign.Center, // Устанавливаем выравнивание текста по центру
                        fontSize = 25.sp
                    )
                }
            }

            // Кнопка для сохранения с проверкой на пустое значение
            Button(
                onClick = {
                    if (quantity.isNotEmpty()) {
                        if (quantity.toDouble() > 0)
                            scanTable.saveChangesFromUser(quantity.toDouble())
                        else {
                            Toast.makeText(
                                this@MainActivity,
                                "Введенное число больше 0.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Введите корректное число", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(shape = RoundedCornerShape(6.dp))
            ) {
                Text(text = "Сохранить")
            }

            // Кнопка для отправки
            Button(
                onClick = {
                    share()
                    scanTable.clear() // очистка таблицы
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(6.dp))
            ) {
                Text(text = "Отправить")
            }
        }
    }
}