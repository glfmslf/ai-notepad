# AI 笔记本 - 技术规格文档

## 一、技术选型

| 层级 | 技术 | 选型理由 |
|------|------|---------|
| **框架** | Jetpack Compose + Kotlin | Android 官方现代 UI 框架，声明式编程，Google 主推 |
| **架构** | MVVM + Clean Architecture | 清晰分层，职责明确，便于测试和扩展 |
| **本地数据库** | Room | Android 官方 ORM，与 Compose 集成良好 |
| **语音识别** | Whisper (设备端) | 完全离线，保护隐私，CDN 加速模型下载 |
| **AI 总结** | MiniMax API (M2.7-highspeed) | 国内访问稳定，中文能力强，价格合理 |
| **状态管理** | ViewModel + StateFlow | 生命周期感知，协程友好 |
| **依赖注入** | Hilt | Google 官方 DI 方案，与 Android 生态深度集成 |
| **网络** | Retrofit + OkHttp | 成熟稳定，拦截器便于统一处理 API Key |
| **加密存储** | EncryptedSharedPreferences | Android Jetpack 安全性套件，硬件级加密 |
| **日期处理** | java.time (LocalDate) | Android 8.0+ 内置，替代 ThreeTenBP |

---

## 二、依赖库

```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    annotationProcessor("com.google.dagger:hilt-compiler:2.50")
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

---

## 三、数据模型

### 3.1 数据库 Schema

```kotlin
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,              // 日期（唯一索引，每日一条）
    val originalText: String,         // 用户输入原文
    val summary: String? = null,      // AI 要点提炼
    val advice: String? = null,       // AI 行动建议
    val tags: List<String> = emptyList(),  // 标签列表
    val isMonthlySummary: Boolean = false,  // 是否为月总结
    val month: String? = null,       // 所属月份 "2026-04"（月总结用）
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Entity(tableName = "prompt_configs")
data class PromptConfig(
    @PrimaryKey
    val id: Int = 1,                 // 单例配置
    val useDefaultDaily: Boolean = true,
    val customDailyPrompt: String? = null,
    val useDefaultMonthly: Boolean = true,
    val customMonthlyPrompt: String? = null
)
```

### 3.2 API Response 模型

```kotlin
// MiniMax 聊天完成响应
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
    val total_tokens: Int,
    val prompt_tokens: Int,
    val completion_tokens: Int
)
```

---

## 四、网络层设计

### 4.1 API Service

```kotlin
interface MiniMaxApiService {

    @POST("/v1/text/chatcompletion_v2")
    suspend fun chatCompletion(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatCompletionResponse

    @Streaming
    @POST("/v1/text/chatcompletion_v2")
    suspend fun chatCompletionStream(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): Flow<ChatCompletionResponse>
}

data class ChatRequest(
    val model: String = "MiniMax-M2.7-highspeed",
    val messages: List<MessageRequest>,
    val stream: Boolean = false
)

data class MessageRequest(
    val role: String,
    val content: String
)
```

### 4.2 API Key 管理

```kotlin
class ApiKeyManager(context: Context) {
    private val encryptedPrefs = EncryptedSharedPreferences
        .create(context, "secure_prefs", context.mainExecutor,
            EncryptedSharedPreferences.AesPadding.GCM)

    fun saveApiKey(key: String) {
        encryptedPrefs.edit().putString("minimax_api_key", key).apply()
    }

    fun getApiKey(): String? {
        return encryptedPrefs.getString("minimax_api_key", null)
    }

    fun hasApiKey(): Boolean = getApiKey() != null

