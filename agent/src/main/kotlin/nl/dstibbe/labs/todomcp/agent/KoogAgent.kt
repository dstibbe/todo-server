/*
 * Copyright (c) 2025 David Stibbe
 */

package nl.dstibbe.labs.todomcp.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.OllamaModels
import ai.koog.prompt.params.LLMParams
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {

    val transport = McpToolRegistryProvider.defaultSseTransport("http://localhost:8081/sse")

    val toolRegistry = McpToolRegistryProvider.fromTransport(
        transport = transport,
        name = "todo-via-koog",
        version = "1.0.0"
    )

    val ollamaAgentConfig = AIAgentConfig(
        prompt = prompt("Personal Assistant", LLMParams(temperature = 0.0)) {
            system("You are my personal assistant. Help me with my tasks.")
        },
        model = OllamaModels.Groq.LLAMA_3_GROK_TOOL_USE_8B,
        maxAgentIterations = 50
    )

    val myStrategy = strategy<String, String>("my-strategy") {
        val nodeCallLLM by nodeLLMRequest()
        val executeToolCall by nodeExecuteTool()
        val sendToolResult by nodeLLMSendToolResult()

        edge(nodeStart forwardTo nodeCallLLM)
        edge(nodeCallLLM forwardTo nodeFinish onAssistantMessage { true })
        edge(nodeCallLLM forwardTo executeToolCall onToolCall { true })
        edge(executeToolCall forwardTo sendToolResult)
        edge(sendToolResult forwardTo nodeFinish onAssistantMessage { true })
    }

    val ollamaAgent = AIAgent(
        promptExecutor = simpleOllamaAIExecutor(),
        strategy = myStrategy,
        agentConfig = ollamaAgentConfig,
        toolRegistry = toolRegistry
    ) {
        handleEvents {
            onToolCall { e ->
                println("Tool called: ${e.tool.name}, args=${e.toolArgs}")
            }
            onAgentRunError { e ->
                println("Agent error: ${e.throwable.message}")
            }
            onAgentFinished { e ->
                println("Final result: ${e.result}")
            }
            onBeforeLLMCall { e ->
                println("LLM called: ${e.prompt}")
            }
        }
    }

    /**
     * Change the prompt below to change the interaction with the agent
     */
    ollamaAgent.run("Please add 'I love cake' to the todo list")
}