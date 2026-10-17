package gg.pearl.mobile

import androidx.compose.runtime.Composable
import gg.pearl.mobile.components.AdaptiveNavigationShell
import gg.pearl.mobile.components.AppNavigationBar
import gg.pearl.mobile.theme.PearlTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    PearlTheme {
        AppNavigationBar()
    }
}

@Composable
@Preview
private fun AppDarkExpandedPreview() {
    PearlTheme(darkTheme = true) {
        AdaptiveNavigationShell(isExpandedWidth = true)
    }
}
