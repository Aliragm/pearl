package gg.pearl.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import gg.pearl.mobile.model.MockGames
import gg.pearl.mobile.model.MockPlatforms
import gg.pearl.mobile.model.MockReviews
import gg.pearl.mobile.model.MockUsers
import gg.pearl.mobile.model.ReviewUiModel
import gg.pearl.mobile.model.UserProfileUiModel
import gg.pearl.mobile.screens.CreateReviewScreen
import gg.pearl.mobile.screens.GameDetailScreen
import gg.pearl.mobile.screens.Menu
import gg.pearl.mobile.screens.Profile
import gg.pearl.mobile.screens.SearchScreen
import kotlinx.serialization.Serializable

// Rotas tipadas @Serializable (T4)
@Serializable
data object MenuRoute

@Serializable
data object SearchRoute

@Serializable
data class ProfileRoute(val userId: String = "user-sensato")

@Serializable
data class GameDetailRoute(val gameId: String)

@Serializable
data class CreateReviewRoute(val gameId: String)

enum class TopLevelDestination(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    MENU(MenuRoute, "Menu", Icons.Filled.Home, "Menu principal"),
    SEARCH(SearchRoute, "Pesquisa", Icons.Filled.Search, "Pesquisar jogos"),
    PROFILE(ProfileRoute("user-sensato"), "Perfil", Icons.Filled.Person, "Meu perfil")
}

fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    if (this == null) return false
    return hierarchy.any { navDest ->
        when (destination) {
            TopLevelDestination.MENU -> navDest.hasRoute<MenuRoute>()
            TopLevelDestination.SEARCH -> navDest.hasRoute<SearchRoute>()
            TopLevelDestination.PROFILE -> navDest.hasRoute<ProfileRoute>()
        }
    }
}

fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    isExpandedWidth: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Estado em memória elevado no grafo para persistir novas avaliações durante a sessão (T2 e T4)
    val games = remember { MockGames }
    val reviews = remember { mutableStateListOf<ReviewUiModel>().apply { addAll(MockReviews) } }

    NavHost(
        navController = navController,
        startDestination = MenuRoute,
        modifier = modifier
    ) {
        composable<MenuRoute> {
            Menu(
                games = games,
                reviews = reviews,
                isExpandedWidth = isExpandedWidth,
                onNavigateToProfile = { userId ->
                    navController.navigate(ProfileRoute(userId = userId))
                },
                onGameClick = { gameId ->
                    navController.navigate(GameDetailRoute(gameId = gameId))
                },
                onWriteReviewClick = { gameId ->
                    navController.navigate(CreateReviewRoute(gameId = gameId))
                }
            )
        }

        composable<SearchRoute> {
            SearchScreen(
                games = games,
                platforms = MockPlatforms,
                isExpandedWidth = isExpandedWidth,
                onGameClick = { gameId ->
                    navController.navigate(GameDetailRoute(gameId = gameId))
                },
                onWriteReviewClick = { gameId ->
                    navController.navigate(CreateReviewRoute(gameId = gameId))
                }
            )
        }

        composable<ProfileRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProfileRoute>()
            val user = MockUsers[route.userId] ?: UserProfileUiModel(
                id = route.userId,
                name = route.userId.replace("-", " ").replaceFirstChar { it.uppercase() },
                description = "Jogador e avaliador da comunidade Pearl.gg."
            )
            val userReviews = reviews.filter {
                it.userId == route.userId || it.userName.equals(user.name, ignoreCase = true)
            }
            val canPop = navController.previousBackStackEntry != null

            Profile(
                user = user,
                reviews = userReviews,
                isExpandedWidth = isExpandedWidth,
                onBackClick = if (canPop) {
                    { navController.popBackStack() }
                } else {
                    { navController.navigateToTab(TopLevelDestination.MENU) }
                },
                onGameClick = { gameId ->
                    navController.navigate(GameDetailRoute(gameId = gameId))
                }
            )
        }

        // Destino com argumento tipado e Deep Link declarado (T4 e T5)
        composable<GameDetailRoute>(
            deepLinks = listOf(
                navDeepLink<GameDetailRoute>(basePath = "pearl://game"),
                navDeepLink<GameDetailRoute>(basePath = "https://pearl.gg/game")
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<GameDetailRoute>()
            val game = games.find { it.id == route.gameId } ?: games.first()
            val gameReviews = reviews.filter { it.gameId == game.id }

            GameDetailScreen(
                game = game,
                reviews = gameReviews,
                isExpandedWidth = isExpandedWidth,
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(MenuRoute)
                    }
                },
                onWriteReviewClick = {
                    navController.navigate(CreateReviewRoute(gameId = game.id))
                },
                onUserClick = { userId ->
                    navController.navigate(ProfileRoute(userId = userId))
                }
            )
        }

        composable<CreateReviewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CreateReviewRoute>()
            val game = games.find { it.id == route.gameId } ?: games.first()

            CreateReviewScreen(
                game = game,
                onBackClick = { navController.popBackStack() },
                onSubmitReview = { userName, _, title, content, stars, hasSpoilers ->
                    val newReview = ReviewUiModel(
                        id = "rev-${reviews.size + 1}",
                        gameId = game.id,
                        gameName = game.name,
                        userId = "user-sensato",
                        userName = userName,
                        title = title,
                        review = content,
                        stars = stars,
                        date = "Agora mesmo",
                        hasSpoilers = hasSpoilers,
                        image = game.image,
                        contentDescription = game.contentDescription
                    )
                    reviews.add(0, newReview)
                    navController.popBackStack()
                }
            )
        }
    }
}
