package com.ai.notepad.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_configs")
data class PromptConfigEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "use_default_daily")
    val useDefaultDaily: Boolean = true,

    @ColumnInfo(name = "custom_daily_prompt")
    val customDailyPrompt: String? = null,

    @ColumnInfo(name = "use_default_monthly")
    val useDefaultMonthly: Boolean = true,

    @ColumnInfo(name = "custom_monthly_prompt")
    val customMonthlyPrompt: String? = null
)
