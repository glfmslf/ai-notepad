package com.ai.notepad.data.repository

import com.ai.notepad.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface DiaryRepository {
    fun observeByDate(date: LocalDate): Flow<DiaryEntry?>
    fun getRecentEntries(limit: Int, offset: Int): Flow<List<DiaryEntry>>
    fun getMonthlySummaries(): Flow<List<DiaryEntry>>
    suspend fun getByDate(date: LocalDate): DiaryEntry?
    suspend fun getMonthlySummaryByMonth(yearMonth: YearMonth): DiaryEntry?
    suspend fun getEntriesByMonth(yearMonth: YearMonth): List<DiaryEntry>
    suspend fun save(entry: DiaryEntry): Long
    suspend fun delete(id: Long)
}
