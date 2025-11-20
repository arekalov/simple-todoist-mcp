package com.arekalov.models

import kotlinx.serialization.Serializable

@Serializable
data class McpToolDescription(
    val name: String,
    val description: String,
    val inputSchema: InputSchema
)

@Serializable
data class InputSchema(
    val type: String,
    val properties: Map<String, PropertySchema>? = null,
    val required: List<String>? = null
)

@Serializable
data class PropertySchema(
    val type: String,
    val description: String
)

@Serializable
data class McpToolsListResponse(
    val tools: List<McpToolDescription>
)

@Serializable
data class McpToolCallRequest(
    val name: String,
    val arguments: Map<String, String> = emptyMap()
)

@Serializable
data class McpToolCallResponse(
    val content: List<McpContent>,
    val isError: Boolean = false
)

@Serializable
data class McpContent(
    val type: String = "text",
    val text: String
)

