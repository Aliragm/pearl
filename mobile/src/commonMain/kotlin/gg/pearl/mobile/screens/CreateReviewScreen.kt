package gg.pearl.mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import gg.pearl.mobile.components.AppBar
import gg.pearl.mobile.model.GameUiModel
import gg.pearl.mobile.model.MockGames
import gg.pearl.mobile.theme.PearlTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

data class ReviewFormValidation(
    val userNameError: String?,
    val emailError: String?,
    val titleError: String?,
    val contentError: String?,
    val starsError: String?,
) {
    val isValid: Boolean
        get() = userNameError == null &&
            emailError == null &&
            titleError == null &&
            contentError == null &&
            starsError == null
}

/**
 * Função pura de validação do formulário de avaliação (T3).
 * Derivada diretamente do estado atual, sem usar remember extra.
 */
fun validateReviewForm(
    userName: String,
    email: String,
    title: String,
    content: String,
    stars: Int,
): ReviewFormValidation {
    val trimmedName = userName.trim()
    val trimmedEmail = email.trim()
    val trimmedTitle = title.trim()
    val trimmedContent = content.trim()

    val userNameError = when {
        trimmedName.isEmpty() -> "Informe seu nome de usuário."
        trimmedName.length < 3 -> "O nome deve ter pelo menos 3 caracteres."
        else -> null
    }

    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    val emailError = when {
        trimmedEmail.isEmpty() -> "Informe seu e-mail."
        !emailRegex.matches(trimmedEmail) -> "Digite um endereço de e-mail válido (ex.: voce@dominio.com)."
        else -> null
    }

    val titleError = when {
        trimmedTitle.isEmpty() -> "Informe um título resumindo sua avaliação."
        trimmedTitle.length < 4 -> "O título deve ter pelo menos 4 caracteres."
        else -> null
    }

    val contentError = when {
        trimmedContent.isEmpty() -> "Escreva sua análise sobre o jogo."
        trimmedContent.length < 15 -> "A análise deve ter pelo menos 15 caracteres (atual: ${trimmedContent.length})."
        else -> null
    }

    val starsError = if (stars !in 1..5) {
        "Selecione uma nota entre 1 e 5 estrelas."
    } else {
        null
    }

    return ReviewFormValidation(
        userNameError = userNameError,
        emailError = emailError,
        titleError = titleError,
        contentError = contentError,
        starsError = starsError,
    )
}

@Composable
fun CreateReviewScreen(
    game: GameUiModel,
    onBackClick: () -> Unit,
    onSubmitReview: (userName: String, email: String, title: String, content: String, stars: Int, hasSpoilers: Boolean) -> Unit,
    initialUserName: String = "User sensato",
    initialEmail: String = "sensato@pearl.gg",
    initialTitle: String = "",
    initialContent: String = "",
    initialStars: Int = 5,
    initialHasSpoilers: Boolean = false,
) {
    var userName by rememberSaveable { mutableStateOf(initialUserName) }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var content by rememberSaveable { mutableStateOf(initialContent) }
    var stars by rememberSaveable { mutableStateOf(initialStars) }
    var hasSpoilers by rememberSaveable { mutableStateOf(initialHasSpoilers) }

    // Validade derivada diretamente por função pura (sem remember extra) - T3
    val validation = validateReviewForm(
        userName = userName,
        email = email,
        title = title,
        content = content,
        stars = stars,
    )

    AppBar(
        title = "Avaliar ${game.name}",
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sua nota para ${game.name}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (starValue in 1..5) {
                    val isSelected = starValue <= stars
                    Row(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(
                                selected = (stars == starValue),
                                onClick = { stars = starValue },
                                role = Role.RadioButton
                            )
                            .semantics {
                                contentDescription = "$starValue de 5 estrelas"
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text(
                    text = "$stars/5",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (validation.starsError != null) {
                Text(
                    text = validation.starsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Seu nome de usuário") },
                singleLine = true,
                isError = validation.userNameError != null,
                supportingText = {
                    validation.userNameError?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail do avaliador") },
                singleLine = true,
                isError = validation.emailError != null,
                supportingText = {
                    validation.emailError?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da avaliação") },
                placeholder = { Text("Ex.: Mundo aberto vivo e história envolvente") },
                singleLine = true,
                isError = validation.titleError != null,
                supportingText = {
                    validation.titleError?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Análise detalhada") },
                placeholder = { Text("Compartilhe seus pontos sobre gameplay, gráficos e narrativa...") },
                minLines = 4,
                isError = validation.contentError != null,
                supportingText = {
                    validation.contentError?.let { Text(it) }
                        ?: Text("Mínimo de 15 caracteres (${content.trim().length}/15)")
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Linha inteira agrupada com toggleable e Role.Switch (T8 - Acessibilidade)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .toggleable(
                        value = hasSpoilers,
                        role = Role.Switch,
                        onValueChange = { hasSpoilers = it }
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = "Esta avaliação contém spoilers",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Avise outros jogadores antes de exibir detalhes da história",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = hasSpoilers,
                    onCheckedChange = null // controlado pelo toggleable da Row pai
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (validation.isValid) {
                            onSubmitReview(
                                userName.trim(),
                                email.trim(),
                                title.trim(),
                                content.trim(),
                                stars,
                                hasSpoilers
                            )
                        }
                    },
                    enabled = validation.isValid,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    Text("Publicar")
                }
            }
        }
    }
}

@Preview
@Composable
private fun CreateReviewScreenInvalidPreview() {
    PearlTheme {
        CreateReviewScreen(
            game = MockGames.first(),
            onBackClick = {},
            onSubmitReview = { _, _, _, _, _, _ -> },
            initialTitle = "",
            initialContent = "Curto"
        )
    }
}

@Preview
@Composable
private fun CreateReviewScreenValidDarkPreview() {
    PearlTheme(darkTheme = true) {
        CreateReviewScreen(
            game = MockGames.first(),
            onBackClick = {},
            onSubmitReview = { _, _, _, _, _, _ -> },
            initialTitle = "Excelente evolução da franquia",
            initialContent = "A direção de arte e a física dos veículos estão em outro patamar neste título.",
            initialStars = 5,
            initialHasSpoilers = true
        )
    }
}
