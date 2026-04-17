# AI 笔记本 - 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个 AI 笔记本 Android 应用，支持每日记录、语音输入、AI 总结、月度复盘

**Architecture:** Clean Architecture + MVVM，Jetpack Compose UI，Room 本地存储，MiniMax API 云端 AI

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Retrofit, Whisper (语音), EncryptedSharedPreferences

---

## 文件结构概览

```
app/src/main/java/com/ai/notepad/
├── AiNotepadApplication.kt          # Application 类
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room Database
│   │   ├── dao/
│   │   │   ├── DiaryDao.kt        # 日记 DAO
│   │   │   └── PromptConfigDao.kt # Prompt 配置 DAO
│   │   ├── entity/
│   │   │   ├── DiaryEntryEntity.kt
│   │   │   └── PromptConfigEntity.kt
│   │   └── converter/
│   │       └── Converters.kt       # TypeConverter
│   ├── remote/
│   │   ├── api/
│   │   │   └── MiniMaxApiService.kt
│   │   └── model/
│   │       └── ChatCompletionModels.kt
│   └── repository/
│       ├── DiaryRepository.kt
│       ├── DiaryRepositoryImpl.kt
│       ├── PromptRepository.kt
│       └── PromptRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── DiaryEntry.kt
│   │   └── PromptConfig.kt
│   └── usecase/
│       ├── GetTodayDiaryUseCase.kt
│       ├── SaveDiaryUseCase.kt
│       ├── SummarizeDailyUseCase.kt
│       └── SummarizeMonthlyUseCase.kt
├── presentation/
│   ├── MainActivity.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Space.kt
│   │   └── Theme.kt
│   ├── components/
│   │   ├── DiaryCard.kt
│   │   ├── MonthlySummaryCard.kt
│   │   ├── TagChip.kt
│   │   └── LoadingIndicator.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeUiState.kt
│   ├── detail/
│   │   ├── DetailScreen.kt
│   │   ├── DetailViewModel.kt
│   │   └── DetailUiState.kt
│   ├── monthly/
│   │   ├── MonthlySummaryScreen.kt
│   │   ├── MonthlyViewModel.kt
│   │   └── MonthlyUiState.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       ├── SettingsViewModel.kt
│       └── SettingsUiState.kt
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
└── util/
    ├── ApiKeyManager.kt
    ├── DateUtils.kt
    ├── PromptTemplate.kt
    └── Result.kt
```

---

## Phase 1: 项目骨架 (P0)

### Task 1: 创建项目结构

**Files:**
- Create: `app/build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/colors.xml`

- [ ] **Step 1: 创建根目录 gradle 文件**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "AiNotepad"
include(":app")
```

- [ ] **Step 2: 创建 app/build.gradle.kts**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ai.notepad"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ai.notepad"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

- [ ] **Step 3: 创建 gradle.properties**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: 创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application
        android:name=".AiNotepadApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AiNotepad">
        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AiNotepad">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 5: 创建 strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">AI 笔记本</string>
</resources>
```

- [ ] **Step 6: 创建 colors.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: create project skeleton with Gradle configuration"
```

---

### Task 2: 创建 Application 类和 Hilt 基础

**Files:**
- Create: `app/src/main/java/com/ai/notepad/AiNotepadApplication.kt`
- Create: `app/src/main/java/com/ai/notepad/di/AppModule.kt`
- Create: `app/src/main/java/com/ai/notepad/presentation/MainActivity.kt`

- [ ] **Step 1: 创建 Application 类**

```kotlin
package com.ai.notepad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AiNotepadApplication : Application()
```

- [ ] **Step 2: 创建 AppModule**

```kotlin
package com.ai.notepad.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
```

- [ ] **Step 3: 创建 MainActivity**

```kotlin
package com.ai.notepad.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ai.notepad.presentation.theme.AiNotepadTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiNotepadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Navigation will be added here
                }
            }
        }
    }
}
```

- [ ] **Step 4: 创建基础 Theme**

```kotlin
package com.ai.notepad.presentation.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF6366F1)
val PrimaryVariant = Color(0xFF4F46E5)
val Secondary = Color(0xFF10B981)
val SecondaryVariant = Color(0xFF059669)

