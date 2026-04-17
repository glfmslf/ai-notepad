package com.ai.notepad.presentation.monthly

import com.ai.notepad.domain.model.DiaryEntry

data class MonthlyUiState(
    val isLoading: Boolean = true,
    val monthlyEntry: DiaryEntry? = null,
    val error: String? = null
)
