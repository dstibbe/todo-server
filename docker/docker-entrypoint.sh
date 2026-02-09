#!/bin/sh
#
# Copyright (c) 2025 David Stibbe
#

echo "Starting Todo Server and MCP Server (stdio)..."

# Start the todo-server in the background
java -jar /app/todo-server.jar &
TODO_SERVER_PID=$!

echo "Todo Server started with PID $TODO_SERVER_PID"

# Wait for todo-server to be ready
echo "Waiting for Todo Server to be ready..."
timeout=30
while [ $timeout -gt 0 ]; do
    if wget -q --spider http://localhost:8080/version 2>/dev/null; then
        echo "Todo Server is ready!"
        break
    fi
    timeout=$((timeout-1))
    sleep 1
done

if [ $timeout -eq 0 ]; then
    echo "Warning: Todo Server may not be ready yet"
fi

# Start the MCP server (stdio) in the foreground
echo "Starting MCP Server (stdio)..."
exec java -jar /app/mcp-server-stdio.jar
