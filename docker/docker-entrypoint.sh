#!/bin/sh
#
# Copyright (c) 2025 David Stibbe
#

echo "Starting Todo Server and MCP Server (stdio)..." >&2

# Start the todo-server in the background
java -jar /app/todo-server.jar >&2 &
TODO_SERVER_PID=$!

echo "Todo Server started with PID $TODO_SERVER_PID" >&2

# Wait for todo-server to be ready
echo "Waiting for Todo Server to be ready..." >&2
timeout=30
while [ $timeout -gt 0 ]; do
    if wget -q --spider http://localhost:8080/version 2>/dev/null; then
        echo "Todo Server is ready!" >&2
        break
    fi
    timeout=$((timeout-1))
    sleep 1
done

if [ $timeout -eq 0 ]; then
    echo "Warning: Todo Server may not be ready yet" >&2
fi

# Start the MCP server (stdio) in the foreground
echo "Starting MCP Server (stdio)..." >&2
exec java -jar /app/mcp-server-stdio.jar