val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF3F4F6)

val OnPrimary = Color.White
val OnSurface = Color(0xFF1F2937)
val OnSurfaceVariant = Color(0xFF6B7280)

val Error = Color(0xFFEF4444)
val Warning = Color(0xFFF59E0B)
val Success = Color(0xFF10B981)
```

```kotlin
package com.ai.notepad.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
)
```

```kotlin
package com.ai.notepad.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnPrimary,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error
)

@Composable
fun AiNotepadTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 5: 创建 Space 常量**

```kotlin
package com.ai.notepad.presentation.theme

import androidx.compose.ui.unit.dp

object Space {
    val Space2 = 2.dp
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space24 = 24.dp
    val Space32 = 32.dp
    val Space48 = 48.dp
}
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add Application, Hilt modules, MainActivity and theme"
```

---

## Phase 2: 数据层 - Room 数据库 (P0)

### Task 3: 创建 Room Entity 和 DAO

**Files:**
- Create: `app/src/main/java/com/ai/notepad/data/local/entity/DiaryEntryEntity.kt`
- Create: `app/src/main/java/com/ai/notepad/data/local/entity/PromptConfigEntity.kt`
- Create: `app/src/main/java/com/ai/notepad/data/local/converter/Converters.kt`
- Create: `app/src/main/java/com/ai/notepad/data/local/dao/DiaryDao.kt`
- Create: `app/src/main/java/com/ai/notepad/data/local/dao/PromptConfigDao.kt`
- Create: `app/src/main/java/com/ai/notepad/data/local/AppDatabase.kt`

- [ ] **Step 1: 创建 DiaryEntryEntity**

```kotlin
package com.ai.notepad.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "original_text")
    val originalText: String,

    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "advice")
    val advice: String? = null,

    @ColumnInfo(name = "tags")
    val tags: String = "[]",

    @ColumnInfo(name = "is_monthly_summary")
    val isMonthlySummary: Boolean = false,

    @ColumnInfo(name = "month")
    val month: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
```

- [ ] **Step 2: 创建 PromptConfigEntity**

```kotlin
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
```

- [ ] **Step 3: 创建 TypeConverters**

```kotlin
package com.ai.notepad.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

- [ ] **Step 4: 创建 DiaryDao**

```kotlin
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
```

- [ ] **Step 5: 创建 PromptConfigDao**

```kotlin
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
```

- [ ] **Step 6: 创建 AppDatabase**

```kotlin
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
```

- [ ] **Step 7: 创建 DatabaseModule**

```kotlin
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
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: add Room entities, DAOs and database"
```

---

### Task 4: 创建 Domain 模型和 Repository

**Files:**
- Create: `app/src/main/java/com/ai/notepad/domain/model/DiaryEntry.kt`
- Create: `app/src/main/java/com/ai/notepad/domain/model/PromptConfig.kt`
- Create: `app/src/main/java/com/ai/notepad/data/repository/DiaryRepository.kt`
- Create: `app/src/main/java/com/ai/notepad/data/repository/DiaryRepositoryImpl.kt`
- Create: `app/src/main/java/com/ai/notepad/data/repository/PromptRepository.kt`
- Create: `app/src/main/java/com/ai/notepad/data/repository/PromptRepositoryImpl.kt`

- [ ] **Step 1: 创建 DiaryEntry Domain Model**

```kotlin
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
```

- [ ] **Step 2: 创建 PromptConfig Domain Model**

```kotlin
package com.ai.notepad.domain.model

