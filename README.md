
![Logo](https://github.com/dstibbe/todo-server/blob/bbf781535bafcadb07d769a5ee91a102029928a4/todo-mcp-logo.png?raw=true)

# Todo-server with MCP

A simple todo server for testing purposes with MCP (Model Context Protocol) integration.

## Description
A REST API server built with Kotlin and Ktor for managing todo items.
Todo items are persisted in a lightweight SQLite database (todos.db).
Also provides two MCP servers for exposing the functionality to AI Agents via the MCP protocol.
The two MCP servers are exposed over different transports: one over HTTP/SSE and one over stdio.

## Modules

- **todo-server**: REST API server for managing todos (port 8080)
- **mcp-server-http-sse**: MCP server using HTTP/SSE transport (port 8081)
- **mcp-server-stdio**: MCP server using stdio transport
- **todo-rest-client**: Client library for programmatic access to the REST API

## Building and Running

### Build

Build the project using Maven:
```bash
mvn clean package
```

### Run

#### Todo Server
Start the REST API server:
```bash
java -jar todo-server/target/todo-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```
The REST server runs by default on port 8080.

#### MCP HTTP/SSE Server
Start the MCP server with HTTP/SSE transport:
```bash
java -jar mcp-server-http-sse/target/mcp-server-http-sse-1.0-SNAPSHOT-jar-with-dependencies.jar [port] [todo-server-url]
```
Default port: 8081, Default todo-server-url: http://127.0.0.1:8080

#### MCP Stdio Server
Start the MCP server with stdio transport:
```bash
java -jar mcp-server-stdio/target/mcp-server-stdio-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### API Endpoints

**Todo Operations:**
- `POST /todo` - Create a new todo item
- `GET /todo` - Get all todo items
- `GET /todo/{id}` - Get a specific todo item
- `DELETE /todo/{id}` - Delete a todo item
- `POST /todo/{id}/state/{true|false}` - Update todo completion state

**Version Information:**
- `GET /version` - Get server version and build date

### Testing

**REST API Testing:**
The REST API can be tested using the provided `call todo server.http` file in the `todo-server/src/test` folder.

**MCP Server Testing:**
The MCP servers can be tested using the [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector).
To quickly install and run the MCP Inspector, use the following commands:

```bash
npx @modelcontextprotocol/inspector
```

## Features

- SQLite database for persistent storage
- RESTful API for todo management
- MCP protocol support via HTTP/SSE and stdio transports
- Version tracking for all modules
- Comprehensive logging