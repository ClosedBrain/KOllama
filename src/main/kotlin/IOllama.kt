package de.jackBeBack

import de.jackBeBack.data.Embedding
import de.jackBeBack.data.Message
import de.jackBeBack.data.Models
import kotlinx.coroutines.flow.Flow

interface IOllama {
    suspend fun generate(prompt: String, onFinish: (String) -> Unit = {}): Flow<String>

    suspend fun chat(messages: List<Message>, onFinish: (List<Message>) -> Unit = {}): Flow<String>

    suspend fun embedding(prompt: String): Embedding

    suspend fun listModels(): Models
}