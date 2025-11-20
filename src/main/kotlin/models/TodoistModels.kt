package com.arekalov.models

import kotlinx.serialization.Serializable

@Serializable
data class TodoistTask(
    val id: String,
    val content: String,
    val description: String = "",
    val priority: Int = 1,
    val due: Due? = null,
    val projectId: String? = null,
    val labels: List<String> = emptyList()
)

@Serializable
data class Due(
    val date: String,
    val string: String? = null,
    val datetime: String? = null,
    val timezone: String? = null
)

