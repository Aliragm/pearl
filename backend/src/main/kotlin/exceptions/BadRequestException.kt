package gg.pearl.exceptions

import io.ktor.http.HttpStatusCode

class BadRequestException(
    detail: String = "Bad request"
) : AppException(
    status = HttpStatusCode.BadRequest,
    title = "Bad Request",
    detail = detail,
    type = "https://pearl.com/exceptions/BadRequestException"
)
