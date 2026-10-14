package gg.pearl.mobile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private const val MAX_STARS = 5

@Composable
fun ReviewCard(
    onNavigateToProfile: () -> Unit,
    userName: String,
    image: DrawableResource,
    contentDescription: String,
    gameName: String,
    review: String,
    stars: Int,
    date: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(120.dp)
                .height(134.dp)
                .clip(MaterialTheme.shapes.large)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = gameName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil de $userName")
                }
                Spacer(Modifier.width(8.dp))
                Text(userName, style = MaterialTheme.typography.bodyMedium)
            }

            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                repeat(MAX_STARS) { index ->
                    val filled = index < stars
                    Icon(
                        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (filled) "Estrela preenchida" else "Estrela vazia",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = "“$review”",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}