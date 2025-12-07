package ui.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Утилита для форматирования времени в UI слое.
 * Platform-specific реализация для JVM/Desktop.
 */
object TimeFormatter {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    /**
     * Форматирует timestamp в строку формата HH:mm
     *
     * @param timestamp Временная метка в миллисекундах с эпохи Unix
     * @return Отформатированное время (например, "14:35")
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
}