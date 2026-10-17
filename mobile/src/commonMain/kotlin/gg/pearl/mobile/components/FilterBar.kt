package gg.pearl.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FilterBar(
    platforms: List<String>,
    selectedPlatform: String?,
    onPlatformSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    topInset: Dp = 0.dp,
    bottomPadding: Dp = 8.dp,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = topInset, bottom = bottomPadding),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterCard(
                platform = "Todos",
                isSelected = selectedPlatform == null,
                updateFilter = { onPlatformSelected(null) },
            )
        }
        items(platforms) { platform ->
            FilterCard(
                platform = platform,
                isSelected = selectedPlatform == platform,
                updateFilter = {
                    onPlatformSelected(if (selectedPlatform == platform) null else platform)
                },
            )
        }
    }
}
