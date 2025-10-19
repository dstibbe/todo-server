package nl.dare.labs

import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.install
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.LoggerFactory
import io.ktor.serialization.jackson.*

/**
 * MCP Server that exposes todo operations as tools
 */
class TodoMcpServer(
    private val port: Int = 8081,
    todoBaseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(TodoMcpServer::class.java)
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
            install(ContentNegotiation) {
                jackson {}
            }
            mcp {
                return@mcp configureMcp()
            }
        }.startSuspend(wait = true)

        logger.info("MCP Server started on port $port")
    }
}