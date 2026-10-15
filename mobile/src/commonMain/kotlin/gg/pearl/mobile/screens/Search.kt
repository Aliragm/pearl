package gg.pearl.mobile.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import gg.pearl.mobile.components.AppSearchBar

// TODO: trocar pela busca real (backend)
private val allGames = listOf(
    "Grand Theft Auto VI",
    "EA FC 27",
    "007 First Light",
    "Minecraft",
    "Persona 5 Royal",
    "Clair Obscur: Expedition 33",
    "The Legend of Zelda: Ocarina of Time",
)

@Composable
fun SearchScreen() {
    val textFieldState = rememberTextFieldState()
    var submittedQuery by rememberSaveable { mutableStateOf("") }

    val query = textFieldState.text.toString()
    val results = remember(query) {
        if (query.isBlank()) emptyList()
        else allGames.filter { it.contains(query, ignoreCase = true) }
    }

    AppSearchBar(
        textFieldState = textFieldState,
        onSearch = { submittedQuery = it },
        searchResults = results,
        modifier = Modifier.fillMaxSize()
    )
}