data class PromptConfig(
    val useDefaultDaily: Boolean = true,
    val customDailyPrompt: String? = null,
    val useDefaultMonthly: Boolean = true,
    val customMonthlyPrompt: String? = null
) {
    companion object {
        const val DEFAULT_DAILY_PROMPT = """【系统提示】
你是一位温和而专业的个人成长教练。你擅长从日常记录中提炼精华，用温暖而简洁的语言给出洞见。你的回复应该：层次分明、易于阅读、洞察深刻但不说教。

【用户消息】
请分析以下今日日记，进行全面复盘。

【日记内容】
{{diary_content}}

【输出要求】
1. 一句话总结（20字以内，精辟概括今天）
2. 做得好的一面（2-3条，指出具体做得好的地方）
3. 做得不好的地方（1-2条，说明原因）
4. 具体可行的行动建议（2-3条）

【输出格式】（严格按此格式返回，不要多余内容）
📝 今日总结
（一句话总结今天）

✅ 做得好
• 好的地方1
• 好的地方2
（如有）

❌ 需要改进
• 不足之处1
• 不足之处2
（如有）

💡 行动建议
→ 建议1
→ 建议2
"""

        const val DEFAULT_MONTHLY_PROMPT = """【系统提示】
你是一位专业的月度复盘教练。你擅长从整月的日记中提炼成长轨迹，找出模式，给予鼓励。你应该：总结有力但不高高在上，建议具体但不强制，用正面的视角帮助用户看到进步。

【用户消息】
请对用户整个月的日记进行月度全面复盘。

【本月日记】
{{monthly_entries}}

【输出要求】
1. 本月一句话概述（20字以内，精辟总结这个月）
2. 本月做得好的方面（3条，指出具体做得好的地方）
3. 本月做得不好的方面（2-3条，说明原因和影响）
4. 下月具体可行的行动建议（3条）

【输出格式】（严格按此格式返回）
✨ 本月概述
（一句话总结）

🌟 做得好
• 好的地方1
• 好的地方2
• 好的地方3

📈 需要改进
• 不足之处1 + 原因分析
• 不足之处2 + 原因分析
（如有）

🎯 下月行动
→ 行动1
→ 行动2
→ 行动3"""
    }
}
```

- [ ] **Step 3: 创建 DiaryRepository 接口**

```kotlin
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
```

- [ ] **Step 4: 创建 DiaryRepositoryImpl**

```kotlin
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
            createdAt = LocalDateTime.ofEpochSecond(createdAt, 0, java.time.ZoneOffset.UTC),
            updatedAt = LocalDateTime.ofEpochSecond(updatedAt, 0, java.time.ZoneOffset.UTC)
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
            createdAt = createdAt.toEpochSecond(java.time.ZoneOffset.UTC),
            updatedAt = updatedAt.toEpochSecond(java.time.ZoneOffset.UTC)
        )
    }
}
```

- [ ] **Step 5: 创建 PromptRepository 接口**

```kotlin
package com.ai.notepad.data.repository

import com.ai.notepad.domain.model.PromptConfig
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    fun observe(): Flow<PromptConfig>
    suspend fun get(): PromptConfig
    suspend fun save(config: PromptConfig)
}
```

- [ ] **Step 6: 创建 PromptRepositoryImpl**

```kotlin
package com.ai.notepad.data.repository

