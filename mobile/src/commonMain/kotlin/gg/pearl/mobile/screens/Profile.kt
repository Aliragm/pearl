package gg.pearl.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppBar
import gg.pearl.mobile.components.ProfileDescription
import gg.pearl.mobile.components.ProfileReviewCard
import gg.pearl.mobile.components.ProfileTop
import gg.pearl.mobile.mobile.generated.resources.Res
import gg.pearl.mobile.mobile.generated.resources.gtavi
import org.jetbrains.compose.resources.DrawableResource

private data class ProfileReview(
    val image: DrawableResource,
    val contentDescription: String,
    val gameName: String,
    val review: String,
    val date: String,
)

// TODO: trocar pelos dados reais
private const val MockUserName = "User sensato"
private const val MockDescription =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n\n" +
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

private val mockReviews = listOf(
    ProfileReview(Res.drawable.gtavi, "Capa de Grand Theft Auto VI", "Grand Theft Auto VI", "Mais do mesmo", "Hoje • 23 min"),
    ProfileReview(Res.drawable.gtavi, "Capa de Grand Theft Auto VI", "Grand Theft Auto VI", "Um refresco aos jogos de história single player", "23/09/2026"),
)

@Composable
fun Profile(
    onBackClick: () -> Unit,
) {
    AppBar(
        title = "Perfil",
        onBackClick = onBackClick,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item { ProfileTop(userName = MockUserName) }
            item { ProfileDescription(description = MockDescription) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            items(mockReviews) { review ->
                ProfileReviewCard(
                    image = review.image,
                    contentDescription = review.contentDescription,
                    gameName = review.gameName,
                    review = review.review,
                    date = review.date,
                )
            }
        }
    }
}
