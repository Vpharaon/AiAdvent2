package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.structured.EventPlanResponse
import ui.components.InputArea
import ui.components.MessagesArea

@Composable
fun EventPlanScreen(component: component.EventPlannerComponent) {
    val state by component.state.collectAsState()
    val messages = state.messages
    val input = state.input
    val isTyping = state.isTyping
    val eventPlan = state.eventPlan
    val errorMessage = state.errorMessage
    val selectedTab = state.selectedTab

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
                text = "Менеджер ресторана",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = component::onBackClick) {
                Text("←", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Описание
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "Менеджер задаст вам от 4 до 10 вопросов для организации новогоднего корпоратива. После сбора информации вы получите план мероприятия с рекомендациями.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Отображение ошибки
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Табы
        if (eventPlan != null) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == mvi.eventplanner.EventPlannerStore.EventPlanTab.CHAT,
                    onClick = { component.onTabSelect(mvi.eventplanner.EventPlannerStore.EventPlanTab.CHAT) },
                    text = { Text("Диалог") }
                )
                Tab(
                    selected = selectedTab == mvi.eventplanner.EventPlannerStore.EventPlanTab.PLAN,
                    onClick = { component.onTabSelect(mvi.eventplanner.EventPlannerStore.EventPlanTab.PLAN) },
                    text = { Text("План") }
                )
                Tab(
                    selected = selectedTab == mvi.eventplanner.EventPlannerStore.EventPlanTab.RAW_JSON,
                    onClick = { component.onTabSelect(mvi.eventplanner.EventPlannerStore.EventPlanTab.RAW_JSON) },
                    text = { Text("Content JSON") }
                )
                Tab(
                    selected = selectedTab == mvi.eventplanner.EventPlannerStore.EventPlanTab.FULL_RESPONSE,
                    onClick = { component.onTabSelect(mvi.eventplanner.EventPlannerStore.EventPlanTab.FULL_RESPONSE) },
                    text = { Text("Full Response") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Контент вкладок
        when (selectedTab) {
            mvi.eventplanner.EventPlannerStore.EventPlanTab.CHAT -> {
                // Область сообщений
                MessagesArea(
                    messages = messages,
                    isTyping = isTyping,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Область ввода
                InputArea(
                    input = input,
                    onInputChange = component::onInputChange,
                    onSendClick = component::onSendClick,
                    onClearClick = component::onClearClick
                )
            }
            mvi.eventplanner.EventPlannerStore.EventPlanTab.PLAN -> {
                eventPlan?.let { plan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        FormattedEventPlanView(plan.eventPlan)
                    }
                }
            }
            mvi.eventplanner.EventPlannerStore.EventPlanTab.RAW_JSON -> {
                eventPlan?.let { plan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        JsonView(plan.rawJson)
                    }
                }
            }
            mvi.eventplanner.EventPlannerStore.EventPlanTab.FULL_RESPONSE -> {
                eventPlan?.let { plan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        JsonView(plan.fullResponseJson)
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedEventPlanView(eventPlan: EventPlanResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Название мероприятия
        Text(
            text = eventPlan.eventName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider()

        // Основная информация
        Text(
            text = "📋 Основная информация",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        InfoRow("Количество гостей", "${eventPlan.guestCount} чел.")
        InfoRow("Бюджет", eventPlan.budget)
        InfoRow("Дата", eventPlan.eventDate)
        InfoRow("Продолжительность", eventPlan.eventDuration)

        HorizontalDivider()

        // Предпочтения по меню
        Text(
            text = "🍽️ Меню",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        eventPlan.menuPreferences.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider()

        // Напитки
        Text(
            text = "🥂 Напитки",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        eventPlan.drinkPreferences.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Особые пожелания (если есть)
        if (eventPlan.specialRequests.isNotEmpty()) {
            HorizontalDivider()
            Text(
                text = "✨ Особые пожелания",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            eventPlan.specialRequests.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        HorizontalDivider()

        // Рекомендации
        Text(
            text = "💡 Рекомендации менеджера",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        eventPlan.recommendations.forEach { recommendation ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        HorizontalDivider()

        // Итоговая стоимость
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💰 Примерная стоимость",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = eventPlan.totalEstimate,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