    fun clearApiKey() {
        encryptedPrefs.edit().remove("minimax_api_key").apply()
    }
}
```

---

## 五、Prompt 设计

### 5.1 默认每日总结 Prompt

```
【系统提示】
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
```

### 5.2 默认月度总结 Prompt

```
【系统提示】
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
→ 行动3
```

### 5.3 Prompt 模板变量

| 变量 | 说明 | 适用场景 |
|------|------|---------|
| `{{diary_content}}` | 日记原文 | 每日总结 |
| `{{monthly_entries}}` | 月度日记列表（格式：日期\n内容\n---\n） | 月度总结 |

---

## 六、模块架构

```
com.ai.notepad
├── data
│   ├── local
│   │   ├── dao/          # Room DAO
│   │   ├── entity/       # Room Entity
│   │   └── database/     # Room Database
│   ├── remote
│   │   ├── api/          # Retrofit API
│   │   └── model/        # API Response Model
│   └── repository/       # Repository 实现
├── domain
│   ├── model/            # 领域模型
│   ├── repository/       # Repository 接口
│   └── usecase/         # 用例
├── presentation
│   ├── home/            # 首页/日记列表
│   ├── detail/          # 日记详情
│   ├── monthly/         # 月总结
│   └── settings/        # 设置页
├── di/                  # Hilt Module
└── util/                # 工具类
```

---

## 七、语音识别集成

### 7.1 Whisper 模型下载

```kotlin
sealed class DownloadState {
    object NotStarted : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Downloaded : DownloadState()
    data class Error(val message: String) : DownloadState()
}

// 模型文件约 50-200MB，首次使用时下载
val whisperModel = remember { WhisperModel.newBuilder(context).build() }
```

### 7.2 语音识别流程

```
用户按住录音按钮
        ↓
实时音频流 → Whisper 本地推理
        ↓
实时转文字显示在输入框
        ↓
用户松开按钮
        ↓
识别结束，可编辑文字
        ↓
点击"AI 总结"
```

---

## 八、安全性设计

| 安全措施 | 实现方式 |
|---------|---------|
| API Key 存储 | EncryptedSharedPreferences (AES-256-GCM) |
| 数据库 | Room 数据库（可配合 SQLCipher 加密） |
| 网络 | HTTPS 传输 |
| 日记数据 | 本地存储，不上传云端 |
| 语音数据 | 设备端处理，不离开手机 |

---

## 九、页面导航

```
Navigation Graph
├── HomeScreen (首页-日记卡片列表)
│   ├── TodayCard (今日日记卡片)
│   ├── HistoryList (历史日记列表)
│   └── FAB → NewEntryScreen
├── DetailScreen (日记详情/编辑)
├── MonthlySummaryScreen (月总结)
└── SettingsScreen (设置)
    ├── ApiKeyInput
    ├── PromptCustomization
    └── About
```

---

## 十、关键业务流程

### 10.1 每日记录流程

```
1. 打开 App → 检测今日是否存在日记
   ├── 存在 → 显示今日卡片（可编辑）
   └── 不存在 → 创建空白日记卡片

2. 用户输入
   ├── 打字输入 → 直接编辑
   └── 语音输入 → 实时转文字 → 用户确认

3. 点击"AI 总结"
   ├── 检测 API Key
   │   ├── 未配置 → 跳转设置页
   │   └── 已配置 → 继续
   ├── 调用 MiniMax API
   ├── 解析响应，提取 summary 和 advice
   └── 更新日记，显示 AI 结果

4. 用户可添加标签 → 保存
```

### 10.2 月度总结流程

```
触发条件：每月最后一天 23:59 自动执行
        或用户手动在设置中触发

1. 收集当月所有日记（按日期排序）
2. 拼接日记列表为格式化文本
3. 调用 MiniMax API（月度总结 Prompt）
4. 创建/更新月度总结日记（isMonthlySummary=true, month="2026-04"）
5. 用户可在首页看到月总结入口
```

---

## 十一、UI/UX 设计

### 11.1 配色方案

```kotlin
// 主题色
val Primary = Color(0xFF6366F1)      // 靛蓝紫 - 主色调
val PrimaryVariant = Color(0xFF4F46E5) // 深靛蓝 - 按压态
val Secondary = Color(0xFF10B981)     // 翠绿 - 成功/AI
val SecondaryVariant = Color(0xFF059669)

// 表面色
val Background = Color(0xFFFAFAFA)   // 浅灰白背景
val Surface = Color(0xFFFFFFFF)       // 卡片白
val SurfaceVariant = Color(0xFFF3F4F6) // 次级表面

// 文字色
val OnPrimary = Color.White
val OnSurface = Color(0xFF1F2937)     // 深灰 - 主文字
val OnSurfaceVariant = Color(0xFF6B7280) // 中灰 - 次级文字

// 功能色
val Error = Color(0xFFEF4444)         // 红色 - 错误
val Warning = Color(0xFFF59E0B)       // 橙色 - 警告
val Success = Color(0xFF10B981)       // 绿色 - 成功
```

### 11.2 字体规范

```kotlin
// 标题层级
val DisplayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
val HeadlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
val TitleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium)

// 正文层级
val BodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
val BodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
val BodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)

// 标签/辅助
val LabelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
```

### 11.3 间距系统

```kotlin
// 8dp 基准网格
val Space2 = 2.dp
val Space4 = 4.dp
val Space8 = 8.dp
val Space12 = 12.dp
val Space16 = 16.dp
val Space24 = 24.dp
val Space32 = 32.dp
val Space48 = 48.dp

