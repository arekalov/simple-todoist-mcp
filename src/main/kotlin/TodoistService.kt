package com.arekalov

import com.arekalov.models.TodoistTask
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodoistService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }
    
    private val baseUrl = "https://api.todoist.com/rest/v2"
    private val token = Config.todoistToken
    
    suspend fun getActiveTasks(): List<TodoistTask> {
        val response = client.get("$baseUrl/tasks") {
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        
        val allTasks: List<TodoistTask> = response.body()
        val today = LocalDate.now()
        
        // Filter tasks: overdue or due today
        return allTasks.filter { task ->
            task.due?.date?.let { dueDate ->
                val taskDate = LocalDate.parse(dueDate)
                taskDate.isBefore(today) || taskDate.isEqual(today)
            } ?: false
        }
    }
    
    fun close() {
        client.close()
    }
}

