package gg.pearl.mobile.model

import gg.pearl.mobile.mobile.generated.resources.Res
import gg.pearl.mobile.mobile.generated.resources.gtavi
import org.jetbrains.compose.resources.DrawableResource

data class GameUiModel(
    val id: String,
    val name: String,
    val developer: String,
    val platform: String,
    val platforms: List<String> = listOf(platform),
    val meanScore: Double = 4.6,
    val synopsis: String = "Explore Vice City e arredores na maior e mais imersiva evolução da série Grand Theft Auto.",
    val image: DrawableResource = Res.drawable.gtavi,
    val contentDescription: String = "Capa de $name",
) {
    fun matches(query: String): Boolean =
        name.contains(query, ignoreCase = true) ||
            developer.contains(query, ignoreCase = true) ||
            platform.contains(query, ignoreCase = true) ||
            platforms.any { it.contains(query, ignoreCase = true) }
}

data class ReviewUiModel(
    val id: String,
    val gameId: String,
    val gameName: String,
    val userId: String,
    val userName: String,
    val title: String,
    val review: String,
    val stars: Int,
    val date: String,
    val hasSpoilers: Boolean = false,
    val image: DrawableResource = Res.drawable.gtavi,
    val contentDescription: String = "Capa de $gameName",
)

data class UserProfileUiModel(
    val id: String,
    val name: String,
    val description: String,
)

// TODO: trocar pelos dados reais (integração com backend na Sprint 2)
val MockPlatforms = listOf(
    "Playstation",
    "Xbox",
    "PC",
    "Nintendo",
)

val MockGames = listOf(
    GameUiModel(
        id = "gta-vi",
        name = "Grand Theft Auto VI",
        developer = "Rockstar Games",
        platform = "Playstation",
        platforms = listOf("Playstation", "Xbox"),
        meanScore = 4.8,
        synopsis = "Grand Theft Auto VI segue para o estado de Leonida, lar das ruas iluminadas por neon de Vice City e além.",
        image = Res.drawable.gtavi,
        contentDescription = "Capa de Grand Theft Auto VI",
    ),
    GameUiModel(
        id = "gta-vi-xbox",
        name = "Grand Theft Auto VI (Xbox Edition)",
        developer = "Rockstar Games",
        platform = "Xbox",
        platforms = listOf("Xbox", "Playstation"),
        meanScore = 4.7,
        synopsis = "Edição para consoles de nova geração com mundo aberto dinâmico e narrativa cinematográfica em Leonida.",
        image = Res.drawable.gtavi,
        contentDescription = "Capa de Grand Theft Auto VI no Xbox",
    ),
    GameUiModel(
        id = "gta-vi-nintendo",
        name = "Grand Theft Auto Stories",
        developer = "Rockstar North",
        platform = "Nintendo",
        platforms = listOf("Nintendo"),
        meanScore = 4.3,
        synopsis = "Aventura portátil ambientada no universo clássico de Vice City com missões inéditas.",
        image = Res.drawable.gtavi,
        contentDescription = "Capa de Grand Theft Auto Stories",
    ),
    GameUiModel(
        id = "gta-seis-pc",
        name = "Grand Theft Auto Seis",
        developer = "Rockstar Games",
        platform = "PC",
        platforms = listOf("PC"),
        meanScore = 4.9,
        synopsis = "Versão definitiva para PC com suporte a altas taxas de quadros, ray tracing completo e ferramentas da comunidade.",
        image = Res.drawable.gtavi,
        contentDescription = "Capa de Grand Theft Auto Seis para PC",
    ),
)

val MockUsers = mapOf(
    "user-sensato" to UserProfileUiModel(
        id = "user-sensato",
        name = "User sensato",
        description = "Entusiasta de RPGs e jogos de mundo aberto. Avalio jogos focando em narrativa, direção de arte e design de missões.\n\nSempre em busca de boas histórias single-player e trilhas sonoras marcantes.",
    ),
    "user-sagaz" to UserProfileUiModel(
        id = "user-sagaz",
        name = "User sagaz",
        description = "Caçador de conquistas e fã de jogos de ação, corrida e simulação. Analiso desempenho técnico, gameplay e fator replay.",
    ),
)

val MockReviews = listOf(
    ReviewUiModel(
        id = "rev-1",
        gameId = "gta-vi",
        gameName = "Grand Theft Auto VI",
        userId = "user-sensato",
        userName = "User sensato",
        title = "Ambientação impecável, mas fórmula conhecida",
        review = "Mais do mesmo na estrutura de missões, porém a riqueza de detalhes do mundo aberto impressiona.",
        stars = 3,
        date = "Hoje • 15:50",
        image = Res.drawable.gtavi,
        contentDescription = "Capa de Grand Theft Auto VI",
    ),
    ReviewUiModel(
        id = "rev-2",
        gameId = "gta-vi",
        gameName = "Grand Theft Auto VI",
        userId = "user-sagaz",
        userName = "User sagaz",
        title = "O novo padrão para jogos de mundo aberto",
        review = "Um refresco aos jogos de história single player com personagens carismáticos e física excelente.",
        stars = 5,
        date = "Ontem • 16:32",
        image = Res.drawable.gtavi,
        contentDescription = "Capa de Grand Theft Auto VI",
    ),
)
