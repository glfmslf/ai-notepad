package com.ai.notepad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ai.notepad.data.local.converter.Converters
import com.ai.notepad.data.local.dao.DiaryDao
import com.ai.notepad.data.local.dao.PromptConfigDao
import com.ai.notepad.data.local.entity.DiaryEntryEntity
import com.ai.notepad.data.local.entity.PromptConfigEntity

@Database(
    entities = [DiaryEntryEntity::class, PromptConfigEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun promptConfigDao(): PromptConfigDao
}
