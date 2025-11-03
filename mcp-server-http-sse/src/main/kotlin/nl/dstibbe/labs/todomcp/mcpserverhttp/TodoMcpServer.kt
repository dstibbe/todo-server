/*
 * Copyright (c) 2025 David Stibbe
 */

/*
 * Copyright (c) 2025 David Stibbe
 */

package nl.dstibbe.labs.todomcp.mcpserverhttp

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.dstibbe.labs.todomcp.restclient.TodoRestClient

fun main(args: Array<String>) {
    val mcpPort = if (args.isNotEmpty()) args[0].toInt() else 8081
    val todoUrl = if (args.size > 1) args[1] else "http://127.0.0.1:8080"
    val server = TodoMcpServer(mcpPort, todoUrl)
    server.start()
}

/**
 * MCP Server that exposes todo operations as tools
 */
class TodoMcpServer(
    private val port: Int = 8081,
    todoBaseUrl: String,
) {
    private val logger = KotlinLogging.logger {}
    private val todoClient = TodoRestClient(todoBaseUrl)

    // Register available tools
    fun configureMcp() = Server(
        Implementation(
            name = "mcp-kotlin todo server",
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
     * Start the MCP Server
     */
    fun start() = runBlocking {
        embeddedServer(CIO, host = "0.0.0.0", port = port) {
            install(CallLogging)
            install(ContentNegotiation) {
                jackson {}
            }
            mcp {
                return@mcp configureMcp()
            }
        }.startSuspend(wait = true)

        logger.info { "MCP Server started on port $port" }
    }
}