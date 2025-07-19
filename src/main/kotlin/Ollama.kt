package de.jackBeBack

import de.jackBeBack.data.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi

@Suppress("unused")
open class Ollama(host: String = "localhost", port: Short = 11434, ssl: Boolean = false, val model: String = "llama2") {

    /**
     * State of the Ollama Server.
     */
    private val _currentState = MutableStateFlow(LLMState.WAITING)
    val currentState: StateFlow<LLMState> = _currentState

    private val _url = "http${if (ssl) "s" else ""}://$host:$port/api"

    /**
     * Ktor Client with Timeout will be used by all Requests
     */
    private val client = HttpClient(CIO) {
        engine {
            // Configure timeouts
            requestTimeout = 300000 // 300 seconds
        }
    }


    /**
    Send the prompt to the server and return a flow of responses
    Flow will be empty until the server responds
    Flow will be closed when the server closes the connection
    Flow will be cancelled when the coroutine scope is cancelled
    @param prompt The prompt to send to the server
    @param onFinish A callback that will be called when the server closes the connection with the whole generated text
    @return A Flow of generated Tokens from the server
     */
    @OptIn(InternalAPI::class, ExperimentalSerializationApi::class)
    suspend fun generate(prompt: String, onFinish: (String) -> Unit = {}): Flow<String> {
        if (_currentState.value == LLMState.RUNNING) {
            throw Exception("Already running")
        }
        val response: HttpResponse = client.post("$_url/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CompletionRequest(
                        model,
                        prompt,
                        system = ""
                    )
                )
            )
        }

        return flow {
            var generatedText = ""
            val channel: ByteReadChannel = response.bodyAsChannel()
            while (true) {
                if (channel.availableForRead > 0) {
                    val response = channel.readUTF8Line()?.toGenerateResponse()
                    if (response != null) {
                        generatedText += response.response
                        emit(response.response)
                    }
                }
                if (channel.isClosedForRead) break
                delay(50) // A small delay to prevent tight looping
            }
            onFinish(generatedText)
        }
    }

    /**
     * @param messages history of messages latest message should be from the user
     * @param onFinish Callback when Generation is finished. Will be called with the new messages history where the last entry is the most recent response form the server
     * @return A Flow of generated Tokens from the server
     */
    @OptIn(InternalAPI::class)
    suspend fun chat(messages: List<Message>, onFinish: (List<Message>) -> Unit = {}): Flow<String> {
        if (_currentState.value == LLMState.RUNNING) {
            throw Exception("Already running")
        }
        val response: HttpResponse = client.post("$_url/chat") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    ChatRequest(
                        model,
                        messages
                    )
                )
            )
        }

        return flow {
            var generatedText = ""
            val channel: ByteReadChannel = response.bodyAsChannel()
            while (true) {
                if (channel.availableForRead > 0) {
                    val chatResponse = channel.readUTF8Line()?.toChatResponse()
                    if (chatResponse != null) {
                        generatedText += chatResponse.message.content
                        emit(chatResponse.message.content)
                    }
                }
                if (channel.isClosedForRead) break
                delay(50) // A small delay to prevent tight looping
            }
            val newMessages = messages.toMutableList()
            newMessages.add(Message(Role.SYSTEM, generatedText))
            onFinish(newMessages)
        }
    }

    /**
     * @param prompt The prompt to generate the embedding for
     * @return The embedding of the prompt
     */
    @OptIn(InternalAPI::class)
    suspend fun embedding(prompt: String): Embedding {
        if (_currentState.value == LLMState.RUNNING) {
            throw Exception("Already running")
        }
        val response: HttpResponse = client.post("$_url/embeddings") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    EmbeddingRequest(
                        model,
                        prompt
                    )
                )
            )
        }

        return response.bodyAsText().toEmbedding()
    }

    /**
     * @return The list of available models
     */
    @OptIn(InternalAPI::class)
    suspend fun listModels(): Models {
        if (_currentState.value == LLMState.RUNNING) {
            throw Exception("Already running")
        }
        val response: HttpResponse = client.get("$_url/tags") {
            contentType(ContentType.Application.Json)
        }

        return response.bodyAsText().toModels()
    }
}


/**
 * State Class for the Llama Server
 */
enum class LLMState {
    WAITING,
    RUNNING
}
