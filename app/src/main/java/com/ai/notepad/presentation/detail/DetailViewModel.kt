package com.ai.notepad.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.domain.usecase.SaveDiaryUseCase
import com.ai.notepad.domain.usecase.SummarizeDailyUseCase
import com.ai.notepad.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val summarizeDailyUseCase: SummarizeDailyUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "DetailViewModel"
    }

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: 0L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "DetailViewModel initialized with entryId: $entryId")
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            Log.d(TAG, "Loading entry for id: $entryId")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val yearMonth = YearMonth.now()
                val entries = diaryRepository.getEntriesByMonth(yearMonth)
                val entry = entries.find { it.id == entryId }
                if (entry != null) {
                    Log.d(TAG, "Found entry: id=${entry.id}, date=${entry.date}")
                } else {
                    Log.d(TAG, "Entry not found, creating new one with id: $entryId")
                }

                val loadedEntry = entry ?: DiaryEntry(
                    id = entryId,
                    date = java.time.LocalDate.now(),
                    originalText = "",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        diaryEntry = loadedEntry,
                        editedText = loadedEntry.originalText,
                        editedTags = loadedEntry.tags
                    )
                }
                Log.d(TAG, "Entry loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load entry", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateText(text: String) {
        _uiState.update { it.copy(editedText = text) }
    }

    fun updateTags(tags: List<String>) {
        _uiState.update { it.copy(editedTags = tags) }
    }

    fun save() {
        viewModelScope.launch {
            val currentEntry = _uiState.value.diaryEntry ?: run {
                Log.w(TAG, "Cannot save: currentEntry is null")
                return@launch
            }
            Log.d(TAG, "Saving entry: id=${currentEntry.id}")
            _uiState.update { it.copy(isSaving = true) }

            try {
                val updatedEntry = currentEntry.copy(
                    originalText = _uiState.value.editedText,
                    tags = _uiState.value.editedTags,
                    updatedAt = LocalDateTime.now()
                )
                saveDiaryUseCase(updatedEntry)
                Log.d(TAG, "Entry saved successfully")
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save entry", e)
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun summarize() {
        viewModelScope.launch {
            val currentEntry = _uiState.value.diaryEntry ?: run {
                Log.w(TAG, "Cannot summarize: currentEntry is null")
                return@launch
            }
            Log.d(TAG, "Starting AI summarization for entry: id=${currentEntry.id}")
            _uiState.update { it.copy(isAiProcessing = true) }

            // 先保存当前内容
            save()

            when (val result = summarizeDailyUseCase(currentEntry.id)) {
                is Result.Success -> {
                    Log.d(TAG, "AI summarization successful")
                    _uiState.update {
                        it.copy(
                            isAiProcessing = false,
                            diaryEntry = result.data,
                            editedText = result.data.originalText,
                            editedTags = result.data.tags
                        )
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "AI summarization failed: ${result.exception.message}")
                    _uiState.update {
                        it.copy(isAiProcessing = false, error = result.exception.message)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
