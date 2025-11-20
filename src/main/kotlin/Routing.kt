package com.arekalov

import com.arekalov.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    val todoistService = TodoistService()
    
    routing {
        get("/") {
            call.respondText("MCP Todoist Server is running")
        }
        
        // MCP initialization endpoint
        post("/initialize") {
            val response = mapOf(
                "protocolVersion" to "2024-11-05",
                "capabilities" to mapOf(
                    "tools" to emptyMap<String, Any>()
                ),
                "serverInfo" to mapOf(
                    "name" to "todoist-mcp-server",
                    "version" to "1.0.0"
                )
            )
            call.respond(HttpStatusCode.OK, response)
        }
        
        post("/tools/list") {
            val tool = McpToolDescription(
                name = "get_active_tasks",
                description = "Retrieves all overdue and today's tasks from Todoist",
                inputSchema = InputSchema(
                    type = "object"
                )
            )
            
            val response = McpToolsListResponse(tools = listOf(tool))
            call.respond(HttpStatusCode.OK, response)
        }
        
        post("/tools/call") {
            try {
                val request = call.receive<McpToolCallRequest>()
                
                if (request.name != "get_active_tasks") {
                    val errorResponse = McpToolCallResponse(
                        content = listOf(McpContent(text = "Unknown tool: ${request.name}")),
                        isError = true
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                    return@post
                }
                
                // Call Todoist API
                val tasks = todoistService.getActiveTasks()
                
                // Format response
                val responseText = if (tasks.isEmpty()) {
                    "No active tasks found for today or overdue."
                } else {
                    buildString {
                        appendLine("Active Tasks (${tasks.size}):")
                        appendLine()
                        tasks.forEachIndexed { index, task ->
                            appendLine("${index + 1}. ${task.content}")
                            if (task.description.isNotEmpty()) {
                                appendLine("   Description: ${task.description}")
                            }
                            task.due?.let { due ->
                                appendLine("   Due: ${due.string ?: due.date}")
                            }
                            appendLine("   Priority: ${task.priority}")
                            if (task.labels.isNotEmpty()) {
                                appendLine("   Labels: ${task.labels.joinToString(", ")}")
                            }
                            appendLine()
                        }
                    }
                }
                
                val response = McpToolCallResponse(
                    content = listOf(McpContent(text = responseText)),
                    isError = false
                )
                
                call.respond(HttpStatusCode.OK, response)
                
            } catch (e: Exception) {
                val errorResponse = McpToolCallResponse(
                    content = listOf(McpContent(text = "Error: ${e.message}")),
                    isError = true
                )
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
    }
}
