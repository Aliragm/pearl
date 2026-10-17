package gg.pearl.exceptions

import io.ktor.http.HttpStatusCode

class ValidationException(
    detail: String
) : AppException(
    status = HttpStatusCode.UnprocessableEntity,
    title = "Validation Error",
    detail = detail,
    type = "https://pearl.com/exceptions/ValidationException"
)
