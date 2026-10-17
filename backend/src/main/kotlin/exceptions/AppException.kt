package gg.pearl.exceptions

import io.ktor.http.HttpStatusCode

abstract class AppException(
    val status: HttpStatusCode,
    val title: String,
    val detail: String,
    val type: String = "about:blank"
) : RuntimeException(detail)
