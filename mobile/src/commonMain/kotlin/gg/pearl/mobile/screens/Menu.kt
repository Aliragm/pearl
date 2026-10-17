package gg.pearl.mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppBar
import gg.pearl.mobile.components.EmptyState
import gg.pearl.mobile.components.LastReviews
import gg.pearl.mobile.components.TopTenMonth
import gg.pearl.mobile.components.TrendingGames
import gg.pearl.mobile.model.GameUiModel
import gg.pearl.mobile.model.MockGames
import gg.pearl.mobile.model.MockReviews
import gg.pearl.mobile.model.ReviewUiModel
import gg.pearl.mobile.theme.PearlTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Menu(
    games: List<GameUiModel> = MockGames,
    reviews: List<ReviewUiModel> = MockReviews,
    onNavigateToProfile: (String) -> Unit = {},
    onGameClick: (String) -> Unit = {},
    onWriteReviewClick: (String) -> Unit = {},
    isExpandedWidth: Boolean = false,
) {
    AppBar(
        title = "Para você",
        actions = {
            IconButton(onClick = { onNavigateToProfile("user-sensato") }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Meu perfil")
            }
        },
        floatingActionButton = {
            if (games.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onWriteReviewClick(games.first().id) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.RateReview,
                        contentDescription = "Nova avaliação"
                    )
                }
            }
        }
    ) { innerPadding ->
        if (games.isEmpty() && reviews.isEmpty()) {
            EmptyState(
                title = "Bem-vindo ao Pearl.gg",
                message = "Ainda não há jogos ou avaliações carregados no feed.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        } else if (isExpandedWidth) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(0.55f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TrendingGames(
                            games = games,
                            onGameClick = onGameClick
                        )
                    }
                    item {
                        TopTenMonth(
                            games = games,
                            onGameClick = onGameClick
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(0.45f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        LastReviews(
                            reviews = reviews,
                            onNavigateToProfile = onNavigateToProfile,
                            onGameClick = onGameClick
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TrendingGames(
                        games = games,
                        onGameClick = onGameClick
                    )
                }
                item {
                    LastReviews(
                        reviews = reviews,
                        onNavigateToProfile = onNavigateToProfile,
                        onGameClick = onGameClick
                    )
                }
                item {
                    TopTenMonth(
                        games = games,
                        onGameClick = onGameClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MenuScreenPreview() {
    PearlTheme {
        Menu(
            games = MockGames,
            reviews = MockReviews,
        )
    }
}

@Preview
@Composable
private fun MenuScreenEmptyPreview() {
    PearlTheme(darkTheme = true) {
        Menu(
            games = emptyList(),
            reviews = emptyList(),
        )
    }
}