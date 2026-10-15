package gg.pearl.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.mobile.generated.resources.Res
import gg.pearl.mobile.mobile.generated.resources.gtavi
import org.jetbrains.compose.resources.DrawableResource

private data class TopTenGame(
    val id: Int,
    val image: DrawableResource,
    val contentDescription: String,
    val name: String,
    val developer: String,
)

@Composable
fun TopTenMonth() {
    // TODO: trocar pelos dados reais (top 10 do mês)
    val games = remember {
        List(10) { index ->
            TopTenGame(
                id = index,
                image = Res.drawable.gtavi,
                contentDescription = "Capa de Grand Theft Auto VI",
                name = "Grand Theft Auto VI",
                developer = "Rockstar Games"
            )
        }
    }

    Column {
        Text(
            text = "Top 10 mais avaliados do mês",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(games, key = { it.id }) { game ->
                SearchCard(
                    image = game.image,
                    contentDescription = game.contentDescription,
                    name = game.name,
                    developer = game.developer
                )
            }
        }
    }
}