// 卡片内边距：16dp
// 卡片间距：12dp
// 页面边距：16dp
// 列表项间距：8dp
```

### 11.4 组件设计

**日记卡片 (DiaryCard)**

```
┌──────────────────────────────────────┐
│ 📅 2026年4月16日          [标签1][标签2] │  ← 顶部栏
├──────────────────────────────────────┤
│                                      │
│  今日完成：                          │
│  - 完成项目方案设计                   │
│  - 和团队对齐了迭代计划               │
│  - 学习了一节 AI 课程                 │
│                                      │
├──────────────────────────────────────┤
│ 🤖 AI 总结                           │  ← AI 区块
│ ──────────────────────────────────── │
│ 📌 今日要点                          │
│ • 完成项目方案设计                    │
│ • 对齐迭代计划                       │
│ • 持续学习                           │
│                                      │
│ 💡 行动建议                          │
│ → 明天继续跟进设计评审               │
│ → 整理课程笔记                       │
└──────────────────────────────────────┘
         [✏️ 编辑]  [🎤 语音]
```

**月总结卡片**

```
┌──────────────────────────────────────┐
│ 📆 2026年4月 月总结          [✨ 自动生成] │
├──────────────────────────────────────┤
│                                      │
│ ✨ 本月概述                          │
│ 稳中求进，持续成长的一月              │
│                                      │
│ 🌟 本月高光                          │
│ • 完成 Q1 目标                      │
│ • 建立每日复盘习惯                   │
│ • 读完 2 本书                       │
│                                      │
│ 📈 成长观察                          │
│ • 执行力有明显提升                   │
│ • 更加注重反思                      │
│                                      │
│ 🎯 下月聚焦                          │
│ → 继续维持复盘习惯                   │
│ → 加强跨团队协作                    │
│ → 探索新技能方向                    │
│                                      │
└──────────────────────────────────────┘
         [📋 查看本月日记]
```

### 11.5 动效规范

| 动效场景 | 规范 |
|---------|------|
| 页面切换 | 共享元素过渡，300ms ease-out |
| 卡片展开 | 高度动画，200ms ease-in-out |
| AI 生成中 | 脉冲动画 + "思考中..." 文字 |
| 语音输入中 | 波纹动画，实时文字高亮 |
| 保存成功 | 短暂绿色闪烁 + checkmark |
| 下拉刷新 | 官方 Material3 pullRefresh |

---

## 十二、状态管理设计

### 12.1 UI State 定义

```kotlin
// 首页状态
data class HomeUiState(
    val isLoading: Boolean = false,
    val todayEntry: DiaryEntry? = null,
    val recentEntries: List<DiaryEntry> = emptyList(),
    val monthlySummaries: List<DiaryEntry> = emptyList(),
    val error: String? = null
)

// 详情页状态
data class DetailUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isAiProcessing: Boolean = false,
    val diaryEntry: DiaryEntry? = null,
    val isEditing: Boolean = false,
    val error: String? = null
)

// 设置页状态
data class SettingsUiState(
    val apiKey: String = "",           // 不显示明文
    val hasApiKey: Boolean = false,
    val dailyPrompt: PromptConfig = PromptConfig(),
    val monthlyPrompt: PromptConfig = PromptConfig(),
    val isSaving: Boolean = false,
    val error: String? = null
)
```

### 12.2 事件流设计

```kotlin
// 用户意图
sealed class HomeEvent {
    object LoadData : HomeEvent()
    object CreateTodayEntry : HomeEvent()
    data class DeleteEntry(val id: Long) : HomeEvent()
    data class NavigateToDetail(val id: Long) : HomeEvent()
    object NavigateToMonthlySummary : HomeEvent()
}

// AI 处理意图
sealed class AiIntent {
    data class SummarizeDaily(val entryId: Long) : AiIntent()
    data class SummarizeMonthly(val month: String) : AiIntent()
    data class UpdatePromptConfig(val config: PromptConfig) : AiIntent()
}

// AI 状态
sealed class AiState {
    object Idle : AiState()
    data class Processing(val message: String = "AI 思考中...") : AiState()
    data class Success(val result: String) : AiState()
    data class Error(val message: String) : AiState()
}
```

---

## 十三、错误处理设计

### 13.1 错误分类

```kotlin
sealed class AppError {
    // 网络相关
    object NoNetwork : AppError()
    data class NetworkError(val message: String) : AppError()
    data class ApiError(val code: Int, val message: String) : AppError()
    object ApiKeyInvalid : AppError()

