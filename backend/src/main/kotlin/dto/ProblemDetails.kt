package gg.pearl.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProblemDetails(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String? = null
)
