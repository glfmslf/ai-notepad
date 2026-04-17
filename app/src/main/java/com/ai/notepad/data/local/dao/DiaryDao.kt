package com.ai.notepad.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ai.notepad.data.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entries WHERE date = :date AND is_monthly_summary = 0 LIMIT 1")
    suspend fun getByDate(date: String): DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries WHERE date = :date AND is_monthly_summary = 0 LIMIT 1")
    fun observeByDate(date: String): Flow<DiaryEntryEntity?>

    @Query("SELECT * FROM diary_entries WHERE is_monthly_summary = 0 ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getRecentEntries(limit: Int, offset: Int): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE is_monthly_summary = 1 ORDER BY month DESC")
    fun getMonthlySummaries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE month = :month AND is_monthly_summary = 1 LIMIT 1")
    suspend fun getMonthlySummaryByMonth(month: String): DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries WHERE date LIKE :monthPrefix || '%' AND is_monthly_summary = 0 ORDER BY date ASC")
    suspend fun getEntriesByMonth(monthPrefix: String): List<DiaryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntryEntity): Long

    @Update
    suspend fun update(entry: DiaryEntryEntity)

    @Delete
    suspend fun delete(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
