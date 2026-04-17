package com.ai.notepad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ai.notepad.data.local.entity.PromptConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptConfigDao {

    @Query("SELECT * FROM prompt_configs WHERE id = 1 LIMIT 1")
    fun observe(): Flow<PromptConfigEntity?>

    @Query("SELECT * FROM prompt_configs WHERE id = 1 LIMIT 1")
    suspend fun get(): PromptConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: PromptConfigEntity)

    @Query("DELETE FROM prompt_configs")
    suspend fun deleteAll()
}
