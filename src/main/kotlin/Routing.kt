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
            application.environment.log.info("📋 Health check request received")
            call.respondText("MCP Todoist Server is running")
        }
        
        // MCP initialization endpoint
        post("/initialize") {
            application.environment.log.info("🔧 MCP initialization request received")
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
            application.environment.log.info("📋 Tools list request received")
            val tool = McpToolDescription(
                name = "get_active_tasks",
                description = "Retrieves all overdue and today's tasks from Todoist",
                inputSchema = InputSchema(
                    type = "object"
                )
            )
            
            val response = McpToolsListResponse(tools = listOf(tool))
            application.environment.log.info("✅ Returning 1 available tool")
            call.respond(HttpStatusCode.OK, response)
        }
        
        post("/tools/call") {
            application.environment.log.info("═══════════════════════════════════════")
            application.environment.log.info("🔧 Tool call request received")
            
            try {
                val request = call.receive<McpToolCallRequest>()
                application.environment.log.info("Tool name: ${request.name}")
                application.environment.log.info("Arguments: ${request.arguments}")
                
                if (request.name != "get_active_tasks") {
                    application.environment.log.error("❌ Unknown tool: ${request.name}")
                    val errorResponse = McpToolCallResponse(
                        content = listOf(McpContent(text = "Unknown tool: ${request.name}")),
                        isError = true
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                    return@post
                }
                
                application.environment.log.info("📥 Fetching tasks from Todoist API...")
                val startTime = System.currentTimeMillis()
                
                // Call Todoist API
                val tasks = todoistService.getActiveTasks()
                val duration = System.currentTimeMillis() - startTime
                
                application.environment.log.info("✅ Retrieved ${tasks.size} tasks from Todoist in ${duration}ms")
                
                // Format response
                val responseText = if (tasks.isEmpty()) {
                    application.environment.log.info("ℹ️ No active tasks found")
                    "No active tasks found for today or overdue."
                } else {
                    application.environment.log.info("📝 Formatting ${tasks.size} tasks")
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
                
                application.environment.log.info("✅ Sending successful response, text length: ${responseText.length}")
                application.environment.log.info("═══════════════════════════════════════")
                call.respond(HttpStatusCode.OK, response)
                
            } catch (e: Exception) {
                application.environment.log.error("❌ Error processing tool call: ${e.message}", e)
                val errorResponse = McpToolCallResponse(
                    content = listOf(McpContent(text = "Error: ${e.message}")),
                    isError = true
                )
                application.environment.log.info("═══════════════════════════════════════")
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
    }
}
