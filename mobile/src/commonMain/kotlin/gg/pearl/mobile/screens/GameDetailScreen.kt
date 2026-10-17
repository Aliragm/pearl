package gg.pearl.mobile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppBar
import gg.pearl.mobile.components.EmptyState
import gg.pearl.mobile.components.ReviewCard
import gg.pearl.mobile.components.StarRatingBar
import gg.pearl.mobile.model.GameUiModel
import gg.pearl.mobile.model.MockGames
import gg.pearl.mobile.model.MockReviews
import gg.pearl.mobile.model.ReviewUiModel
import gg.pearl.mobile.theme.PearlTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
fun GameDetailScreen(
    game: GameUiModel,
    reviews: List<ReviewUiModel>,
    onBackClick: (() -> Unit)? = null,
    onWriteReviewClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    isExpandedWidth: Boolean = false,
) {
    AppBar(
        title = game.name,
        onBackClick = onBackClick,
    ) { innerPadding ->
        if (isExpandedWidth) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(0.45f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameHeaderSection(
                        game = game,
                        onWriteReviewClick = onWriteReviewClick
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(0.55f),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Avaliações da comunidade (${reviews.size})",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    if (reviews.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Sem avaliações ainda",
                                message = "Ninguém avaliou ${game.name} até o momento. Que tal inaugurar as análises?",
                                actionLabel = "Avaliar",
                                onActionClick = onWriteReviewClick
                            )
                        }
                    } else {
                        items(reviews, key = { it.id }) { review ->
                            ReviewCard(
                                onNavigateToProfile = { onUserClick(review.userId) },
                                userName = review.userName,
                                image = review.image,
                                contentDescription = review.contentDescription,
                                gameName = review.gameName,
                                title = review.title,
                                review = review.review,
                                stars = review.stars,
                                date = review.date
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    GameHeaderSection(
                        game = game,
                        onWriteReviewClick = onWriteReviewClick
                    )
                }
                item {
                    Text(
                        text = "Avaliações da comunidade (${reviews.size})",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                if (reviews.isEmpty()) {
                    item {
                        EmptyState(
                            title = "Sem avaliações ainda",
                            message = "Ninguém avaliou ${game.name} até o momento. Que tal inaugurar as análises?",
                            actionLabel = "Avaliar",
                            onActionClick = onWriteReviewClick
                        )
                    }
                } else {
                    items(reviews, key = { it.id }) { review ->
                        ReviewCard(
                            onNavigateToProfile = { onUserClick(review.userId) },
                            userName = review.userName,
                            image = review.image,
                            contentDescription = review.contentDescription,
                            gameName = review.gameName,
                            title = review.title,
                            review = review.review,
                            stars = review.stars,
                            date = review.date
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameHeaderSection(
    game: GameUiModel,
    onWriteReviewClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            painter = painterResource(game.image),
            contentDescription = game.contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(MaterialTheme.shapes.extraLarge)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.developer,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StarRatingBar(
                    stars = game.meanScore.roundToInt(),
                    starSize = 24.dp
                )
            }

            FilledTonalButton(onClick = onWriteReviewClick) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Avaliar")
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            game.platforms.forEach { platform ->
                AssistChip(
                    onClick = {},
                    label = { Text(platform) }
                )
            }
        }

        Text(
            text = "Sinopse",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )

        Text(
            text = game.synopsis,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun GameDetailScreenPreview() {
    PearlTheme {
        GameDetailScreen(
            game = MockGames.first(),
            reviews = MockReviews,
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun GameDetailScreenEmptyPreview() {
    PearlTheme(darkTheme = true) {
        GameDetailScreen(
            game = MockGames.first(),
            reviews = emptyList(),
            onBackClick = {},
            isExpandedWidth = true
        )
    }
}
