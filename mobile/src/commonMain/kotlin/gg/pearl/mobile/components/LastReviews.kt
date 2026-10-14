package gg.pearl.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.mobile.generated.resources.Res
import gg.pearl.mobile.mobile.generated.resources.gtavi
import org.jetbrains.compose.resources.DrawableResource

private data class ReviewItem(
    val id: Int,
    val userName: String,
    val image: DrawableResource,
    val contentDescription: String,
    val gameName: String,
    val review: String,
    val stars: Int,
    val date: String,
)

@Composable
fun LastReviews(
    onNavigateToProfile: () -> Unit = {},
) {
    // TODO: trocar pelos dados reais
    val items = remember {
        listOf(
            ReviewItem(
                id = 1,
                userName = "User sensato",
                image = Res.drawable.gtavi,
                contentDescription = "Mulher sentada num carro abraçando homem por trás",
                gameName = "Grand Theft Auto VI",
                review = "Mais do mesmo",
                stars = 3,
                date = "Hoje • 15:50"
            ),
            ReviewItem(
                id = 2,
                userName = "User sagaz",
                image = Res.drawable.gtavi,
                contentDescription = "Mulher sentada num carro abraçando homem por trás",
                gameName = "Grand Theft Auto VI",
                review = "Um refresco aos jogos de história single player",
                stars = 5,
                date = "Ontem • 16:32"
            ),
        )
    }

    Column {
        Text(
            text = "Avaliações recentes",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items.forEach { item ->
                ReviewCard(
                    onNavigateToProfile = onNavigateToProfile,
                    userName = item.userName,
                    image = item.image,
                    contentDescription = item.contentDescription,
                    gameName = item.gameName,
                    review = item.review,
                    stars = item.stars,
                    date = item.date
                )
            }
        }
    }
}