package nl.dare.labs

fun main(args: Array<String>) {
    val mcpPort = if (args.isNotEmpty()) args[0].toInt() else 8081
    val todoUrl = if (args.size > 1) args[1] else "http://127.0.0.1:8080"
    val server = TodoMcpServer(mcpPort, todoUrl)
    server.start()
}