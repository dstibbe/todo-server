package nl.dare.labs

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class TodoRestClient(private val baseUrl: String = "http://127.0.0.1:8080") {

    private val logger = LoggerFactory.getLogger(TodoRestClient::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {}
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    /**
     * Add a new todo item
     * @param text The content of the todo item
     * @return The created TodoItem
     */
    fun addTodo(text: String): TodoItem {
        return runBlocking {
            try {
                logger.info("Adding todo item: $text")
                val response = client.post("$baseUrl/todo") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("text" to text))
                }

                if (response.status.isSuccess()) {
                    val todoItem = response.body<TodoItem>()
                    logger.info("Added todo item: $todoItem")
                    todoItem
                } else {
                    logger.error("Error from Todo Server: ${response.status}")
                    throw RuntimeException("Failed to add todo item: ${response.status}")
                }
            } catch (e: Exception) {
                logger.error("Error adding todo item", e)
                throw RuntimeException("Failed to add todo item", e)
            }
        }
    }


    /**
     * Finish a new todo item
     * @param id the id of the todo item
     * @return The created TodoItem
     */
    fun finishTodo(id: String): TodoItem {
        return runBlocking {
            try {
                logger.info("Finishing todo item: $id")
                val response = client.post("$baseUrl/todo/$id/state/true") {
                    contentType(ContentType.Application.Json)
                }

                if (response.status.isSuccess()) {
                    val todoItem = response.body<TodoItem>()
                    logger.info("Finished todo item: $todoItem")
                    todoItem
                } else {
                    logger.error("Error from Todo Server: ${response.status}")
                    throw RuntimeException("Failed to finish todo item: ${response.status}")
                }
            } catch (e: Exception) {
                logger.error("Error finishing todo item", e)
                throw RuntimeException("Failed to finish todo item", e)
            }
        }
    }

    /**
     * Get all todo items
     * @return List of all TodoItems
     */
    fun listTodos(): List<TodoItem> {
        return runBlocking {
            try {
                logger.info("Retrieving all todo items")
                val response = client.get("$baseUrl/todo")

                if (response.status.isSuccess()) {
                    val todoItems = response.body<List<TodoItem>>()
                    logger.info("Retrieved ${todoItems.size} todo items")
                    todoItems
                } else {
                    logger.error("Error from Todo Server: ${response.status}")
                    throw RuntimeException("Failed to get todo items: ${response.status}")
                }
            } catch (e: Exception) {
                logger.error("Error getting todo items", e)
                throw RuntimeException("Failed to get todo items", e)
            }
        }
    }

    /**
     * Delete a todo item by ID
     * @param id ID of the todo item to delete
     * @return true if the item was deleted, false if not found
     */
    fun removeTodo(id: String): Boolean {
        return runBlocking {
            try {
                logger.info("Deleting todo item with ID: $id")
                val response = client.delete("$baseUrl/todo/$id")

                when (response.status) {
                    HttpStatusCode.NoContent -> {
                        logger.info("Deleted todo item with ID: $id")
                        true
                    }

                    HttpStatusCode.NotFound -> {
                        logger.info("Todo item with ID: $id not found")
                        false
                    }

                    else -> {
                        logger.error("Error from Todo Server: ${response.status}")
                        throw RuntimeException("Failed to delete todo item: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error deleting todo item", e)
                throw RuntimeException("Failed to delete todo item", e)
            }
        }
    }
}