    // 语音相关
    object MicPermissionDenied : AppError()
    object SpeechRecognizerUnavailable : AppError()
    data class SpeechError(val message: String) : AppError()

    // 存储相关
    object DatabaseError : AppError()

    // AI 相关
    object AiServiceUnavailable : AppError()
    data class AiResponseParseError(val rawResponse: String) : AiError()

    // 通用
    object UnknownError : AppError()
}
```

### 13.2 错误展示策略

| 错误类型 | UI 反馈 |
|---------|--------|
| NoNetwork | Snackbar + 重试按钮 |
| ApiKeyInvalid | Dialog 提示 + 跳转设置 |
| MicPermissionDenied | Dialog 说明权限必要性 + 跳转设置 |
| AiResponseParseError | Snackbar + 日记内容保留，可重试 |
| DatabaseError | 全局 ErrorScreen + 联系支持 |
| UnknownError | Snackbar + 记录日志 |

### 13.3 重试机制

```kotlin
// API 调用重试：最多 3 次，指数退避
suspend fun <T> apiWithRetry(block: suspend () -> T): Result<T> {
    var attempts = 0
    while (attempts < MAX_RETRIES) {
        try {
            return Result.success(block())
        } catch (e: Exception) {
            attempts++
            if (attempts >= MAX_RETRIES) {
                return Result.failure(e)
            }
            delay(BASE_DELAY * attempts * 2)  // 1s, 2s, 4s
        }
    }
    return Result.failure(UnknownError)
}
```

---

## 十四、Room 数据库 Schema

```kotlin
@Database(
    entities = [DiaryEntryEntity::class, PromptConfigEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun promptConfigDao(): PromptConfigDao
}

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String,  // "2026-04-16"

    @ColumnInfo(name = "original_text")
    val originalText: String,

    @ColumnInfo(name = "summary")
    val summary: String?,

    @ColumnInfo(name = "advice")
    val advice: String?,

    @ColumnInfo(name = "tags")
    val tags: String,  // JSON: ["工作","学习"]

    @ColumnInfo(name = "is_monthly_summary")
    val isMonthlySummary: Boolean = false,

    @ColumnInfo(name = "month")
    val month: String?,  // "2026-04"

    @ColumnInfo(name = "created_at")
    val createdAt: Long,  // timestamp

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

@Entity(tableName = "prompt_configs")
data class PromptConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val useDefaultDaily: Boolean = true,
    val customDailyPrompt: String?,
    val useDefaultMonthly: Boolean = true,
    val customMonthlyPrompt: String?
)
```

---

## 十五、月度定时任务设计

### 15.1 自动触发机制

```kotlin
// 使用 WorkManager 定时任务
class MonthlySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())

            // 检查是否当月最后一天
            if (LocalDate.now() == lastDayOfMonth) {
                generateMonthlySummary()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun generateMonthlySummary() {
        // 1. 收集当月日记
        // 2. 调用 AI
        // 3. 保存月总结
    }
}

// 配置周期性任务
val monthlyWorkRequest = PeriodicWorkRequestBuilder<MonthlySummaryWorker>(
    1, TimeUnit.DAYS
)
    .setInitialDelay(calculateDelayToNextMonth(), TimeUnit.MILLISECONDS)
    .build()
