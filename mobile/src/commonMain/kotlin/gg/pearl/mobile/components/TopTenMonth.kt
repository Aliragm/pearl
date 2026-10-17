package gg.pearl.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.model.GameUiModel
import gg.pearl.mobile.model.MockGames

@Composable
fun TopTenMonth(
    games: List<GameUiModel> = MockGames,
    onGameClick: (String) -> Unit = {},
) {
    Column {
        Text(
            text = "Top 10 mais avaliados do mês",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .semantics { heading() }
        )
        if (games.isEmpty()) {
            EmptyState(
                title = "Ranking em atualização",
                message = "Ainda não há jogos suficientes avaliados neste mês."
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(games, key = { index, game -> "${game.id}-$index" }) { _, game ->
                    SearchCard(
                        image = game.image,
                        contentDescription = game.contentDescription,
                        name = game.name,
                        developer = game.developer,
                        onClick = { onGameClick(game.id) }
                    )
                }
            }
        }
    }
}
