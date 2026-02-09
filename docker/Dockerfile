# Copyright (c) 2026 David Stibbe

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create data directory for the database
RUN mkdir -p /data

# Copy the pre-built artifacts
COPY todo-server/target/app.jar /app/todo-server.jar
COPY mcp-server-stdio/target/app.jar /app/mcp-server-stdio.jar

# Copy startup script
COPY docker/docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# Expose port for todo-server
EXPOSE 8080

# Use volume for database persistence
VOLUME /data

# Set environment variable for database path
ENV DB_PATH=/data/todos.db

ENTRYPOINT ["/app/docker-entrypoint.sh"]
