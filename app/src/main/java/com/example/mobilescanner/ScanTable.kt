import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStreamReader
import java.io.BufferedReader

data class ScanRecord(
    val scanDateTime: String,
    val routeSheetNumber: String,
    val partName: String,
    val partCode: String,
    val quantity: Int,
    val measurement: String
)

class ScanTable(private val context: Context) {
    private val records = mutableListOf<ScanRecord>()
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private fun addTable(record: ScanRecord) {
        records.add(record)
        saveToExcel()
    }

    private fun addTable(data: List<String>) {
        records.add(
            ScanRecord(
                scanDateTime = data[0],
                routeSheetNumber = data[1].trim(),
                partName = data[2].trim(),
                partCode = data[3].trim(),
                quantity = data[4].trim().toInt(),
                measurement = data[5].trim()
            )
        )
        saveToExcel()
    }

    private fun saveToExcel() {
        // Если разрешение есть, сохраняем таблицу
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Scan Records")

        // Заголовок
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Дата/время сканирования")
        headerRow.createCell(1).setCellValue("№ маршрутного листа")
        headerRow.createCell(2).setCellValue("Наименование детали")
        headerRow.createCell(3).setCellValue("Код детали")
        headerRow.createCell(4).setCellValue("Количество")
        headerRow.createCell(5).setCellValue("Единица измерения")

        // Заполнение данными
        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(record.scanDateTime)
            row.createCell(1).setCellValue(record.routeSheetNumber)
            row.createCell(2).setCellValue(record.partName)
            row.createCell(3).setCellValue(record.partCode)
            row.createCell(4).setCellValue(record.quantity.toDouble())
            row.createCell(5).setCellValue(record.measurement)
        }

        // Сохранение файла
        val filename = "scan_records.xls"
        val file = File(context.getExternalFilesDir(null), filename)
        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)
        workbook.close()
        outputStream.close()
        Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
    }

    fun add(data: String) {
        try {
            // Создание строки с текущей датой и данными, затем разделение на элементы
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val currentDateTime = dateFormat.format(Date())
            val info = ("$currentDateTime;$data").split(";")

            // Проверка, чтобы количество элементов было равно 5
            if (info.size != 6) {
                throw IllegalArgumentException("Неверное количество элементов: ${info.size}")
            }

            // Добавление информации в таблицу
            addTable(info)

        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Неверный формат данных")
        }
    }
}


