package gg.pearl.dto

data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int = if (size > 0) ((total + size - 1) / size).toInt() else 0
)
