package gg.pearl.mobile.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterCard(
    platform: String,
    isSelected: Boolean,
    updateFilter: () -> Unit,
    modifier: Modifier = Modifier,
){
    FilterChip(
        selected = isSelected,
        onClick = updateFilter,
        label = { Text(text = platform) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else null,
        modifier = modifier,
    )
}