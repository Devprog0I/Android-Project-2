package com.example.database

import androidx.compose.foundation.gestures.detectTapGestures
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack  // ИСПРАВЛЕНО
import androidx.compose.material.icons.automirrored.filled.ArrowForward // ИСПРАВЛЕНО
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // ИСПРАВЛЕНО
import com.example.database.ui.theme.DatabaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DatabaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Инициализация ViewModel
                        val viewModel: GameViewModel = viewModel()
                        GameApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun GameApp(viewModel: GameViewModel) {
    // Подписка на состояния
    val currentIndex by viewModel.currentIndex.collectAsState()
    val showDetails by viewModel.showDetails.collectAsState()
    val currentGame = viewModel.currentGame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Коллекция видеоигр",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Карточка с игрой
        GameCard(
            game = currentGame,
            showDetails = showDetails,
            onLongPress = { viewModel.toggleDetails() },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Навигационные кнопки
        NavigationButtons(
            onPrevious = {
                viewModel.previousItem()
                viewModel.resetShowDetails()
            },
            onNext = {
                viewModel.nextItem()
                viewModel.resetShowDetails()
            },
            isFirstItem = viewModel.isFirstItem,
            isLastItem = viewModel.isLastItem
        )

        // Индикатор
        Text(
            text = "Элемент ${currentIndex + 1} из ${viewModel.collectionSize}",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun GameCard(
    game: GameItem,
    showDetails: Boolean,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) {  // ИСПРАВЛЕНО: combinedClickable -> pointerInput
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Изображение (заглушка)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getGameEmoji(game.genre),
                    fontSize = 80.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Название
            Text(
                text = game.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Информационные чипы
            InfoChipsRow(game = game)

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Детальная информация
            if (showDetails) {
                Text(
                    text = game.detailedInfo,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Text(
                    text = "👆 Нажмите и удерживайте для просмотра детальной информации",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun InfoChipsRow(game: GameItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Первая строка: год и жанр
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoChip(text = "📅 ${game.year}")
            InfoChip(text = "🎮 ${game.genre}")
        }

        // Вторая строка: разработчик и платформа
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoChip(text = "👤 ${game.developer}")
            InfoChip(text = "📱 ${game.platform}")
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun NavigationButtons(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isFirstItem: Boolean,
    isLastItem: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onPrevious,
            enabled = !isFirstItem,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,  // ИСПРАВЛЕНО
                contentDescription = "Предыдущий"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Предыдущий")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onNext,
            enabled = !isLastItem,
            modifier = Modifier.weight(1f)
        ) {
            Text("Следующий")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,  // ИСПРАВЛЕНО
                contentDescription = "Следующий"
            )
        }
    }
}

fun getGameEmoji(genre: String): String {
    return when (genre.lowercase()) {
        "rpg" -> "⚔️"
        "action-adventure" -> "🗺️"
        "sandbox" -> "🏗️"
        "action" -> "💥"
        "platformer" -> "🦸"
        "action rpg" -> "🗡️"
        "roguelike" -> "♾️"
        else -> "🎮"
    }
}

@Preview(showBackground = true)
@Composable
fun GameAppPreview() {
    DatabaseTheme {
        // Создаем тестовые данные для превью
        val previewGame = GameItem(
            title = "The Witcher 3",
            imageUrl = "",
            developer = "CD Projekt Red",
            year = 2015,
            genre = "RPG",
            platform = "PC",
            detailedInfo = "Тестовое описание игры"
        )
        GameCard(
            game = previewGame,
            showDetails = false,
            onLongPress = {},
            modifier = Modifier
        )
    }
}