import com.ai.notepad.data.local.dao.PromptConfigDao
import com.ai.notepad.data.local.entity.PromptConfigEntity
import com.ai.notepad.domain.model.PromptConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRepositoryImpl @Inject constructor(
    private val promptConfigDao: PromptConfigDao
) : PromptRepository {

    override fun observe(): Flow<PromptConfig> {
        return promptConfigDao.observe().map { entity ->
            entity?.toDomain() ?: PromptConfig()
        }
    }

    override suspend fun get(): PromptConfig {
        return promptConfigDao.get()?.toDomain() ?: PromptConfig()
    }

    override suspend fun save(config: PromptConfig) {
        promptConfigDao.insert(config.toEntity())
    }

    private fun PromptConfigEntity.toDomain(): PromptConfig {
        return PromptConfig(
            useDefaultDaily = useDefaultDaily,
            customDailyPrompt = customDailyPrompt,
            useDefaultMonthly = useDefaultMonthly,
            customMonthlyPrompt = customMonthlyPrompt
        )
    }

    private fun PromptConfig.toEntity(): PromptConfigEntity {
        return PromptConfigEntity(
            id = 1,
            useDefaultDaily = useDefaultDaily,
            customDailyPrompt = customDailyPrompt,
            useDefaultMonthly = useDefaultMonthly,
            customMonthlyPrompt = customMonthlyPrompt
        )
    }
}
```

- [ ] **Step 7: 创建 RepositoryModule**

```kotlin
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
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: add domain models and repository layer"
```

---

## Phase 3: AI 层 - MiniMax API 集成 (P0)

### Task 5: 创建 API Service 和网络模块

**Files:**
- Create: `app/src/main/java/com/ai/notepad/data/remote/model/ChatCompletionModels.kt`
- Create: `app/src/main/java/com/ai/notepad/data/remote/api/MiniMaxApiService.kt`
- Create: `app/src/main/java/com/ai/notepad/util/ApiKeyManager.kt`
- Create: `app/src/main/java/com/ai/notepad/di/NetworkModule.kt`

- [ ] **Step 1: 创建 ChatCompletion Models**

```kotlin
package com.ai.notepad.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatCompletionRequest(
    val model: String = "MiniMax-M2.7-highspeed",
    val messages: List<MessageRequest>,
    val stream: Boolean = false
)

data class MessageRequest(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val finish_reason: String,
    val index: Int,
    val message: Message
)

data class Message(
    val role: String,
    val content: String
)

data class Usage(
    @SerializedName("total_tokens")
    val totalTokens: Int
)
```

- [ ] **Step 2: 创建 MiniMaxApiService**

```kotlin
package com.ai.notepad.data.remote.api

import com.ai.notepad.data.remote.model.ChatCompletionRequest
import com.ai.notepad.data.remote.model.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MiniMaxApiService {

    @POST("/v1/text/chatcompletion_v2")
    suspend fun chatCompletion(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
```

- [ ] **Step 3: 创建 ApiKeyManager**

```kotlin
package com.ai.notepad.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String) {
        encryptedPrefs.edit().putString(KEY_API_KEY, key).apply()
    }

    fun getApiKey(): String? {
        return encryptedPrefs.getString(KEY_API_KEY, null)
    }

    fun hasApiKey(): Boolean = !getApiKey().isNullOrBlank()

    fun clearApiKey() {
        encryptedPrefs.edit().remove(KEY_API_KEY).apply()
    }

    companion object {
        private const val KEY_API_KEY = "minimax_api_key"
    }
}
```

- [ ] **Step 4: 创建 NetworkModule**

```kotlin
package com.ai.notepad.di

import com.ai.notepad.data.remote.api.MiniMaxApiService
import com.ai.notepad.util.ApiKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.minimax.io"

    @Provides
    @Singleton
    fun provideOkHttpClient(apiKeyManager: ApiKeyManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val apiKey = apiKeyManager.getApiKey()
            val newRequest = if (apiKey != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $apiKey")
                    .build()
            } else {
                originalRequest
            }
            chain.proceed(newRequest)
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMiniMaxApiService(retrofit: Retrofit): MiniMaxApiService {
        return retrofit.create(MiniMaxApiService::class.java)
    }
}
```

- [ ] **Step 5: 创建 Result 工具类**

```kotlin
package com.ai.notepad.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }
}

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

- [ ] **Step 6: 创建 PromptTemplate 工具类**

