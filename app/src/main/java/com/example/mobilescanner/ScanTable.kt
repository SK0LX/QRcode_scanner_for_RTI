import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.graphics.Color
import com.example.mobilescanner.R
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanRecord(
    val scanDateTime: String,
    val routeSheetNumber: String,
    val partName: String,
    val partCode: String,
    var quantity: Double,
    val measurement: String
)

class ScanTable(private val context: Context) {
    private val records = mutableListOf<ScanRecord>()
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    public val isEmpty:Boolean
        get() {
            return records.size == 0
        }

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
                quantity = checkQuantity(data[4]),
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
        CustomToastMakeText(context, "Сохранено", Toast.LENGTH_SHORT,
            android.graphics.Color.BLACK)
    }

    fun add(data: String) {
        try {
            // Создание строки с текущей датой и данными, затем разделение на элементы
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val currentDateTime = dateFormat.format(Date())
            val info = ("$currentDateTime;$data").split(";")

            // Проверка, чтобы количество элементов было равно 6
            if (info.size != 6) {
                throw IllegalArgumentException("Неверное количество элементов: ${info.size}")
            }

            // Добавление информации в таблицу
            addTable(info)

        } catch (e: IllegalArgumentException) {
            // Обработка IllegalArgumentException
            CustomToastMakeText(context, e.toString(), Toast.LENGTH_SHORT,
                android.graphics.Color.RED)
        } catch (e: NumberFormatException) {
            // Обработка NumberFormatException
            CustomToastMakeText(context, "Неудалось считать данный QrCode.",
                Toast.LENGTH_SHORT, android.graphics.Color.RED)
        } catch (e: Exception) {
            // Обработка других исключений
            CustomToastMakeText(context, "Произошла ошибка: ${e.message}",
                Toast.LENGTH_SHORT, android.graphics.Color.RED)
        }
    }

    fun saveChangesFromUser(number: Double){
        if (records.size == 0){
            CustomToastMakeText(context, "Отсканируйте сначала QrCode",
                Toast.LENGTH_SHORT,android.graphics.Color.BLACK)
            return
        }
        records[records.size - 1].quantity = number
        saveToExcel()
    }

    fun takeLastCount(): String {
        if (records.size == 0)
            return ""
        return records[records.size - 1].quantity.toString()
    }

    fun takeLastType(): String {
        if (records.size == 0)
            return "Кол-во"
        return records[records.size - 1].measurement
    }

    fun clear(){
        records.clear()
    }

    fun checkQuantity(line:String) : Double{
        if (line == " " || line == ""){
            return 0.0
        }
        return line.toDouble()
    }

    fun CustomToastMakeText(context: Context, text: CharSequence, duration: Int, color: Int) {
        // Проверяем, есть ли разрешение на отображение оверлея
        if (!Settings.canDrawOverlays(context)) {
            // Если разрешение не предоставлено, запрашиваем его
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            // Запускаем активность для получения разрешения
            (context as Activity).startActivityForResult(intent, REQUEST_CODE)
        } else {
            // Если разрешение есть, показываем кастомный Toast
            CustomToast(context)
                .setText(text.toString())
                .setBackgroundColor(color)
                .setTextColor(android.graphics.Color.WHITE)
                .setTextSize(24f)
                .setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 100)
                .show()
        }
    }


}
