package com.ai.notepad.presentation.monthly

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.domain.model.DiaryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MonthlyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val month: String = savedStateHandle.get<String>("month") ?: ""

    private val _uiState = MutableStateFlow(MonthlyUiState())
    val uiState: StateFlow<MonthlyUiState> = _uiState.asStateFlow()

    init {
        loadMonthlySummary()
    }

    private fun loadMonthlySummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val yearMonth = YearMonth.parse(month)
                val entry = diaryRepository.getMonthlySummaryByMonth(yearMonth)
                _uiState.update { it.copy(isLoading = false, monthlyEntry = entry) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
