# Todo Server Docker

This directory contains a Dockerfile that packages both the todo-server and the MCP server (stdio) in a single container.

## Prerequisites

Build the project with Maven before creating the Docker image:

```bash
mvn clean package
```

## Building the Image

From the project root directory:

```bash
docker build -f docker/Dockerfile -t todo-server .
```

## Running the Container

```bash
docker run -d \
  -p 8080:8080 \
  -v todo-data:/data \
  --name todo-server \
  todo-server
```

## Configuration

- **Port**: The todo-server runs on port 8080
- **Database**: The SQLite database is stored in `/data/todos.db` inside the container
- **Volume**: Mount a volume at `/data` to persist the database across container restarts

## Environment Variables

- `DB_PATH`: Path to the SQLite database file (default: `/data/todos.db`)

## Services

The container runs two services:
1. **Todo Server** (HTTP REST API) - runs on port 8080
2. **MCP Server (stdio)** - provides MCP protocol interface

Both services share the same database located on the persistent volume.
