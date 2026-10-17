package gg.pearl.exceptions

import io.ktor.http.HttpStatusCode

class NotFoundException(
    detail: String = "Resource not found"
) : AppException(
    status = HttpStatusCode.NotFound,
    title = "Resource Not Found",
    detail = detail,
    type = "https://pearl.com/exceptions/NotFoundException"
)
