package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import java.util.*

// In-memory storage for todo items
private val todoItems = mutableMapOf<String, TodoItem>()

@Serializable
data class TodoRequest(
    val text: String,
    val done: Boolean = false
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(io.ktor.server.plugins.callloging.CallLogging)
    
    install(io.ktor.server.plugins.statuspages.StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Unknown error")))
        }
    }

    routing {
        // POST /todo - add a new todo item
        post("/todo") {
            val request = call.receive<TodoRequest>()
            val id = UUID.randomUUID().toString()
            val todoItem = TodoItem(
                id = id,
                text = request.text,
                done = request.done
            )
            todoItems[id] = todoItem
            call.respond(HttpStatusCode.Created, todoItem)
        }

        // GET /todo - get all todo items
        get("/todo") {
            call.respond(ArrayList(todoItems.values))
        }

        // GET /todo/{id} - get specific todo item
        get("/todo/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id parameter"))
                return@get
            }
            
            val todoItem = todoItems[id]
            if (todoItem == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
            } else {
                call.respond(todoItem)
            }
        }

        // DELETE /todo/{id} - delete specific todo item
        delete("/todo/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id parameter"))
                return@delete
            }
            
            val removed = todoItems.remove(id)
            if (removed == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Todo item deleted"))
            }
        }

        // POST /todo/{id}/state/{state} - update state of specific todo item
        post("/todo/{id}/state/{state}") {
            val id = call.parameters["id"]
            val stateStr = call.parameters["state"]
            
            if (id == null || stateStr == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id or state parameter"))
                return@post
            }
            
            val state = when (stateStr.lowercase()) {
                "true", "done" -> true
                "false", "undone" -> false
                else -> {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid state. Use 'true' or 'false'"))
                    return@post
                }
            }
            
            val todoItem = todoItems[id]
            if (todoItem == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
            } else {
                val updatedItem = todoItem.copy(done = state)
                todoItems[id] = updatedItem
                call.respond(updatedItem)
            }
        }
    }
}
