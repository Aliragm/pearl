package gg.pearl.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppBar
import gg.pearl.mobile.components.EmptyState
import gg.pearl.mobile.components.ProfileDescription
import gg.pearl.mobile.components.ProfileReviewCard
import gg.pearl.mobile.components.ProfileTop
import gg.pearl.mobile.model.MockReviews
import gg.pearl.mobile.model.MockUsers
import gg.pearl.mobile.model.ReviewUiModel
import gg.pearl.mobile.model.UserProfileUiModel
import gg.pearl.mobile.theme.PearlTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Profile(
    user: UserProfileUiModel = MockUsers.values.first(),
    reviews: List<ReviewUiModel> = MockReviews,
    onBackClick: (() -> Unit)? = null,
    onGameClick: (String) -> Unit = {},
    isExpandedWidth: Boolean = false,
) {
    AppBar(
        title = "Perfil",
        onBackClick = onBackClick,
    ) { innerPadding ->
        if (isExpandedWidth) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(
                    modifier = Modifier.weight(0.4f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ProfileTop(userName = user.name)
                    ProfileDescription(description = user.description)
                }

                LazyColumn(
                    modifier = Modifier.weight(0.6f),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        ProfileReviewsHeader()
                    }
                    if (reviews.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Nenhuma avaliação publicada",
                                message = "${user.name} ainda não publicou avaliações de jogos."
                            )
                        }
                    } else {
                        items(reviews, key = { it.id }) { review ->
                            ProfileReviewCard(
                                image = review.image,
                                contentDescription = review.contentDescription,
                                gameName = review.gameName,
                                review = review.review,
                                date = review.date,
                                stars = review.stars,
                                onClick = { onGameClick(review.gameId) }
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
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item { ProfileTop(userName = user.name) }
                item { ProfileDescription(description = user.description) }
                item { ProfileReviewsHeader() }
                if (reviews.isEmpty()) {
                    item {
                        EmptyState(
                            title = "Nenhuma avaliação publicada",
                            message = "${user.name} ainda não publicou avaliações de jogos."
                        )
                    }
                } else {
                    items(reviews, key = { it.id }) { review ->
                        ProfileReviewCard(
                            image = review.image,
                            contentDescription = review.contentDescription,
                            gameName = review.gameName,
                            review = review.review,
                            date = review.date,
                            stars = review.stars,
                            onClick = { onGameClick(review.gameId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileReviewsHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) { heading() }
    ) {
        Text(
            text = "Últimas avaliações",
            style = MaterialTheme.typography.headlineMedium,
        )
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(28.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.onSurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    PearlTheme {
        Profile(
            user = MockUsers.values.first(),
            reviews = MockReviews,
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun ProfileScreenEmptyDarkPreview() {
    PearlTheme(darkTheme = true) {
        Profile(
            user = MockUsers.values.first(),
            reviews = emptyList(),
            onBackClick = {}
        )
    }
}

