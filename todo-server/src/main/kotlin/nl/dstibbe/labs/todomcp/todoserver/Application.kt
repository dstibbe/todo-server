/*
 * Copyright (c) 2025 David Stibbe
 */

package nl.dstibbe.labs.todomcp.todoserver

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.*

// In-memory storage for todo items
private val todoItems = mutableMapOf<String, TodoItem>()

@Serializable
data class TodoRequest(
    val text: String
)

private val logger = KotlinLogging.logger {}

fun main() {
    embeddedServer(CIO, host = "0.0.0.0", port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(CallLogging)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error { "Unhandled exception: $cause" }
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Unknown error")))
        }
    }

    routing {
        route("/todo") {
            post {
                try {
                    val request = call.receive<TodoRequest>()
                    val id = UUID.randomUUID().toString()
                    val todoItem = TodoItem(
                        id = id,
                        text = request.text,
                        done = false,
                    )
                    todoItems[id] = todoItem
                    call.respond(HttpStatusCode.Created, todoItem)
                } catch (e: Exception) {
                    logger.error { "Error creating todo item: $e" }
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            get {
                val todos = todoItems.values
                logger.info { "Retrieved ${todos.size} todo items" }
                call.respond(HttpStatusCode.OK, todos)
            }
        }

        route("/todo/{id}") {
            get {
                val id = call.parameters["id"]!!

                todoItems[id]?.let { todoItem ->
                    call.respond(todoItem)
                } ?: call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
            }

            delete {
                val id = call.parameters["id"]!!

                todoItems.remove(id)?.let {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Todo item deleted"))
                } ?: call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
            }

            route("/state/{state}") {
                post {
                    val id = call.parameters["id"]!!
                    val stateStr = call.parameters["state"]?.toBooleanStrictOrNull()

                    if (stateStr == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing id or proper state parameter")
                        )
                        return@post
                    }

                    todoItems[id]?.apply {
                        val newItem = this.copy(done = stateStr)
                        todoItems[id] = newItem
                        call.respond(HttpStatusCode.OK, newItem)
                    } ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
                    }
                }
            }
        }
    }
}
