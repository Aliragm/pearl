package gg.pearl.mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppSearchBar
import gg.pearl.mobile.components.EmptyState
import gg.pearl.mobile.components.FilterBar
import gg.pearl.mobile.components.SearchCard
import gg.pearl.mobile.components.StarRatingBar
import gg.pearl.mobile.model.GameUiModel
import gg.pearl.mobile.model.MockGames
import gg.pearl.mobile.model.MockPlatforms
import gg.pearl.mobile.theme.PearlTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

private val SearchBarSpace: Dp = 80.dp
private val FilterBarSpace: Dp = 56.dp

@Composable
fun SearchScreen(
    games: List<GameUiModel> = MockGames,
    platforms: List<String> = MockPlatforms,
    onGameClick: (String) -> Unit = {},
    onWriteReviewClick: (String) -> Unit = {},
    isExpandedWidth: Boolean = false,
    initialSelectedPlatform: String? = null,
) {
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()

    var selectedPlatform by rememberSaveable { mutableStateOf(initialSelectedPlatform) }
    var selectedPreviewGameId by rememberSaveable { mutableStateOf(games.firstOrNull()?.id) }

    val filteredGames = remember(games, query, selectedPlatform) {
        games
            .filter { query.isBlank() || it.matches(query) }
            .filter { selectedPlatform == null || it.platform.equals(selectedPlatform, ignoreCase = true) || it.platforms.any { p -> p.equals(selectedPlatform, ignoreCase = true) } }
    }

    val suggestions = remember(games, query) {
        if (query.isBlank()) emptyList()
        else games.filter { it.matches(query) }.map { it.name }.distinct()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (filteredGames.isEmpty()) {
            EmptyState(
                title = "Nenhum jogo encontrado",
                message = "Não encontramos resultados para os filtros selecionados. Tente limpar o filtro de plataforma ou buscar outro termo.",
                icon = Icons.Outlined.SearchOff,
                actionLabel = if (selectedPlatform != null || query.isNotBlank()) "Limpar filtros" else null,
                onActionClick = {
                    selectedPlatform = null
                    textFieldState.edit { replace(0, length, "") }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = SearchBarSpace + FilterBarSpace)
            )
        } else if (isExpandedWidth) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = SearchBarSpace + FilterBarSpace,
                        bottom = 16.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.weight(0.6f).fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredGames, key = { it.id }) { game ->
                        SearchCard(
                            image = game.image,
                            contentDescription = game.contentDescription,
                            name = game.name,
                            developer = game.developer,
                            width = Dp.Unspecified,
                            onClick = { selectedPreviewGameId = game.id }
                        )
                    }
                }

                val activeGame = filteredGames.find { it.id == selectedPreviewGameId } ?: filteredGames.first()
                Card(
                    modifier = Modifier.weight(0.4f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = activeGame.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = activeGame.developer,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StarRatingBar(
                            stars = activeGame.meanScore.roundToInt(),
                            starSize = 20.dp
                        )
                        Text(
                            text = "Plataformas: ${activeGame.platforms.joinToString(", ")}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = activeGame.synopsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onGameClick(activeGame.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ver página")
                            }
                            Button(
                                onClick = { onWriteReviewClick(activeGame.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Avaliar")
                            }
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = SearchBarSpace + FilterBarSpace,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredGames, key = { it.id }) { game ->
                    SearchCard(
                        image = game.image,
                        contentDescription = game.contentDescription,
                        name = game.name,
                        developer = game.developer,
                        width = Dp.Unspecified,
                        onClick = { onGameClick(game.id) }
                    )
                }
            }
        }

        FilterBar(
            platforms = platforms,
            selectedPlatform = selectedPlatform,
            onPlatformSelected = { selectedPlatform = it },
            topInset = SearchBarSpace,
        )

        AppSearchBar(
            textFieldState = textFieldState,
            onSearch = { /* o grid já filtra enquanto digita */ },
            searchResults = suggestions,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        )
    }
}

@Preview
@Composable
private fun SearchScreenPreview() {
    PearlTheme {
        SearchScreen(
            games = MockGames,
            platforms = MockPlatforms
        )
    }
}

@Preview
@Composable
private fun SearchScreenEmptyPreview() {
    PearlTheme(darkTheme = true) {
        SearchScreen(
            games = emptyList(),
            platforms = MockPlatforms
        )
    }
}