package com.ai.notepad.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class DiaryEntry(
    val id: Long = 0,
    val date: LocalDate,
    val originalText: String,
    val summary: String? = null,
    val advice: String? = null,
    val tags: List<String> = emptyList(),
    val isMonthlySummary: Boolean = false,
    val month: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
