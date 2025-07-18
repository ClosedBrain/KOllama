package de.jackBeBack.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json


val json = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalSerializationApi::class)
fun String.toGenerateResponse(): GenerateResponse? {
    return try {
        json.decodeFromString(this)
    } catch (_: Exception) {
        null
    }
}


@OptIn(ExperimentalSerializationApi::class)
fun String.toChatResponse(): ChatResponse? {
    return try {
        json.decodeFromString(this)
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun String.toEmbedding(): Embedding {
    return json.decodeFromString(this)
}

@OptIn(ExperimentalSerializationApi::class)
fun String.toModels(): Models {
    return json.decodeFromString(this)
}

