package gg.pearl.mobile.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import gg.pearl.mobile.screens.Menu
import gg.pearl.mobile.screens.Profile
import gg.pearl.mobile.screens.SearchScreen

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    MENU("menu", "Menu", Icons.Filled.Home, "Menu"),
    SEARCH("search", "Pesquisa", Icons.Filled.Search, "Pesquisa"),
    PROFILE("profile", "Perfil", Icons.Filled.Person, "Perfil")
}

fun NavHostController.navigateToTab(destination: Destination) {
    navigate(destination.route) {
        graph.findStartDestination().route?.let {
            popUpTo(it) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(Destination.MENU.route) {
            Menu(onNavigateToProfile = { navController.navigateToTab(Destination.PROFILE) })
        }
        composable(Destination.SEARCH.route) {
            SearchScreen()
        }
        composable(Destination.PROFILE.route) {
            Profile(onBackClick = { navController.navigateToTab(Destination.MENU) })
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
