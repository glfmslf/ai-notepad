package com.ai.notepad.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.domain.usecase.GetTodayDiaryUseCase
import com.ai.notepad.domain.usecase.SaveDiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayDiaryUseCase: GetTodayDiaryUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "HomeViewModel initialized")
        loadData()
    }

    fun loadData() {
        Log.d(TAG, "Loading data...")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val todayEntry = getTodayDiaryUseCase.getOrCreate()
                Log.d(TAG, "Today's entry loaded: id=${todayEntry.id}, date=${todayEntry.date}")
                _uiState.update { it.copy(todayEntry = todayEntry, isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load today's entry", e)
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }

        viewModelScope.launch {
            getTodayDiaryUseCase().collect { entry ->
                Log.d(TAG, "Today's entry updated: id=${entry?.id}")
                _uiState.update { it.copy(todayEntry = entry) }
            }
        }

        viewModelScope.launch {
            diaryRepository.getRecentEntries(20, 0).collect { entries ->
                Log.d(TAG, "Recent entries updated: count=${entries.size}")
                _uiState.update { it.copy(recentEntries = entries) }
            }
        }

        viewModelScope.launch {
            diaryRepository.getMonthlySummaries().collect { summaries ->
                Log.d(TAG, "Monthly summaries updated: count=${summaries.size}")
                _uiState.update { it.copy(monthlySummaries = summaries) }
            }
        }
    }

    fun createTodayEntry(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "Creating today's entry")
            val today = LocalDate.now()
            val newEntry = DiaryEntry(
                date = today,
                originalText = "",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            val id = saveDiaryUseCase(newEntry)
            Log.d(TAG, "Today's entry created with id: $id")
            loadData()
            onCreated(id)
        }
    }
}
