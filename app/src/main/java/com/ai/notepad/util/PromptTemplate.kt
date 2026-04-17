package com.ai.notepad.util

import com.ai.notepad.domain.model.PromptConfig

object PromptTemplate {

    fun getDailyPrompt(config: PromptConfig, diaryContent: String): String {
        val template = if (config.useDefaultDaily) {
            PromptConfig.DEFAULT_DAILY_PROMPT
        } else {
            config.customDailyPrompt ?: PromptConfig.DEFAULT_DAILY_PROMPT
        }
        return template.replace("{{diary_content}}", diaryContent)
    }

    fun getMonthlyPrompt(config: PromptConfig, monthlyEntries: String): String {
        val template = if (config.useDefaultMonthly) {
            PromptConfig.DEFAULT_MONTHLY_PROMPT
        } else {
            config.customMonthlyPrompt ?: PromptConfig.DEFAULT_MONTHLY_PROMPT
        }
        return template.replace("{{monthly_entries}}", monthlyEntries)
    }
}