```kotlin
package com.ai.notepad.util

import com.ai.notepad.domain.model.PromptConfig

object PromptTemplate {

    fun getDailyPrompt(config: PromptConfig, diaryContent: String): String {
        val template = if (config.useDefaultDaily) {
            PromptConfig.DEFAULT_DAILY_PROMPT
        } else {
            config.customDailyPrompt ?: PromptConfig.DEFAULT_DAILY_PROMPT
        }
        return template.replace("{{diary_content}}", diaryContent)
    }

    fun getMonthlyPrompt(config: PromptConfig, monthlyEntries: String): String {
        val template = if (config.useDefaultMonthly) {
            PromptConfig.DEFAULT_MONTHLY_PROMPT
        } else {
            config.customMonthlyPrompt ?: PromptConfig.DEFAULT_MONTHLY_PROMPT
        }
        return template.replace("{{monthly_entries}}", monthlyEntries)
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add MiniMax API service and network layer"
```

---

### Task 6: 创建 UseCase

**Files:**
- Create: `app/src/main/java/com/ai/notepad/domain/usecase/GetTodayDiaryUseCase.kt`
- Create: `app/src/main/java/com/ai/notepad/domain/usecase/SaveDiaryUseCase.kt`
- Create: `app/src/main/java/com/ai/notepad/domain/usecase/SummarizeDailyUseCase.kt`
- Create: `app/src/main/java/com/ai/notepad/domain/usecase/SummarizeMonthlyUseCase.kt`

- [ ] **Step 1: 创建 GetTodayDiaryUseCase**

```kotlin
package com.ai.notepad.domain.usecase

import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class GetTodayDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(): Flow<DiaryEntry?> {
        return diaryRepository.observeByDate(LocalDate.now())
    }

    suspend fun getOrCreate(): DiaryEntry {
        val today = LocalDate.now()
        return diaryRepository.getByDate(today) ?: DiaryEntry(
            date = today,
            originalText = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
```

- [ ] **Step 2: 创建 SaveDiaryUseCase**

```kotlin
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
```

- [ ] **Step 3: 创建 SummarizeDailyUseCase**

```kotlin
package com.ai.notepad.domain.usecase

import com.ai.notepad.data.remote.api.MiniMaxApiService
import com.ai.notepad.data.remote.model.ChatCompletionRequest
import com.ai.notepad.data.remote.model.MessageRequest
import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.data.repository.PromptRepository
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.util.ApiKeyManager
import com.ai.notepad.util.PromptTemplate
import com.ai.notepad.util.Result
import com.ai.notepad.util.safeApiCall
import java.time.LocalDateTime
import javax.inject.Inject

class SummarizeDailyUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val promptRepository: PromptRepository,
    private val apiService: MiniMaxApiService,
    private val apiKeyManager: ApiKeyManager
) {
    suspend operator fun invoke(entryId: Long): Result<DiaryEntry> {
        if (!apiKeyManager.hasApiKey()) {
            return Result.Error(Exception("API Key 未配置，请在设置中配置"))
        }

        val entry = diaryRepository.getByDate(
            java.time.LocalDate.now()
        ) ?: return Result.Error(Exception("日记不存在"))

        if (entry.originalText.isBlank()) {
            return Result.Error(Exception("日记内容为空"))
        }

        val promptConfig = promptRepository.get()
        val prompt = PromptTemplate.getDailyPrompt(promptConfig, entry.originalText)

        val request = ChatCompletionRequest(
            model = "MiniMax-M2.7-highspeed",
            messages = listOf(
                MessageRequest(role = "user", content = prompt)
            )
        )

        return safeApiCall {
            val response = apiService.chatCompletion(
                authorization = "Bearer ${apiKeyManager.getApiKey()}",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("AI 返回为空")

            val (summary, advice) = parseAiResponse(content)

            val updatedEntry = entry.copy(
                summary = summary,
                advice = advice,
                updatedAt = LocalDateTime.now()
            )
            diaryRepository.save(updatedEntry)
            updatedEntry
        }
    }

    private fun parseAiResponse(content: String): Pair<String, String?> {
        val lines = content.lines()
        val summaryBuilder = StringBuilder()
        val adviceBuilder = StringBuilder? = null

        var inAdviceSection = false

        for (line in lines) {
            when {
                line.contains("💡") || line.contains("行动建议") -> {
                    inAdviceSection = true
                }
                inAdviceSection && (line.startsWith("→") || line.startsWith("-") || line.startsWith("•")) -> {
                    adviceBuilder?.appendLine(line)
                }
                line.startsWith("•") || line.startsWith("📌") || line.startsWith("•") -> {
                    if (!inAdviceSection) {
                        summaryBuilder.appendLine(line)
                    }
                }
            }
        }

        return summaryBuilder.toString().trim() to adviceBuilder?.toString()?.trim()
    }
}
```

