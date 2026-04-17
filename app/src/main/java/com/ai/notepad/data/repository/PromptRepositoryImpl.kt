package com.ai.notepad.data.repository

import com.ai.notepad.data.local.dao.PromptConfigDao
import com.ai.notepad.data.local.entity.PromptConfigEntity
import com.ai.notepad.domain.model.PromptConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRepositoryImpl @Inject constructor(
    private val promptConfigDao: PromptConfigDao
) : PromptRepository {

    override fun observe(): Flow<PromptConfig> {
        return promptConfigDao.observe().map { entity ->
            entity?.toDomain() ?: PromptConfig()
        }
    }

    override suspend fun get(): PromptConfig {
        return promptConfigDao.get()?.toDomain() ?: PromptConfig()
    }

    override suspend fun save(config: PromptConfig) {
        promptConfigDao.insert(config.toEntity())
    }

    private fun PromptConfigEntity.toDomain(): PromptConfig {
        return PromptConfig(
            useDefaultDaily = useDefaultDaily,
            customDailyPrompt = customDailyPrompt,
            useDefaultMonthly = useDefaultMonthly,
            customMonthlyPrompt = customMonthlyPrompt
        )
    }

    private fun PromptConfig.toEntity(): PromptConfigEntity {
        return PromptConfigEntity(
            id = 1,
            useDefaultDaily = useDefaultDaily,
            customDailyPrompt = customDailyPrompt,
            useDefaultMonthly = useDefaultMonthly,
            customMonthlyPrompt = customMonthlyPrompt
        )
    }
}
