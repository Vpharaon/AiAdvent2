# LLM Chat - Desktop приложение

Desktop-приложение для общения с LLM (Large Language Model) на базе Kotlin Multiplatform и Compose Desktop.

## Особенности

- ✅ Kotlin Multiplatform + Compose Desktop
- ✅ Интеграция с OpenAI API (или другими LLM)
- ✅ In-memory хранение диалога
- ✅ Красивый UI с различием сообщений пользователя и бота
- ✅ Индикатор "Bot is typing..."
- ✅ Автопрокрутка сообщений
- ✅ Отправка по Enter (Shift+Enter для новой строки)
- ✅ Очистка диалога
- ✅ Управление зависимостями через Koin

## Требования

- JDK 17 или выше
- OpenAI API ключ (или другой LLM API)

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <url-репозитория>
cd AiAdvent2
```

### 2. Настройка API ключа

Установите переменную окружения с вашим OpenAI API ключом:

**macOS/Linux:**
```bash
export OPENAI_API_KEY="ваш-api-ключ"
```

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="ваш-api-ключ"
```

**Windows (CMD):**
```cmd
set OPENAI_API_KEY=ваш-api-ключ
```

Или измените значение по умолчанию в файле `src/desktopMain/kotlin/Main.kt`:
```kotlin
val apiKey = System.getenv("OPENAI_API_KEY") ?: "ваш-api-ключ"
```

### 3. Сборка проекта

```bash
./gradlew build
```

### 4. Запуск приложения

```bash
./gradlew run
```

## Архитектура

Проект следует чистой архитектуре и разделен на слои:

```
src/
├── commonMain/
│   ├── kotlin/
│   │   ├── model/           # Модели данных (Message, LLMResponse)
│   │   ├── repository/      # Репозиторий (ChatRepository)
│   │   ├── viewmodel/       # ViewModel (ChatViewModel)
│   │   ├── network/         # Сетевой слой (LLMApiClient)
│   │   └── di/              # Dependency Injection (Koin modules)
└── desktopMain/
    └── kotlin/
        └── Main.kt          # UI (Compose Desktop)
```

### Компоненты

1. **Model Layer** (`model/`)
   - `Message` - модель сообщения (id, content, isUser, timestamp)
   - `LLMResponse` - модель ответа от LLM (message, success)

2. **Repository Layer** (`repository/`)
   - `ChatRepository` - управление сообщениями в памяти (in-memory cache)
   - Методы: `sendMessage()`, `getMessages()`, `clearMessages()`

3. **ViewModel Layer** (`viewmodel/`)
   - `ChatViewModel` - управление состоянием UI
   - Состояния: `messages`, `input`, `isTyping`
   - Методы: `sendMessage()`, `clearChat()`, `updateInput()`

4. **Network Layer** (`network/`)
   - `LLMApiClient` - клиент для работы с OpenAI API
   - Поддержка настройки model, apiUrl, apiKey

5. **UI Layer** (`Main.kt`)
   - Compose Desktop интерфейс
   - Компоненты: `ChatScreen`, `MessagesArea`, `MessageItem`, `InputArea`, `TypingIndicator`

6. **DI Layer** (`di/`)
   - Koin модули для управления зависимостями

## Тестирование

### Проверка основных функций:

1. **Отправка сообщений:**
   - Введите текст в поле ввода
   - Нажмите Enter или кнопку "Send"
   - Проверьте, что сообщение пользователя появилось справа (синий фон)
   - Проверьте индикатор "Bot is typing..."
   - Проверьте, что ответ бота появился слева (серый фон)

2. **Защита от пустых сообщений:**
   - Попробуйте отправить пустое сообщение
   - Кнопка "Send" должна быть неактивна

3. **Очистка диалога:**
   - Нажмите кнопку "Clear Chat"
   - Проверьте, что все сообщения удалены
   - Проверьте, что появилось новое приветственное сообщение

4. **Автопрокрутка:**
   - Отправьте несколько сообщений
   - Проверьте, что список автоматически прокручивается вниз

5. **Отправка по Enter:**
   - Нажмите Enter в поле ввода - сообщение отправляется
   - Нажмите Shift+Enter - добавляется новая строка

6. **Обработка ошибок:**
   - Введите неправильный API ключ или отключите интернет
   - Проверьте, что приложение не падает и показывает сообщение об ошибке

## Сборка исполняемого файла

Для создания исполняемого jar-файла:

```bash
./gradlew packageUberJarForCurrentOS
```

Или для создания нативного установщика:

```bash
./gradlew packageDistributionForCurrentOS
```

## Настройка

### Изменение LLM провайдера

Чтобы использовать другой LLM API (не OpenAI), отредактируйте `src/commonMain/kotlin/network/LLMApiClient.kt`:

1. Измените `apiUrl` на URL вашего API
2. Адаптируйте структуры запроса/ответа (`OpenAIRequest`, `OpenAIResponse`)
3. Обновите заголовки и формат авторизации

### Изменение модели

В `LLMApiClient` по умолчанию используется `gpt-3.5-turbo`. Чтобы изменить:

```kotlin
class LLMApiClient(
    private val apiKey: String,
    private val apiUrl: String = "https://api.openai.com/v1/chat/completions",
    private val model: String = "gpt-4" // Измените здесь
)
```

## Структура проекта

```
AiAdvent2/
├── build.gradle.kts              # Конфигурация Gradle
├── settings.gradle.kts           # Настройки проекта
├── README.md                     # Этот файл
├── desktop_llm_chat_plan.md     # План разработки
├── src/
│   ├── commonMain/              # Общий код для всех платформ
│   │   └── kotlin/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── viewmodel/
│   │       ├── network/
│   │       └── di/
│   └── desktopMain/            # Код специфичный для Desktop
│       └── kotlin/
│           └── Main.kt
└── gradle/                      # Gradle Wrapper
```

## Зависимости

- **Kotlin Multiplatform** 2.2.20
- **Compose Multiplatform Desktop** 1.7.1
- **Ktor Client** 3.0.1 (для HTTP запросов)
- **Kotlinx Serialization** 1.7.3 (для JSON)
- **Kotlinx Coroutines** 1.9.0 (для асинхронности)
- **Koin** 4.0.0 (для DI)

## Лицензия

[Укажите вашу лицензию]

## Автор

[Укажите ваше имя]