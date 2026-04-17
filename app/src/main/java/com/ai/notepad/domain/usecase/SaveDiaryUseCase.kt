package com.ai.notepad.domain.usecase

import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.domain.model.DiaryEntry
import java.time.LocalDateTime
import javax.inject.Inject

class SaveDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(entry: DiaryEntry): Long {
        val updatedEntry = entry.copy(updatedAt = LocalDateTime.now())
        return diaryRepository.save(updatedEntry)
    }
}
