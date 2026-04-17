package com.ai.notepad.presentation.home

import com.ai.notepad.domain.model.DiaryEntry

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayEntry: DiaryEntry? = null,
    val recentEntries: List<DiaryEntry> = emptyList(),
    val monthlySummaries: List<DiaryEntry> = emptyList(),
    val error: String? = null
)
