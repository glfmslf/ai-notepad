package com.ai.notepad.domain.usecase

import android.util.Log
import com.ai.notepad.data.remote.api.MiniMaxApiService
import com.ai.notepad.data.remote.model.ChatCompletionRequest
import com.ai.notepad.data.remote.model.MessageRequest
import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.data.repository.PromptRepository
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.util.ApiKeyManager
import com.ai.notepad.util.PromptTemplate
import com.ai.notepad.util.Result
import com.ai.notepad.util.safeApiCall
import java.time.LocalDateTime
import java.time.LocalDate
import javax.inject.Inject

class SummarizeDailyUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val promptRepository: PromptRepository,
    private val apiService: MiniMaxApiService,
    private val apiKeyManager: ApiKeyManager
) {
    companion object {
        private const val TAG = "SummarizeDailyUseCase"
    }

    suspend operator fun invoke(entryId: Long): Result<DiaryEntry> {
        Log.d(TAG, "Starting daily summarization for entryId: $entryId")

        if (!apiKeyManager.hasApiKey()) {
            Log.w(TAG, "API Key not configured")
            return Result.Error(Exception("API Key 未配置，请在设置中配置"))
        }

        val today = LocalDate.now()
        Log.d(TAG, "Fetching entry for date: $today")

        val entry = diaryRepository.getByDate(today)
        if (entry == null) {
            Log.w(TAG, "Entry not found for date: $today")
            return Result.Error(Exception("日记不存在"))
        }

        if (entry.originalText.isBlank()) {
            Log.w(TAG, "Entry text is blank")
            return Result.Error(Exception("日记内容为空"))
        }

        Log.d(TAG, "Entry text length: ${entry.originalText.length}")

        val promptConfig = promptRepository.get()
        val prompt = PromptTemplate.getDailyPrompt(promptConfig, entry.originalText)
        Log.d(TAG, "Generated prompt length: ${prompt.length}")

        val request = ChatCompletionRequest(
            model = "MiniMax-M2.7-highspeed",
            messages = listOf(
                MessageRequest(role = "user", content = prompt)
            )
        )

        return safeApiCall {
            val apiKey = apiKeyManager.getApiKey() ?: throw Exception("API Key 不存在")
            Log.d(TAG, "Calling MiniMax API...")

            val response = apiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            Log.d(TAG, "API Response id: ${response.id}, choices count: ${response.choices?.size ?: 0}")

            // 检查 API 是否返回了错误
            if (response.id.isBlank() || response.choices == null) {
                Log.e(TAG, "API returned empty id or null choices")
                throw Exception("API 请求失败，请检查 API Key 是否有效")
            }

            val choices = response.choices
            if (choices.isEmpty()) {
                Log.e(TAG, "API returned empty choices list")
                throw Exception("AI 返回为空，请重试")
            }

            val content = choices.firstOrNull()?.message?.content
            if (content.isNullOrBlank()) {
                Log.e(TAG, "API returned empty content")
                throw Exception("AI 返回内容解析失败")
            }

            Log.d(TAG, "Received content length: ${content.length}")

            val summary = content
            val advice: String? = null

            val updatedEntry = entry.copy(
                summary = summary,
                advice = advice,
                updatedAt = LocalDateTime.now()
            )
            diaryRepository.save(updatedEntry)
            Log.d(TAG, "Saved updated entry with summary, id: ${updatedEntry.id}")
            updatedEntry
        }
    }
}
