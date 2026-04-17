package com.ai.notepad.presentation.detail

import com.ai.notepad.domain.model.DiaryEntry

data class DetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isAiProcessing: Boolean = false,
    val diaryEntry: DiaryEntry? = null,
    val editedText: String = "",
    val editedTags: List<String> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false
)
