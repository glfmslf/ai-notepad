package com.ai.notepad.di

import android.content.Context
import androidx.room.Room
import com.ai.notepad.data.local.AppDatabase
import com.ai.notepad.data.local.dao.DiaryDao
import com.ai.notepad.data.local.dao.PromptConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ai_notepad_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDiaryDao(database: AppDatabase): DiaryDao {
        return database.diaryDao()
    }

    @Provides
    @Singleton
    fun providePromptConfigDao(database: AppDatabase): PromptConfigDao {
        return database.promptConfigDao()
    }
}