- [ ] **Step 4: 创建 SummarizeMonthlyUseCase**

```kotlin
package com.ai.notepad.domain.usecase

import com.ai.notepad.data.remote.api.MiniMaxApiService
import com.ai.notepad.data.remote.model.ChatCompletionRequest
import com.ai.notepad.data.remote.model.MessageRequest
import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.data.repository.PromptRepository
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.util.ApiKeyManager
import com.ai.notepad.util.PromptTemplate
import com.ai.notepad.util.Result
import com.ai.notepad.util.safeApiCall
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

class SummarizeMonthlyUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val promptRepository: PromptRepository,
    private val apiService: MiniMaxApiService,
    private val apiKeyManager: ApiKeyManager
) {
    suspend operator fun invoke(yearMonth: YearMonth): Result<DiaryEntry> {
        if (!apiKeyManager.hasApiKey()) {
            return Result.Error(Exception("API Key 未配置，请在设置中配置"))
        }

        val entries = diaryRepository.getEntriesByMonth(yearMonth)
        if (entries.isEmpty()) {
            return Result.Error(Exception("本月没有日记"))
        }

        val formattedEntries = entries.joinToString("\n---\n") { entry ->
            "${entry.date}: ${entry.originalText}"
        }

        val promptConfig = promptRepository.get()
        val prompt = PromptTemplate.getMonthlyPrompt(promptConfig, formattedEntries)

        val request = ChatCompletionRequest(
            model = "MiniMax-M2.7-highspeed",
            messages = listOf(
                MessageRequest(role = "user", content = prompt)
            )
        )

        return safeApiCall {
            val response = apiService.chatCompletion(
                authorization = "Bearer ${apiKeyManager.getApiKey()}",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("AI 返回为空")

            val monthlyEntry = DiaryEntry(
                date = yearMonth.atDay(1),
                originalText = formattedEntries,
                summary = content,
                isMonthlySummary = true,
                month = yearMonth.toString(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            diaryRepository.save(monthlyEntry)
            monthlyEntry
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add use cases for diary and AI summarization"
```

---

## Phase 4: 首页 - 卡片列表 (P0)

### Task 7: 创建导航和首页

**Files:**
- Create: `app/src/main/java/com/ai/notepad/presentation/navigation/AppNavigation.kt`
- Create: `app/src/main/java/com/ai/notepad/presentation/home/HomeUiState.kt`
- Create: `app/src/main/java/com/ai/notepad/presentation/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/ai/notepad/presentation/home/HomeScreen.kt`

- [ ] **Step 1: 创建 AppNavigation**

```kotlin
package com.ai.notepad.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{entryId}") {
        fun createRoute(entryId: Long) = "detail/$entryId"
    }
    object Monthly : Screen("monthly/{month}") {
        fun createRoute(month: String) = "monthly/$month"
    }
    object Settings : Screen("settings")
}
```

```kotlin
package com.ai.notepad.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ai.notepad.presentation.detail.DetailScreen
import com.ai.notepad.presentation.home.HomeScreen
import com.ai.notepad.presentation.monthly.MonthlySummaryScreen
import com.ai.notepad.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.Detail.createRoute(entryId))
                },
                onNavigateToMonthly = { month ->
                    navController.navigate(Screen.Monthly.createRoute(month))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            DetailScreen(
                entryId = entryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Monthly.route,
            arguments = listOf(navArgument("month") { type = NavType.StringType })
        ) { backStackEntry ->
            val month = backStackEntry.arguments?.getString("month") ?: ""
            MonthlySummaryScreen(
                month = month,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **Step 2: 创建 HomeUiState**

```kotlin
package com.ai.notepad.presentation.home

