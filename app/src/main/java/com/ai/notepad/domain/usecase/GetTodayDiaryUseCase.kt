package com.ai.notepad.domain.usecase

import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class GetTodayDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(): Flow<DiaryEntry?> {
        return diaryRepository.observeByDate(LocalDate.now())
    }

    suspend fun getOrCreate(): DiaryEntry {
        val today = LocalDate.now()
        return diaryRepository.getByDate(today) ?: DiaryEntry(
            date = today,
            originalText = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
