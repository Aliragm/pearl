package gg.pearl.mobile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfileReviewCard(
    image: DrawableResource,
    contentDescription: String,
    gameName: String,
    review: String,
    date: String,
    modifier: Modifier = Modifier,
    stars: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(
                role = Role.Button,
                onClickLabel = "Ver detalhes de $gameName",
                onClick = onClick,
            )
    } else {
        modifier.fillMaxWidth()
    }

    Row(
        modifier = clickModifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
                .clip(MaterialTheme.shapes.large),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .height(120.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (stars != null) {
                    StarRatingBar(
                        stars = stars,
                        starSize = 18.dp,
                    )
                }
                Text(
                    text = "“$review”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
