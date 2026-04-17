package com.ai.notepad.data.repository

import com.ai.notepad.data.local.dao.DiaryDao
import com.ai.notepad.data.local.entity.DiaryEntryEntity
import com.ai.notepad.domain.model.DiaryEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun observeByDate(date: LocalDate): Flow<DiaryEntry?> {
        return diaryDao.observeByDate(date.format(dateFormatter)).map { it?.toDomain() }
    }

    override fun getRecentEntries(limit: Int, offset: Int): Flow<List<DiaryEntry>> {
        return diaryDao.getRecentEntries(limit, offset).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getMonthlySummaries(): Flow<List<DiaryEntry>> {
        return diaryDao.getMonthlySummaries().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getByDate(date: LocalDate): DiaryEntry? {
        return diaryDao.getByDate(date.format(dateFormatter))?.toDomain()
    }

    override suspend fun getMonthlySummaryByMonth(yearMonth: YearMonth): DiaryEntry? {
        return diaryDao.getMonthlySummaryByMonth(yearMonth.toString())?.toDomain()
    }

    override suspend fun getEntriesByMonth(yearMonth: YearMonth): List<DiaryEntry> {
        return diaryDao.getEntriesByMonth(yearMonth.toString()).map { it.toDomain() }
    }

    override suspend fun save(entry: DiaryEntry): Long {
        return diaryDao.insert(entry.toEntity())
    }

    override suspend fun delete(id: Long) {
        diaryDao.deleteById(id)
    }

    private fun DiaryEntryEntity.toDomain(): DiaryEntry {
        val tagsType = object : TypeToken<List<String>>() {}.type
        return DiaryEntry(
            id = id,
            date = LocalDate.parse(date, dateFormatter),
            originalText = originalText,
            summary = summary,
            advice = advice,
            tags = try { gson.fromJson(tags, tagsType) } catch (e: Exception) { emptyList() },
            isMonthlySummary = isMonthlySummary,
            month = month,
            createdAt = LocalDateTime.ofEpochSecond(createdAt, 0, ZoneOffset.UTC),
            updatedAt = LocalDateTime.ofEpochSecond(updatedAt, 0, ZoneOffset.UTC)
        )
    }

    private fun DiaryEntry.toEntity(): DiaryEntryEntity {
        return DiaryEntryEntity(
            id = id,
            date = date.format(dateFormatter),
            originalText = originalText,
            summary = summary,
            advice = advice,
            tags = gson.toJson(tags),
            isMonthlySummary = isMonthlySummary,
            month = month,
            createdAt = createdAt.toEpochSecond(ZoneOffset.UTC),
            updatedAt = updatedAt.toEpochSecond(ZoneOffset.UTC)
        )
    }
}
