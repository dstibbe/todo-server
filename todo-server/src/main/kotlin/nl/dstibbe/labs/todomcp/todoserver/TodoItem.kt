/*
 * Copyright (c) 2025 David Stibbe
 */

package nl.dstibbe.labs.todomcp.todoserver

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    val text: String,
    val done: Boolean
)
