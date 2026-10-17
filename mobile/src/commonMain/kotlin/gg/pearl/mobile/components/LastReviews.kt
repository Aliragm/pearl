package gg.pearl.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.model.MockReviews
import gg.pearl.mobile.model.ReviewUiModel

@Composable
fun LastReviews(
    reviews: List<ReviewUiModel> = MockReviews,
    onNavigateToProfile: (String) -> Unit = {},
    onGameClick: (String) -> Unit = {},
) {
    Column {
        Text(
            text = "Avaliações recentes",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .semantics { heading() }
        )
        if (reviews.isEmpty()) {
            EmptyState(
                title = "Nenhuma avaliação recente",
                message = "Seja o primeiro a publicar uma avaliação de jogo na comunidade!"
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                reviews.forEach { item ->
                    ReviewCard(
                        onNavigateToProfile = { onNavigateToProfile(item.userId) },
                        userName = item.userName,
                        image = item.image,
                        contentDescription = item.contentDescription,
                        gameName = item.gameName,
                        title = item.title,
                        review = item.review,
                        stars = item.stars,
                        date = item.date,
                        onGameClick = { onGameClick(item.gameId) }
                    )
                }
            }
        }
    }
}