/*
 * Copyright (c) 2025 David Stibbe
 */

package nl.dstibbe.labs.todomcp.todoserver

import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DriverManager

private val logger = KotlinLogging.logger {}

class TodoRepository(private val dbPath: String = "todos.db") {
    private val connection: Connection

    init {
        connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
        initializeDatabase()
    }

    private fun initializeDatabase() {
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS todos (
                    id TEXT PRIMARY KEY,
                    text TEXT NOT NULL,
                    done INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        logger.info { "Database initialized at $dbPath" }
    }

    fun create(todoItem: TodoItem) {
        connection.prepareStatement(
            "INSERT INTO todos (id, text, done) VALUES (?, ?, ?)"
        ).use { statement ->
            statement.setString(1, todoItem.id)
            statement.setString(2, todoItem.text)
            statement.setInt(3, if (todoItem.done) 1 else 0)
            statement.executeUpdate()
        }
        logger.debug { "Created todo: ${todoItem.id}" }
    }

    fun findAll(): List<TodoItem> {
        val todos = mutableListOf<TodoItem>()
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT id, text, done FROM todos").use { resultSet ->
                while (resultSet.next()) {
                    todos.add(
                        TodoItem(
                            id = resultSet.getString("id"),
                            text = resultSet.getString("text"),
                            done = resultSet.getInt("done") == 1
                        )
                    )
                }
            }
        }
        logger.debug { "Retrieved ${todos.size} todos" }
        return todos
    }

    fun findById(id: String): TodoItem? {
        connection.prepareStatement("SELECT id, text, done FROM todos WHERE id = ?").use { statement ->
            statement.setString(1, id)
            statement.executeQuery().use { resultSet ->
                return if (resultSet.next()) {
                    TodoItem(
                        id = resultSet.getString("id"),
                        text = resultSet.getString("text"),
                        done = resultSet.getInt("done") == 1
                    )
                } else {
                    null
                }
            }
        }
    }

    fun update(todoItem: TodoItem): Boolean {
        connection.prepareStatement(
            "UPDATE todos SET text = ?, done = ? WHERE id = ?"
        ).use { statement ->
            statement.setString(1, todoItem.text)
            statement.setInt(2, if (todoItem.done) 1 else 0)
            statement.setString(3, todoItem.id)
            val updated = statement.executeUpdate()
            if (updated > 0) {
                logger.debug { "Updated todo: ${todoItem.id}" }
            }
            return updated > 0
        }
    }

    fun delete(id: String): Boolean {
        connection.prepareStatement("DELETE FROM todos WHERE id = ?").use { statement ->
            statement.setString(1, id)
            val deleted = statement.executeUpdate()
            if (deleted > 0) {
                logger.debug { "Deleted todo: $id" }
            }
            return deleted > 0
        }
    }

    fun close() {
        connection.close()
        logger.info { "Database connection closed" }
    }
}
