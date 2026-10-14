package gg.pearl.mobile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.mobile.generated.resources.Res
import gg.pearl.mobile.mobile.generated.resources.gtavi
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class CarouselItem(
    val id: Int,
    val image: DrawableResource,
    val contentDescription: String,
    val name: String,
    val developer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingGames(){
    Column {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Jogos em alta", style = MaterialTheme.typography.titleLarge)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = "Tendência de alta"
            )
        }

        val items = remember {
            listOf(
                CarouselItem(0, Res.drawable.gtavi, "jogo 1", "Grand Theft Auto VI", "Rockstar Games"),
                CarouselItem(1, Res.drawable.gtavi, "jogo 2", "Grand Theft Auto VI", "Rockstar Games"),
                CarouselItem(2, Res.drawable.gtavi, "jogo 3", "Grand Theft Auto VI", "Rockstar Games"),
                CarouselItem(3, Res.drawable.gtavi, "jogo 4", "Grand Theft Auto VI", "Rockstar Games"),
                CarouselItem(4, Res.drawable.gtavi, "jogo 5", "Grand Theft Auto VI", "Rockstar Games"),
            )
        }

        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { items.count() },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 8.dp),
            preferredItemWidth = 300.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) { i ->
            val item = items[i]
            Column {
                Image(
                    modifier = Modifier
                        .height(205.dp)
                        .fillMaxWidth()
                        .maskClip(MaterialTheme.shapes.extraLarge),
                    painter = painterResource(item.image),
                    contentDescription = item.contentDescription,
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
                val info = carouselItemInfo
                val focus = if (info.maxSize > info.minSize) {
                    ((info.size - info.minSize) / (info.maxSize - info.minSize)).coerceIn(0f, 1f)
                } else 1f
                val captionAlpha = ((focus - 0.7f) / 0.3f).coerceIn(0f, 1f)
                Column(modifier = Modifier.graphicsLayer { alpha = captionAlpha }) {
                    Text(
                        text = item.name,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.developer,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}