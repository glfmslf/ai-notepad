package com.ai.notepad.presentation.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.notepad.data.repository.PromptRepository
import com.ai.notepad.domain.model.PromptConfig
import com.ai.notepad.domain.usecase.SummarizeMonthlyUseCase
import com.ai.notepad.util.ApiKeyManager
import com.ai.notepad.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyManager: ApiKeyManager,
    private val summarizeMonthlyUseCase: SummarizeMonthlyUseCase,
    private val promptRepository: PromptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadApiKeyStatus()
        loadPromptConfig()
    }

    private fun loadApiKeyStatus() {
        Log.d("SettingsViewModel", "Loading API key status")
        _uiState.update {
            it.copy(hasApiKey = apiKeyManager.hasApiKey())
        }
    }

    private fun loadPromptConfig() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Loading prompt config")
            try {
                val config = promptRepository.get()
                _uiState.update {
                    it.copy(
                        promptConfig = config,
                        dailyPromptText = config.customDailyPrompt ?: PromptConfig.DEFAULT_DAILY_PROMPT,
                        monthlyPromptText = config.customMonthlyPrompt ?: PromptConfig.DEFAULT_MONTHLY_PROMPT
                    )
                }
                Log.d("SettingsViewModel", "Prompt config loaded: useDefaultDaily=${config.useDefaultDaily}")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to load prompt config", e)
            }
        }
    }

    fun saveApiKey(apiKey: String) {
        Log.d("SettingsViewModel", "Saving API key")
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                apiKeyManager.saveApiKey(apiKey)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true, hasApiKey = true) }
                Log.d("SettingsViewModel", "API key saved successfully")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to save API key", e)
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearApiKey() {
        Log.d("SettingsViewModel", "Clearing API key")
        apiKeyManager.clearApiKey()
        _uiState.update { it.copy(hasApiKey = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun generateMonthlySummary() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Generating monthly summary")
            _uiState.update { it.copy(isGeneratingSummary = true) }
            val currentMonth = YearMonth.now()
            when (val result = summarizeMonthlyUseCase(currentMonth)) {
                is Result.Success -> {
                    Log.d("SettingsViewModel", "Monthly summary generated successfully")
                    _uiState.update { it.copy(isGeneratingSummary = false, error = null) }
                }
                is Result.Error -> {
                    Log.e("SettingsViewModel", "Failed to generate monthly summary: ${result.exception.message}")
                    _uiState.update { it.copy(isGeneratingSummary = false, error = result.exception.message) }
                }
            }
        }
    }

    fun updateDailyPrompt(text: String) {
        _uiState.update { it.copy(dailyPromptText = text) }
    }

    fun updateMonthlyPrompt(text: String) {
        _uiState.update { it.copy(monthlyPromptText = text) }
    }

    fun savePromptConfig() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Saving prompt config")
            try {
                val currentState = _uiState.value
                val config = PromptConfig(
                    useDefaultDaily = false,
                    customDailyPrompt = currentState.dailyPromptText,
                    useDefaultMonthly = false,
                    customMonthlyPrompt = currentState.monthlyPromptText
                )
                promptRepository.save(config)
                _uiState.update { it.copy(saveSuccess = true, promptConfig = config) }
                Log.d("SettingsViewModel", "Prompt config saved successfully")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to save prompt config", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Resetting to default prompts")
            try {
                val config = PromptConfig()
                promptRepository.save(config)
                _uiState.update {
                    it.copy(
                        promptConfig = config,
                        dailyPromptText = PromptConfig.DEFAULT_DAILY_PROMPT,
                        monthlyPromptText = PromptConfig.DEFAULT_MONTHLY_PROMPT
                    )
                }
                Log.d("SettingsViewModel", "Reset to default prompts successfully")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to reset prompts", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
