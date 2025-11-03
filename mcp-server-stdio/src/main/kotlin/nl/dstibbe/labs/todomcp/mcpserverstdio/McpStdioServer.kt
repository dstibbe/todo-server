/*
 * Copyright (c) 2025 David Stibbe
 */

/*
 * Copyright (c) 2025 David Stibbe
 */

package nl.dstibbe.labs.todomcp.mcpserverstdio

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.dstibbe.labs.todomcp.restclient.TodoRestClient
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) = runBlocking {
    // Set log level to INFO programmatically
    with(LoggerFactory.getILoggerFactory() as LoggerContext) {
        getLogger(ROOT_LOGGER_NAME).level = Level.OFF
    }

    try {
        // Create and start the server
        val server = McpStdioServer()
        System.err.println("MCP StdIO Server started successfully")
        server.start()
    } catch (e: Exception) {
        System.err.println("Failed to start MCP StdIO Server: ${e.message}")
        exitProcess(1)
    }
}

/**
 * MCP Server implementation that communicates via standard input/output.
 * This class handles the communication between the DevCommands client and the MCP protocol.
 */
class McpStdioServer(val todoClient: TodoRestClient = TodoRestClient()) {

    private val logger = KotlinLogging.logger {}
    private val server = configureMcp()

    /**
     * Configure the MCP Server with capabilities and handlers
     */
    private fun configureMcp() = Server(
        Implementation(
            name = "mcp-kotlin presentation controller stdio server",
            version = "1.0.0"
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
//                prompts = ServerCapabilities.Prompts(listChanged = true),
//                resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                tools = ServerCapabilities.Tools(listChanged = true),
                logging = null
            )
        )
    ).apply {
        // Add create task tool
        addTool(
            name = "add_todo",
            description = "Add a new todo item",
            inputSchema = Tool.Input(
                properties = JsonObject(
                    mapOf(
                        "text" to JsonObject(mapOf("type" to JsonPrimitive("string"))),
                    )
                ),
                required = listOf("text")
            )
        ) { request ->
            val todoText = (request.arguments["text"] as JsonPrimitive).content
            val todoItem = todoClient.addTodo(todoText)
            CallToolResult(
                content = listOf(
                    TextContent("Added todo item: ${todoItem.text}")
                )
            )
        }

        addTool(
            name = "list_todos",
            description = "List all todo items",
            inputSchema = Tool.Input()
        ) { request ->
            val todos = todoClient.listTodos()
            CallToolResult(
                content = listOf(
                    TextContent(todos.joinToString("\n") { "{ id: ${it.id}, text:${it.text}, done: ${it.done} }" })
                )
            )
        }

        addTool(
            name = "remove_todo",
            description = "remove a todo item",
            inputSchema = Tool.Input(
                properties = JsonObject(
                    mapOf(
                        "id" to JsonObject(mapOf("type" to JsonPrimitive("string"))),
                    )
                )
            )
        ) { request ->
            val id = (request.arguments["id"] as JsonPrimitive).content
            val removed = todoClient.removeTodo(id)

            if (removed) {
                CallToolResult(
                    content = listOf(
                        TextContent("Todo removed successfully")
                    )
                )
            } else {
                CallToolResult(
                    content = listOf(
                        TextContent("Todo item not found")
                    ),
                    isError = true,
                )
            }
        }
        addTool(
            name = "finish_todo",
            description = "Mark todo item as completed",
            inputSchema = Tool.Input(
                properties = JsonObject(
                    mapOf(
                        "id" to JsonObject(mapOf("type" to JsonPrimitive("string"))),
                    )
                ),
                required = listOf("id")
            )
        ) { request ->
            val id = (request.arguments["id"] as JsonPrimitive).content
            val finishedItem = todoClient.finishTodo(id)
            CallToolResult(
                content = listOf(
                    TextContent("Finished todo item: $finishedItem")
                )
            )
        }
    }


    /**
     * Starts the MCP server and listens for input on stdin.
     * Sends responses back through stdout.
     */
    suspend fun start() {
        server.connect(
            StdioServerTransport(
                inputStream = System.`in`.asInput(),
                outputStream = System.out.asSink().buffered()
            )
        )
        val done = Job()
        System.err.println("Server is CONNECTED!!!")
        server.onClose {
            done.complete()
        }
        done.join()
        System.err.println("Server closed")
    }
}
