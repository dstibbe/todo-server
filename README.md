
![Logo](https://github.com/dstibbe/todo-server/blob/bbf781535bafcadb07d769a5ee91a102029928a4/todo-mcp-logo.png?raw=true)

# Todo-server with MCP

A simple todo server for testing purposes. Includes two MCP servers

## Description
A REST API server built with Kotlin and Ktor for managing todo items (in memory).
Also provides two MCP servers for exposing the functionality to AI Agents via the MCP protocol.
The two MCP agents are exposed over different media: one over HTTP and one over stdio.

## Building and Running

### Build

Build the project using Maven:
```bash
mvn clean package
```

### Run
Each server provides a main method to start it.
The REST server runs by default on port 8080. The HTTP MCP server runs by default on port 8081.

### Testing

The REST API can be tested using the provided `call todo server.http` file in the `src/test/resources` folder.

The MCP servers can be tested using the [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector).
To quickly install and run the MCP Inspector, use the following commands:

```bash
npx @modelcontextprotocol/inspector
```