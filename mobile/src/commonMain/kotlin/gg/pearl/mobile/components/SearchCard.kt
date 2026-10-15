package gg.pearl.mobile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Card de jogo (capa + nome + desenvolvedora).
 * Reutilizável no "Top 10 do mês" e nos resultados da pesquisa.
 *
 * @param width largura do card; passe [Dp.Unspecified] para ele ocupar a largura disponível
 * (útil numa lista vertical de pesquisa).
 */
@Composable
fun SearchCard(
    image: DrawableResource,
    contentDescription: String,
    name: String,
    developer: String,
    modifier: Modifier = Modifier,
    width: Dp = 132.dp,
    onClick: (() -> Unit)? = null,
) {
    val sizeModifier = if (width == Dp.Unspecified) modifier.fillMaxWidth() else modifier.width(width)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    val content: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(image),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = developer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = sizeModifier, colors = colors, content = { content() })
    } else {
        Card(modifier = sizeModifier, colors = colors, content = { content() })
    }
}
