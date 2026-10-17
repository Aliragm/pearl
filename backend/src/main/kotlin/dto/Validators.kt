package gg.pearl.dto

import gg.pearl.exceptions.ValidationException

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun UserDTO.validate() {
    if (name.isBlank() || name.trim().length < 2) {
        throw ValidationException("User name must have at least 2 non-blank characters.")
    }
    if (email.isBlank() || !EMAIL_REGEX.matches(email.trim())) {
        throw ValidationException("A valid email address is required.")
    }
    if (password.length < 6) {
        throw ValidationException("Password must be at least 6 characters long.")
    }
}

fun UpdateUserDTO.validate() {
    if (name == null && email == null) {
        throw ValidationException("At least one field (name or email) must be provided for update.")
    }
    if (name != null && (name.isBlank() || name.trim().length < 2)) {
        throw ValidationException("User name must have at least 2 non-blank characters.")
    }
    if (email != null && (email.isBlank() || !EMAIL_REGEX.matches(email.trim()))) {
        throw ValidationException("A valid email address is required.")
    }
}

fun GameDTO.validate() {
    if (name.isBlank()) {
        throw ValidationException("Game name must not be blank.")
    }
    if (developer.isBlank()) {
        throw ValidationException("Game developer must not be blank.")
    }
    if (meanScore !in 0.0..5.0) {
        throw ValidationException("Game meanScore must be between 0.0 and 5.0.")
    }
}

fun UpdateGameDTO.validate() {
    if (name == null && developer == null && meanScore == null && platform == null) {
        throw ValidationException("At least one field must be provided to update a game.")
    }
    if (name != null && name.isBlank()) {
        throw ValidationException("Game name must not be blank.")
    }
    if (developer != null && developer.isBlank()) {
        throw ValidationException("Game developer must not be blank.")
    }
    if (meanScore != null && meanScore !in 0.0..5.0) {
        throw ValidationException("Game meanScore must be between 0.0 and 5.0.")
    }
}

fun PostDTO.validate() {
    if (title.isBlank()) {
        throw ValidationException("Post title must not be blank.")
    }
    if (content.isBlank()) {
        throw ValidationException("Post content must not be blank.")
    }
    if (score !in 0.0..5.0) {
        throw ValidationException("Post score must be between 0.0 and 5.0.")
    }
}

fun UpdatePostDTO.validate() {
    if (title == null && content == null && score == null) {
        throw ValidationException("At least one field (title, content, or score) must be provided for update.")
    }
    if (title != null && title.isBlank()) {
        throw ValidationException("Post title must not be blank.")
    }
    if (content != null && content.isBlank()) {
        throw ValidationException("Post content must not be blank.")
    }
    if (score != null && score !in 0.0..5.0) {
        throw ValidationException("Post score must be between 0.0 and 5.0.")
    }
}

fun CommentsDTO.validate() {
    if (content.isBlank()) {
        throw ValidationException("Comment content must not be blank.")
    }
}

fun UpdateCommentDTO.validate() {
    if (content == null || content.isBlank()) {
        throw ValidationException("Comment content must not be blank.")
    }
}