import com.ai.notepad.domain.model.DiaryEntry

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayEntry: DiaryEntry? = null,
    val recentEntries: List<DiaryEntry> = emptyList(),
    val monthlySummaries: List<DiaryEntry> = emptyList(),
    val error: String? = null
)
```

- [ ] **Step 3: 创建 HomeViewModel**

```kotlin
package com.ai.notepad.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.domain.usecase.GetTodayDiaryUseCase
import com.ai.notepad.domain.usecase.SaveDiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayDiaryUseCase: GetTodayDiaryUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val todayEntry = getTodayDiaryUseCase.getOrCreate()
                _uiState.update { it.copy(todayEntry = todayEntry, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }

        viewModelScope.launch {
            getTodayDiaryUseCase().collect { entry ->
                _uiState.update { it.copy(todayEntry = entry) }
            }
        }
    }

    fun createTodayEntry() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val newEntry = DiaryEntry(
                date = today,
                originalText = "",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            val id = saveDiaryUseCase(newEntry)
            loadData()
        }
    }
}
```

- [ ] **Step 4: 创建 HomeScreen**

```kotlin
package com.ai.notepad.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai.notepad.presentation.components.DiaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToMonthly: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 笔记本") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    uiState.todayEntry?.let { onNavigateToDetail(it.id) }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建日记")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.todayEntry?.let { entry ->
                    item {
                        Text(
                            text = "今日",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        DiaryCard(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) }
                        )
                    }
                }

                if (uiState.recentEntries.isNotEmpty()) {
                    item {
                        Text(
                            text = "历史",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(uiState.recentEntries) { entry ->
                        DiaryCard(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) }
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add navigation and home screen"
```

---

### Task 8: 创建 DiaryCard 组件

**Files:**
- Create: `app/src/main/java/com/ai/notepad/presentation/components/DiaryCard.kt`
- Create: `app/src/main/java/com/ai/notepad/presentation/components/TagChip.kt`

- [ ] **Step 1: 创建 TagChip**

```kotlin
package com.ai.notepad.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ai.notepad.presentation.theme.Space

@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = Space.Space8, vertical = Space.Space4),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
```

- [ ] **Step 2: 创建 DiaryCard**

```kotlin
package com.ai.notepad.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.presentation.theme.Space
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiaryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Space.Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.date.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium
                )
                if (entry.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Space.Space4)
                    ) {
                        entry.tags.take(3).forEach { tag ->
                            TagChip(tag = tag)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.Space8))

            Text(
                text = entry.originalText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!entry.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Space.Space12))
                Text(
                    text = "📌 今日要点",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add DiaryCard and TagChip components"
```

---

## Phase 5: 详情页 (P0)

### Task 9: 创建详情页

**Files:**
- Create: `app/src/main/java/com/ai/notepad/presentation/detail/DetailUiState.kt`
- Create: `app/src/main/java/com/ai/notepad/presentation/detail/DetailViewModel.kt`
- Create: `app/src/main/java/com/ai/notepad/presentation/detail/DetailScreen.kt`

- [ ] **Step 1: 创建 DetailUiState**

```kotlin
package com.ai.notepad.presentation.detail

import com.ai.notepad.domain.model.DiaryEntry

data class DetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isAiProcessing: Boolean = false,
    val diaryEntry: DiaryEntry? = null,
    val editedText: String = "",
    val editedTags: List<String> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false
)
```

- [ ] **Step 2: 创建 DetailViewModel**

```kotlin
package com.ai.notepad.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.notepad.data.repository.DiaryRepository
import com.ai.notepad.domain.model.DiaryEntry
import com.ai.notepad.domain.usecase.SaveDiaryUseCase
import com.ai.notepad.domain.usecase.SummarizeDailyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val summarizeDailyUseCase: SummarizeDailyUseCase
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: 0L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val entries = diaryRepository.getEntriesByMonth(
                    java.time.YearMonth.now()
                )
                val entry = entries.find { it.id == entryId }
                    ?: DiaryEntry(
                        id = entryId,
                        date = java.time.LocalDate.now(),
                        originalText = "",
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        diaryEntry = entry,
                        editedText = entry.originalText,
                        editedTags = entry.tags
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateText(text: String) {
        _uiState.update { it.copy(editedText = text) }
    }

    fun updateTags(tags: List<String>) {
        _uiState.update { it.copy(editedTags = tags) }
    }

    fun save() {
        viewModelScope.launch {
            val currentEntry = _uiState.value.diaryEntry ?: return@launch
            _uiState.update { it.copy(isSaving = true) }

            try {
                val updatedEntry = currentEntry.copy(
                    originalText = _uiState.value.editedText,
                    tags = _uiState.value.editedTags,
                    updatedAt = LocalDateTime.now()
                )
                saveDiaryUseCase(updatedEntry)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun summarize() {
        viewModelScope.launch {
            val currentEntry = _uiState.value.diaryEntry ?: return@launch
            _uiState.update { it.copy(isAiProcessing = true) }

            // 先保存当前内容
            save()

            when (val result = summarizeDailyUseCase(currentEntry.id)) {
                is com.ai.notepad.util.Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isAiProcessing = false,
                            diaryEntry = result.data,
                            editedText = result.data.originalText,
                            editedTags = result.data.tags
                        )
                    }
                }
                is com.ai.notepad.util.Result.Error -> {
                    _uiState.update {
                        it.copy(isAiProcessing = false, error = result.exception.message)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
```

- [ ] **Step 3: 创建 DetailScreen**

```kotlin
package com.ai.notepad.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai.notepad.presentation.components.TagChip
import com.ai.notepad.presentation.theme.Space
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    entryId: Long,
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("保存成功")
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    uiState.diaryEntry?.let {
                        Text(it.date.format(dateFormatter))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "保存")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Space.Space16)
            ) {
                // 日记内容输入
                OutlinedTextField(
                    value = uiState.editedText,
                    onValueChange = { viewModel.updateText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    label = { Text("今日记录") },
                    placeholder = { Text("今天发生了什么？你的感悟是...") }
                )

                Spacer(modifier = Modifier.height(Space.Space16))

                // 标签
                Text(
                    text = "标签",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(Space.Space8))

                var newTag by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("添加标签") },
                    placeholder = { Text("输入标签后点击添加") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(Space.Space8))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.Space4),
                    verticalArrangement = Arrangement.spacedBy(Space.Space4)
                ) {
                    uiState.editedTags.forEach { tag ->
                        AssistChip(
                            onClick = {
                                viewModel.updateTags(uiState.editedTags - tag)
                            },
                            label = { Text(tag) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.Space24))

                // AI 总结按钮
                Button(
                    onClick = { viewModel.summarize() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isAiProcessing && uiState.editedText.isNotBlank()
                ) {
                    if (uiState.isAiProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text("AI 思考中...")
                    } else {
                        Text("🤖 AI 总结")
                    }
                }

                // AI 结果展示
                uiState.diaryEntry?.let { entry ->
                    if (!entry.summary.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(Space.Space24))
                        Text(
                            text = "📌 AI 总结",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Space.Space8))
                        Text(
                            text = entry.summary ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (!entry.advice.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(Space.Space16))
                        Text(
                            text = "💡 行动建议",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(Space.Space8))
                        Text(
                            text = entry.advice ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add detail screen with AI summarization"
```

---

## Phase 6-9: 后续功能

由于计划较长，我将在后续任务中继续实现：
- Phase 6: 语音输入（Whisper 集成）
- Phase 7: 月度总结页面
- Phase 8: 设置页面（API Key + Prompt 自定义）
- Phase 9: 动效和打磨

---

## 计划执行方式

**Plan complete and saved to `docs/superpowers/plans/2026-04-16-ai-notepad-implementation-plan.md`**

**Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
