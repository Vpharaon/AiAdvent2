package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HomeScreenItem(
    val icon: String,
    val title: String,
    val description: String,
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onOpenEventPlanner: () -> Unit,
    onOpenRecipes: () -> Unit,
    onOpenChat: () -> Unit
) {
    val items = listOf(
        HomeScreenItem(
            icon = "🎉",
            title = "Менеджер ресторана",
            description = "Организация корпоратива",
            containerColor = { MaterialTheme.colorScheme.primaryContainer },
            contentColor = { MaterialTheme.colorScheme.onPrimaryContainer },
            onClick = onOpenEventPlanner
        ),
        HomeScreenItem(
            icon = "👨‍🍳",
            title = "Шеф-повар",
            description = "Получить рецепт блюда",
            containerColor = { MaterialTheme.colorScheme.secondaryContainer },
            contentColor = { MaterialTheme.colorScheme.onSecondaryContainer },
            onClick = onOpenRecipes
        ),
        HomeScreenItem(
            icon = "💬",
            title = "Чат",
            description = "Общение с AI ассистентом",
            containerColor = { MaterialTheme.colorScheme.tertiaryContainer },
            contentColor = { MaterialTheme.colorScheme.onTertiaryContainer },
            onClick = onOpenChat
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items) { item ->
                HomeScreenCard(
                    icon = item.icon,
                    title = item.title,
                    description = item.description,
                    containerColor = item.containerColor(),
                    contentColor = item.contentColor(),
                    onClick = item.onClick
                )
            }
        }
    }
}

@Composable
fun HomeScreenCard(
    icon: String,
    title: String,
    description: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}