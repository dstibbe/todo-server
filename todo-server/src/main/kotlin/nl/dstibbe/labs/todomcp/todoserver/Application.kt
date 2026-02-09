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

private val logger = KotlinLogging.logger {}
private val todoRepository = TodoRepository()

@Serializable
data class TodoRequest(
    val text: String
)

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
                    todoRepository.create(todoItem)
                    call.respond(HttpStatusCode.Created, todoItem)
                } catch (e: Exception) {
                    logger.error { "Error creating todo item: $e" }
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            get {
                val todos = todoRepository.findAll()
                logger.info { "Retrieved ${todos.size} todo items" }
                call.respond(HttpStatusCode.OK, todos)
            }
        }

        route("/todo/{id}") {
            get {
                val id = call.parameters["id"]!!

                todoRepository.findById(id)?.let { todoItem ->
                    call.respond(todoItem)
                } ?: call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
            }

            delete {
                val id = call.parameters["id"]!!

                if (todoRepository.delete(id)) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Todo item deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
                }
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

                    todoRepository.findById(id)?.let { existingItem ->
                        val updatedItem = existingItem.copy(done = stateStr)
                        if (todoRepository.update(updatedItem)) {
                            call.respond(HttpStatusCode.OK, updatedItem)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update todo"))
                        }
                    } ?: run {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo item not found"))
                    }
                }
            }
        }
    }
}
