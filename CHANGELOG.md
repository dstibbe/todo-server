# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0-SNAPSHOT] - In Development

### Added
- Versioning mechanism for todo-server module
  - version.properties file with Maven resource filtering
  - loadVersion() function to read version from properties
  - /version API endpoint to expose version information
  - Version logging at server startup
- SQLite database integration for persistent todo storage
  - TodoRepository.kt for database operations
  - Automatic database initialization
  - CRUD operations with SQLite backend
  - Database file stored as todos.db
- Todo REST API server using Kotlin and Ktor
  - In-memory todo item management
  - CRUD operations for todo items
  - Application.kt with REST endpoints
  - TodoItem.kt data model
  - Runs on port 8080 by default
- MCP Server HTTP/SSE implementation
  - TodoMcpServer.kt for HTTP/SSE transport
  - Exposes todo functionality via MCP protocol over HTTP
  - Runs on port 8081 by default
- MCP Server stdio implementation
  - McpStdioServer.kt for stdio transport
  - Exposes todo functionality via MCP protocol over stdio
- Todo REST Client
  - TodoRestClient.kt for programmatic access
  - TodoItem.kt client-side data model
- Project infrastructure
  - Maven multi-module project structure
  - Logback logging configuration for all modules
  - Copyright headers on all source files
  - Testing utilities (call todo server.http)
  - Todo MCP logo image
- Documentation
  - README.md with project description
  - Build and run instructions
  - Testing guidelines
  - MCP Inspector integration guide
  - LICENSE file (GNU General Public License v3.0)
  - CHANGELOG.md for tracking project changes

### Changed
- Replaced in-memory todo storage with SQLite database
- Cleaned up project structure and dependencies
- Updated POM configurations for better dependency management
- Removed duplicate copyright headers from all source files

### Removed
- AI Agent module (agent/)
  - Removed KoogAgent.kt implementation
  - Removed Ollama LLM integration
