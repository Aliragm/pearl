package gg.pearl.mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppSearchBar
import gg.pearl.mobile.components.SearchCard
import gg.pearl.mobile.mobile.generated.resources.Res
import gg.pearl.mobile.mobile.generated.resources.gtavi
import org.jetbrains.compose.resources.DrawableResource

private data class SearchCardData(
    val image: DrawableResource,
    val contentDescription: String,
    val name: String,
    val developer: String,
    val platform: String
) {
    fun matches(query: String): Boolean =
        name.contains(query, ignoreCase = true) ||
            developer.contains(query, ignoreCase = true) ||
            platform.contains(query, ignoreCase = true)
}

// TODO: trocar pela busca real (backend)
private val allGames = listOf(
    SearchCardData(Res.drawable.gtavi, "Capa de Grand Theft Auto VI", "Grand Theft Auto VI", "Rockstar Games", platform = "Playstation"),
    SearchCardData(Res.drawable.gtavi, "Capa de Grand Theft Auto VI", "Grand Theft Auto VI", "Rockstar Games", platform = "Xbox"),
    SearchCardData(Res.drawable.gtavi, "Capa de Grand Theft Auto VI", "Grand Theft Auto VI", "Rockstar Games", platform = "Nintendo"),
    SearchCardData(Res.drawable.gtavi, "Capa de Grand Theft Auto Seis", "Grand Theft Auto Seis", "Rockstar Games", platform = "PC"),
)

private val SearchBarSpace: Dp = 80.dp

@Composable
fun SearchScreen() {
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()

    val filteredGames = remember(query) {
        if (query.isBlank()) allGames else allGames.filter { it.matches(query) }
    }

    val suggestions = remember(query) {
        if (query.isBlank()) emptyList()
        else allGames.filter { it.matches(query) }.map { it.name }.distinct()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = SearchBarSpace,
                bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredGames) { game ->
                SearchCard(
                    image = game.image,
                    contentDescription = game.contentDescription,
                    name = game.name,
                    developer = game.developer,
                    width = Dp.Unspecified
                )
            }
        }

        AppSearchBar(
            textFieldState = textFieldState,
            onSearch = { /* o grid já filtra enquanto digita */ },
            searchResults = suggestions,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        )
    }
}