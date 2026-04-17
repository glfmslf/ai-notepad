package com.ai.notepad.di

import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.data.repository.DiaryRepositoryImpl
import com.ai.notepad.data.repository.PromptRepository
import com.ai.notepad.data.repository.PromptRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindPromptRepository(impl: PromptRepositoryImpl): PromptRepository
}