```

### 15.2 手动触发

```kotlin
// 设置页可手动触发月总结生成
object MonthlySummaryUseCase {
    suspend operator fun invoke(month: String): Result<DiaryEntry> {
        // 1. 获取当月所有日记
        // 2. 拼接格式化的日记列表
        // 3. 调用 MiniMax API
        // 4. 创建月总结日记
        // 5. 保存到数据库
    }
}
```

---

## 十六、技术关注点

### 16.1 性能优化

| 场景 | 优化方案 |
|------|---------|
| AI 响应 | 流式输出（Streaming），先展示已生成内容 |
| 数据库 | Room 索引：`date` 字段建立索引 |
| 列表 | LazyColumn 分页加载，每次 20 条 |
| 图片/模型 | 模型懒加载，不影响首屏启动速度 |
| Whisper | 后台预加载模型，首次录音无需等待 |

### 16.2 兼容性

| 项目 | 最低要求 |
|------|---------|
| Android | API 26 (Android 8.0) |
| Compose | API 21+（Compose Runtime） |
| EncryptedSharedPreferences | API 23+ |
| Room | API 21+ |

### 16.3 可访问性

- 所有图片提供 contentDescription
- 触摸目标最小 48dp
- 颜色对比度符合 WCAG AA 标准
- 支持 TalkBack 屏幕阅读
- 支持动态字体大小

---

## 十七、项目结构

```
app/
├── src/main/
│   ├── java/com/ai/notepad/
│   │   ├── AiNotepadApplication.kt
│   │   │
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── dao/
│   │   │   │   │   ├── DiaryDao.kt
│   │   │   │   │   └── PromptConfigDao.kt
│   │   │   │   ├── entity/
│   │   │   │   │   ├── DiaryEntryEntity.kt
│   │   │   │   │   └── PromptConfigEntity.kt
│   │   │   │   └── converter/
│   │   │   │       └── Converters.kt
│   │   │   │
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   └── MiniMaxApiService.kt
│   │   │   │   ├── model/
│   │   │   │   │   └── ChatCompletionModels.kt
│   │   │   │   └── repository/
│   │   │   │       └── AiRepositoryImpl.kt
│   │   │   │
│   │   │   └── repository/
│   │   │       ├── DiaryRepository.kt
│   │   │       ├── DiaryRepositoryImpl.kt
│   │   │       ├── PromptRepository.kt
│   │   │       └── PromptRepositoryImpl.kt
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── DiaryEntry.kt
│   │   │   │   └── PromptConfig.kt
│   │   │   ├── repository/
│   │   │   │   └── ... (interfaces)
│   │   │   └── usecase/
│   │   │       ├── GetTodayDiaryUseCase.kt
│   │   │       ├── SaveDiaryUseCase.kt
│   │   │       ├── SummarizeDailyUseCase.kt
│   │   │       ├── SummarizeMonthlyUseCase.kt
│   │   │       └── ...
│   │   │
│   │   ├── presentation/
│   │   │   ├── MainActivity.kt
│   │   │   ├── navigation/
│   │   │   │   └── AppNavigation.kt
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Type.kt
│   │   │   │   ├── Space.kt
│   │   │   │   └── Theme.kt
│   │   │   ├── components/
│   │   │   │   ├── DiaryCard.kt
│   │   │   │   ├── MonthlySummaryCard.kt
│   │   │   │   ├── TagChip.kt
│   │   │   │   ├── VoiceInputButton.kt
│   │   │   │   └── LoadingIndicator.kt
│   │   │   │
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── HomeViewModel.kt
│   │   │   │   └── HomeUiState.kt
│   │   │   │
│   │   │   ├── detail/
│   │   │   │   ├── DetailScreen.kt
│   │   │   │   ├── DetailViewModel.kt
│   │   │   │   └── DetailUiState.kt
│   │   │   │
│   │   │   ├── monthly/
│   │   │   │   ├── MonthlySummaryScreen.kt
│   │   │   │   ├── MonthlyViewModel.kt
│   │   │   │   └── MonthlyUiState.kt
│   │   │   │
│   │   │   └── settings/
│   │   │       ├── SettingsScreen.kt
│   │   │       ├── SettingsViewModel.kt
│   │   │       └── SettingsUiState.kt
│   │   │
│   │   ├── di/
│   │   │   ├── AppModule.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   ├── NetworkModule.kt
│   │   │   └── RepositoryModule.kt
│   │   │
│   │   ├── util/
│   │   │   ├── ApiKeyManager.kt
│   │   │   ├── DateUtils.kt
│   │   │   ├── PromptTemplate.kt
│   │   │   └── Result.kt
│   │   │
│   │   └── voice/
│   │       ├── WhisperManager.kt
│   │       └── SpeechRecognizerManager.kt
│   │
│   └── res/
│       ├── values/
│       │   ├── strings.xml
│       │   ├── colors.xml
│       │   └── themes.xml
│       └── ...
│
├── build.gradle.kts
└── proguard-rules.pro
```

---

## 十八、开发阶段规划

| 阶段 | 内容 | 优先级 |
|------|------|--------|
| **Phase 1** | 项目骨架：Compose + Room + Hilt 搭建 | P0 |
| **Phase 2** | 数据层：Diary CRUD + 本地存储 | P0 |
| **Phase 3** | AI 层：MiniMax API 集成 + Prompt | P0 |
| **Phase 4** | 首页：日记卡片列表 + 今日日记 | P0 |
| **Phase 5** | 详情页：日记编辑 + AI 总结展示 | P0 |
| **Phase 6** | 语音输入：Whisper 集成 | P1 |
| **Phase 7** | 月度总结：自动生成 + 展示 | P1 |
| **Phase 8** | 设置页：API Key + Prompt 自定义 | P1 |
| **Phase 9** | 打磨：动效 + 错误处理 + 边界情况 | P2 |
