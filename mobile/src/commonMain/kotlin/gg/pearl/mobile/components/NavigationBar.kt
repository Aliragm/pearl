package gg.pearl.mobile.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import gg.pearl.mobile.navigation.AppNavHost
import gg.pearl.mobile.navigation.TopLevelDestination
import gg.pearl.mobile.navigation.isTopLevelDestinationInHierarchy
import gg.pearl.mobile.navigation.navigateToTab

@Composable
fun AppNavigationBar(
    navController: NavHostController = rememberNavController(),
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isExpandedWidth = windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    AdaptiveNavigationShell(
        isExpandedWidth = isExpandedWidth,
        navController = navController
    )
}

/**
 * Decisão de layout separada do conteúdo para facilitar testes e previews nas duas larguras (T7).
 */
@Composable
fun AdaptiveNavigationShell(
    isExpandedWidth: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = backStackEntry?.destination

    if (isExpandedWidth) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                header = {
                    Text(
                        text = "Pearl.gg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                    )
                }
            ) {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationRailItem(
                        selected = currentDestination.isTopLevelDestinationInHierarchy(destination),
                        onClick = { navController.navigateToTab(destination) },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
            AppNavHost(
                navController = navController,
                isExpandedWidth = true,
                modifier = Modifier.weight(1f).fillMaxSize()
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination.isTopLevelDestinationInHierarchy(destination),
                            onClick = { navController.navigateToTab(destination) },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.contentDescription
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        ) { contentPadding ->
            AppNavHost(
                navController = navController,
                isExpandedWidth = false,
                modifier = Modifier.padding(contentPadding)
            )
        }
    }
}