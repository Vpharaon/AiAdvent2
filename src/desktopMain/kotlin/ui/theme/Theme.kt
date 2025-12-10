package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Палитра цветов для светлой темы.
 *
 * Используется мягкая, современная палитра с акцентом на читаемость и эстетику.
 * Основные цвета:
 * - Primary: Глубокий индиго (#3F51B5) - основной акцентный цвет
 * - Secondary: Теплый янтарный (#FFA726) - дополнительный акцент
 * - Background: Светло-серый (#F5F5F5) - фон приложения
 * - Surface: Белый (#FFFFFF) - поверхности карточек и компонентов
 */
private val LightColorScheme = lightColorScheme(
    // Основные цвета
    primary = Color(0xFF3F51B5),              // Глубокий индиго
    onPrimary = Color(0xFFFFFFFF),            // Белый текст на primary
    primaryContainer = Color(0xFFE8EAF6),     // Светлый контейнер primary
    onPrimaryContainer = Color(0xFF1A237E),   // Темный текст на контейнере

    // Дополнительные цвета
    secondary = Color(0xFFFFA726),            // Теплый янтарный
    onSecondary = Color(0xFF000000),          // Черный текст на secondary
    secondaryContainer = Color(0xFFFFE0B2),   // Светлый контейнер secondary
    onSecondaryContainer = Color(0xFFE65100), // Темный текст на контейнере

    // Третичные цвета
    tertiary = Color(0xFF26A69A),             // Морская волна
    onTertiary = Color(0xFFFFFFFF),           // Белый текст на tertiary
    tertiaryContainer = Color(0xFFB2DFDB),    // Светлый контейнер tertiary
    onTertiaryContainer = Color(0xFF004D40),  // Темный текст на контейнере

    // Фоновые цвета
    background = Color(0xFFF5F5F5),           // Светло-серый фон
    onBackground = Color(0xFF212121),         // Темный текст на фоне

    // Поверхности
    surface = Color(0xFFFFFFFF),              // Белые карточки
    onSurface = Color(0xFF212121),            // Темный текст на поверхности
    surfaceVariant = Color(0xFFE0E0E0),       // Вариант поверхности
    onSurfaceVariant = Color(0xFF424242),     // Текст на варианте поверхности

    // Контуры
    outline = Color(0xFFBDBDBD),              // Границы и разделители
    outlineVariant = Color(0xFFE0E0E0),       // Более светлые границы

    // Состояния
    error = Color(0xFFD32F2F),                // Красный для ошибок
    onError = Color(0xFFFFFFFF),              // Белый текст на ошибках
    errorContainer = Color(0xFFFFCDD2),       // Контейнер ошибки
    onErrorContainer = Color(0xFFB71C1C),     // Текст на контейнере ошибки

    // Дополнительные состояния
    surfaceTint = Color(0xFF3F51B5),          // Оттенок для поверхностей
    inverseSurface = Color(0xFF212121),       // Инверсная поверхность
    inverseOnSurface = Color(0xFFFFFFFF),     // Текст на инверсной поверхности
    inversePrimary = Color(0xFF9FA8DA),       // Инверсный primary
    scrim = Color(0x80000000),                // Затемнение для модальных окон
)

/**
 * Палитра цветов для темной темы.
 *
 * Элегантная темная палитра с мягкими акцентами, снижающая нагрузку на глаза.
 * Основные цвета:
 * - Primary: Светлый индиго (#7986CB) - основной акцентный цвет
 * - Secondary: Янтарный (#FFB74D) - дополнительный акцент
 * - Background: Темно-серый (#121212) - фон приложения
 * - Surface: Угольный (#1E1E1E) - поверхности карточек и компонентов
 */
private val DarkColorScheme = darkColorScheme(
    // Основные цвета
    primary = Color(0xFF7986CB),              // Светлый индиго
    onPrimary = Color(0xFF1A237E),            // Темный текст на primary
    primaryContainer = Color(0xFF303F9F),     // Темный контейнер primary
    onPrimaryContainer = Color(0xFFE8EAF6),   // Светлый текст на контейнере

    // Дополнительные цвета
    secondary = Color(0xFFFFB74D),            // Янтарный
    onSecondary = Color(0xFFE65100),          // Темный текст на secondary
    secondaryContainer = Color(0xFFFF6F00),   // Темный контейнер secondary
    onSecondaryContainer = Color(0xFFFFE0B2), // Светлый текст на контейнере

    // Третичные цвета
    tertiary = Color(0xFF4DB6AC),             // Светлая морская волна
    onTertiary = Color(0xFF004D40),           // Темный текст на tertiary
    tertiaryContainer = Color(0xFF00695C),    // Темный контейнер tertiary
    onTertiaryContainer = Color(0xFFB2DFDB),  // Светлый текст на контейнере

    // Фоновые цвета
    background = Color(0xFF121212),           // Почти черный фон
    onBackground = Color(0xFFE0E0E0),         // Светлый текст на фоне

    // Поверхности
    surface = Color(0xFF1E1E1E),              // Угольные карточки
    onSurface = Color(0xFFE0E0E0),            // Светлый текст на поверхности
    surfaceVariant = Color(0xFF2C2C2C),       // Вариант поверхности
    onSurfaceVariant = Color(0xFFBDBDBD),     // Текст на варианте поверхности

    // Контуры
    outline = Color(0xFF616161),              // Границы и разделители
    outlineVariant = Color(0xFF424242),       // Более темные границы

    // Состояния
    error = Color(0xFFEF5350),                // Яркий красный для ошибок
    onError = Color(0xFFB71C1C),              // Темный текст на ошибках
    errorContainer = Color(0xFFC62828),       // Контейнер ошибки
    onErrorContainer = Color(0xFFFFCDD2),     // Текст на контейнере ошибки

    // Дополнительные состояния
    surfaceTint = Color(0xFF7986CB),          // Оттенок для поверхностей
    inverseSurface = Color(0xFFE0E0E0),       // Инверсная поверхность
    inverseOnSurface = Color(0xFF212121),     // Текст на инверсной поверхности
    inversePrimary = Color(0xFF3F51B5),       // Инверсный primary
    scrim = Color(0x80000000),                // Затемнение для модальных окон
)

/**
 * Тема приложения AI Ассистент.
 *
 * Автоматически переключается между светлой и темной темой в зависимости
 * от системных настроек. Использует Material Design 3 для современного
 * и согласованного внешнего вида.
 *
 * @param darkTheme Использовать ли темную тему. По умолчанию определяется
 *                  системными настройками через [isSystemInDarkTheme]
 * @param content Composable контент, который будет отображаться с этой темой
 *
 * @sample
 * ```kotlin
 * AppTheme {
 *     // Ваш UI код здесь
 * }
 * ```
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Выбираем цветовую схему в зависимости от темы
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // Применяем тему Material Design 3 с выбранной цветовой схемой
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}