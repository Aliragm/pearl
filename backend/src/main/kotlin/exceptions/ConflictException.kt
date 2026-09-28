package gg.pearl.exceptions

import io.ktor.http.HttpStatusCode

class ConflictException(
    detail: String
) : AppException(
    status = HttpStatusCode.Conflict,
    title = "Conflict",
    detail = detail,
    type = "https://pearl.com/exceptions/ConflictException"
)
