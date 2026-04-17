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
import java.time.YearMonth
import javax.inject.Inject

class SummarizeMonthlyUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val promptRepository: PromptRepository,
    private val apiService: MiniMaxApiService,
    private val apiKeyManager: ApiKeyManager
) {
    companion object {
        private const val TAG = "SummarizeMonthlyUseCase"
    }

    suspend operator fun invoke(yearMonth: YearMonth): Result<DiaryEntry> {
        Log.d(TAG, "Starting monthly summarization for: $yearMonth")

        if (!apiKeyManager.hasApiKey()) {
            Log.w(TAG, "API Key not configured")
            return Result.Error(Exception("API Key 未配置，请在设置中配置"))
        }

        val entries = diaryRepository.getEntriesByMonth(yearMonth)
        Log.d(TAG, "Found ${entries.size} entries for month $yearMonth")

        if (entries.isEmpty()) {
            Log.w(TAG, "No entries found for month $yearMonth")
            return Result.Error(Exception("本月没有日记"))
        }

        val formattedEntries = entries.joinToString("\n---\n") { entry ->
            "${entry.date}: ${entry.originalText}"
        }
        Log.d(TAG, "Formatted entries length: ${formattedEntries.length}")

        val promptConfig = promptRepository.get()
        val prompt = PromptTemplate.getMonthlyPrompt(promptConfig, formattedEntries)
        Log.d(TAG, "Generated prompt length: ${prompt.length}")

        val request = ChatCompletionRequest(
            model = "MiniMax-M2.7-highspeed",
            messages = listOf(
                MessageRequest(role = "user", content = prompt)
            )
        )

        return safeApiCall {
            val apiKey = apiKeyManager.getApiKey() ?: throw Exception("API Key 不存在")
            Log.d(TAG, "Calling MiniMax API for monthly summary...")

            val response = apiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            Log.d(TAG, "API Response id: ${response.id}, choices count: ${response.choices?.size ?: 0}")

            val choices = response.choices
            if (choices.isNullOrEmpty()) {
                Log.e(TAG, "API returned empty choices")
                throw Exception("API 请求失败，请检查 API Key 是否有效")
            }

            val content = choices.firstOrNull()?.message?.content
            if (content.isNullOrBlank()) {
                Log.e(TAG, "API returned empty content")
                throw Exception("AI 返回内容解析失败")
            }

            Log.d(TAG, "Received content length: ${content.length}")

            val monthlyEntry = DiaryEntry(
                date = yearMonth.atDay(1),
                originalText = formattedEntries,
                summary = content,
                isMonthlySummary = true,
                month = yearMonth.toString(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            diaryRepository.save(monthlyEntry)
            Log.d(TAG, "Saved monthly summary entry, id: ${monthlyEntry.id}")
            monthlyEntry
        }
    }
}
