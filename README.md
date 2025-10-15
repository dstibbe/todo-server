# todo-server
A simple todo server for testing purposes

## Description
A REST API server built with Kotlin and Ktor for managing todo items.

## Data Model
```kotlin
TodoItem(
    val id: String,
    val text: String,
    val done: Boolean
)
```

## API Endpoints

### POST /todo
Add a new todo item. Automatically generates a unique ID for the item.

**Request Body:**
```json
{
  "text": "Buy groceries",
  "done": false
}
```

**Response:**
```json
{
  "id": "f58783f4-d595-44cb-bd73-7292c9cb45cb",
  "text": "Buy groceries",
  "done": false
}
```

### GET /todo
Get all todo items.

**Response:**
```json
[
  {
    "id": "f58783f4-d595-44cb-bd73-7292c9cb45cb",
    "text": "Buy groceries",
    "done": false
  }
]
```

### GET /todo/{id}
Get a specific todo item by ID.

**Response:**
```json
{
  "id": "f58783f4-d595-44cb-bd73-7292c9cb45cb",
  "text": "Buy groceries",
  "done": false
}
```

### DELETE /todo/{id}
Delete a specific todo item by ID.

**Response:**
```json
{
  "message": "Todo item deleted"
}
```

### POST /todo/{id}/state/{state}
Update the done state of a specific todo item. State can be `true` or `false`.

**Response:**
```json
{
  "id": "f58783f4-d595-44cb-bd73-7292c9cb45cb",
  "text": "Buy groceries",
  "done": true
}
```

## Building and Running

### Build
```bash
mvn clean package
```

### Run
```bash
java -jar target/todo-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The server will start on `http://localhost:8080`.

## Requirements
- Java 11 or higher
- Maven 3.x
