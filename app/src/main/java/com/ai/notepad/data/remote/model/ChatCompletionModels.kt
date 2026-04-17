package com.ai.notepad.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatCompletionRequest(
    val model: String = "MiniMax-M2.7-highspeed",
    val messages: List<MessageRequest>,
    val stream: Boolean = false
)

data class MessageRequest(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val finish_reason: String,
    val index: Int,
    val message: Message
)

data class Message(
    val role: String,
    val content: String
)

data class Usage(
    @SerializedName("total_tokens")
    val totalTokens: Int
)
