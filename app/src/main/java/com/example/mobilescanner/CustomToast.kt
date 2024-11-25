import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class CustomToast(private val context: Context) {

    private var message: String = ""
    private var backgroundColor: Int = Color.BLACK
    private var textColor: Int = Color.WHITE
    private var textSize: Float = 16f
    private var duration: Long = 2000
    private var gravity: Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
    private var yOffset: Int = 200
    private var padding: Int = 16

    private var toastView: View? = null

    fun setText(message: String): CustomToast {
        this.message = message
        return this
    }

    fun setBackgroundColor(color: Int): CustomToast {
        this.backgroundColor = color
        return this
    }

    fun setTextColor(color: Int): CustomToast {
        this.textColor = color
        return this
    }

    fun setTextSize(size: Float): CustomToast {
        this.textSize = size
        return this
    }

    fun setDuration(duration: Long): CustomToast {
        this.duration = duration
        return this
    }

    fun setGravity(gravity: Int, yOffset: Int = 200): CustomToast {
        this.gravity = gravity
        this.yOffset = yOffset
        return this
    }

    fun show() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(context)

        // Создание текстового вида для отображения
        toastView = TextView(context).apply {
            text = message
            textSize = this@CustomToast.textSize
            setTextColor(this@CustomToast.textColor)
            gravity = Gravity.CENTER // Выровнять текст по центру
            setPadding(padding, padding, padding, padding)

            // Создание фона с круглыми углами
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 16f // Угол в пикселях
            drawable.setColor(this@CustomToast.backgroundColor) // Цвет фона
            background = drawable
        }

        // Настройка параметров для WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Для API 26+
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = this@CustomToast.gravity
            this.y = this@CustomToast.yOffset
        }

        // Добавление View
        windowManager.addView(toastView, params)

        // Удаление через заданное время
        Handler(Looper.getMainLooper()).postDelayed({
            removeToast(windowManager)
        }, duration)
    }

    private fun removeToast(windowManager: WindowManager) {
        toastView?.let {
            windowManager.removeView(it)
            toastView = null
        }
    }
}
