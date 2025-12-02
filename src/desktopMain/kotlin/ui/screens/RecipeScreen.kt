package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import data.repository.StructuredChatRepository
import domain.structured.RecipeResponse
import domain.structured.RecipeWithRaw
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class RecipeTab {
    FORMATTED, RAW_JSON
}

@Composable
fun RecipeScreen(onBack: () -> Unit) {
    val repository: StructuredChatRepository = koinInject()
    val scope = rememberCoroutineScope()

    var dishName by remember { mutableStateOf("") }
    var recipeData by remember { mutableStateOf<RecipeWithRaw?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(RecipeTab.FORMATTED) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Шеф-повар",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onBack) {
                Text("←", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Поле ввода
        OutlinedTextField(
            value = dishName,
            onValueChange = { dishName = it },
            label = { Text("Название блюда") },
            placeholder = { Text("Например: Борщ, Тирамису, Карбонара...") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка получения рецепта
        Button(
            onClick = {
                if (dishName.isNotBlank()) {
                    isLoading = true
                    errorMessage = null
                    recipeData = null

                    scope.launch {
                        val result = repository.getRecipeWithRaw(dishName)
                        isLoading = false

                        result.onSuccess { data ->
                            recipeData = data
                            selectedTab = RecipeTab.FORMATTED
                        }.onFailure { error ->
                            errorMessage = "Ошибка: ${error.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && dishName.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isLoading) "Готовлю рецепт..." else "Получить рецепт")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Отображение ошибки
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Отображение рецепта
        recipeData?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Табы
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    Tab(
                        selected = selectedTab == RecipeTab.FORMATTED,
                        onClick = { selectedTab = RecipeTab.FORMATTED },
                        text = { Text("Рецепт") }
                    )
                    Tab(
                        selected = selectedTab == RecipeTab.RAW_JSON,
                        onClick = { selectedTab = RecipeTab.RAW_JSON },
                        text = { Text("Raw JSON") }
                    )
                }

                // Контент вкладок
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    when (selectedTab) {
                        RecipeTab.FORMATTED -> {
                            FormattedRecipeView(data.recipe)
                        }
                        RecipeTab.RAW_JSON -> {
                            JsonView(data.rawJson)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedRecipeView(recipe: RecipeResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Название и страна
        Text(
            text = recipe.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "\uD83C\uDF0D ${recipe.country}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        HorizontalDivider()

        // История
        Text(
            text = "\uD83D\uDCDC История",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = recipe.history,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider()

        // Ингредиенты
        Text(
            text = "\uD83E\uDD5B Ингредиенты",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        recipe.ingredients.forEach { ingredient ->
            val unit = if (ingredient.unit != null) " ${ingredient.unit}" else ""
            Text(
                text = "• ${ingredient.name}: ${ingredient.amount}${unit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider()

        // Инструкции
        Text(
            text = "\uD83D\uDC68\u200D\uD83C\uDF73 Приготовление",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        recipe.instructions.forEachIndexed { index, instruction ->
            Text(
                text = "${index + 1}. $instruction",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun JsonView(jsonContent: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = jsonContent,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}