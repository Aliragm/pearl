package gg.pearl.exceptions

import io.ktor.http.HttpStatusCode

class InvalidUuidException(
    detail: String = "Invalid UUID format"
) : AppException(
    status = HttpStatusCode.BadRequest,
    title = "Invalid UUID format",
    detail = detail,
    type = "https://pearl.com/exceptions/InvalidUuidException"
)