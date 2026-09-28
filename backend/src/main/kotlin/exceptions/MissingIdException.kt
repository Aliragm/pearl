package gg.pearl.exceptions

import io.ktor.http.HttpStatusCode

class MissingIdException(
    detail: String = "Missing ID parameter"
) : AppException(
    status = HttpStatusCode.BadRequest,
    title = "Missing ID parameter",
    detail = detail,
    type = "https://pearl.com/exceptions/MissingIdException"
)