package com.ai.notepad.data.repository

import com.ai.notepad.domain.model.PromptConfig
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    fun observe(): Flow<PromptConfig>
    suspend fun get(): PromptConfig
    suspend fun save(config: PromptConfig)
}
