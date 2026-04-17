package com.ai.notepad.data.remote.api

import com.ai.notepad.data.remote.model.ChatCompletionRequest
import com.ai.notepad.data.remote.model.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MiniMaxApiService {

    @POST("/v1/text/chatcompletion_v2")
    suspend fun chatCompletion(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
