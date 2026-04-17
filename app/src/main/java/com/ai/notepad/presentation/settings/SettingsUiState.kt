package com.ai.notepad.presentation.settings

import com.ai.notepad.domain.model.PromptConfig

data class SettingsUiState(
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isGeneratingSummary: Boolean = false,
    val error: String? = null,
    val promptConfig: PromptConfig = PromptConfig(),
    val dailyPromptText: String = "",
    val monthlyPromptText: String = ""
)
