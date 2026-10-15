package gg.pearl.mobile.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppBar
import gg.pearl.mobile.components.LastReviews
import gg.pearl.mobile.components.TopTenMonth
import gg.pearl.mobile.components.TrendingGames

@Composable
fun Menu(
    onNavigateToProfile: () -> Unit
) {
    AppBar(
        title = "Para você",
        actions = {
            IconButton(onClick = onNavigateToProfile) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                TrendingGames()
            }
            item {
                LastReviews(onNavigateToProfile = onNavigateToProfile)
            }
            item {
                TopTenMonth()
            }
        }
    }
}