# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

AI 笔记本 - 一个本地存储的 AI 辅助日记应用，支持语音输入和智能总结。

## 构建命令

```bash
# 需要 Java 21
export JAVA_HOME="/Users/yuyou/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home"

# 生成 Gradle Wrapper (首次)
./gradlew wrapper

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 运行 (需连接设备或模拟器)
./gradlew installDebug

# 清理
./gradlew clean

# 依赖检查
./gradlew dependencies
```

## 架构

**MVVM + Clean Architecture** 三层架构：

```
presentation/     → UI 层 (Compose Screen + ViewModel)
domain/           → 业务层 (UseCase + Repository 接口)
data/             → 数据层 (Repository 实现 + Room + Retrofit)
```

**依赖流向：** UI → ViewModel → UseCase → Repository → DataSource

**关键原则：**
- domain 层不依赖任何外部实现
- data 层通过接口被 domain 层调用
- presentation 层通过 StateFlow 与 UI 通信

## 技术栈

| 类别 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| DI | Hilt |
| 数据库 | Room |
| 网络 | Retrofit + OkHttp |
| 状态管理 | ViewModel + StateFlow |
| 语音 | Android SpeechRecognizer (中文识别) |
| AI | MiniMax API (M2.7-highspeed) |
| 安全 | EncryptedSharedPreferences |

## 关键设计

### API Key 安全存储
使用 `EncryptedSharedPreferences` (AES-256-GCM) 存储 API Key，硬件级加密。

### Prompt 可自定义
设置页支持用户自定义每日/每月总结的 Prompt 模板，使用 `PromptConfig` 实体存储。

### 本地优先
所有数据存储在本地 Room 数据库，不上传云端。语音识别使用 Android 原生 API，无需网络。

### 日期唯一性
每日只允许一条日记 (按 `date` 字段唯一索引)，不存在则自动创建。

## 目录结构

```
app/src/main/java/com/ai/notepad/
├── data/           # 数据层 (Repository 实现, Room, Retrofit)
├── domain/         # 业务层 (UseCase, Repository 接口, 模型)
├── presentation/   # UI 层 (Screen, ViewModel, Theme)
├── di/             # Hilt 依赖注入模块
└── util/           # 工具类 (ApiKeyManager, Result, VoiceRecognitionService)
```

## 语音输入

使用 Android 原生 `SpeechRecognizer` API 实现语音转文字，支持中文识别 (zh-CN)。
实现类: `util/VoiceRecognitionService.kt`

## API 集成

- **Base URL:** `https://api.minimaxi.com`
- **模型:** MiniMax-M2.7-highspeed
- **认证:** Bearer Token (API Key)
- **端点:** `/v1/text/chatcompletion_v2`

Prompt 模板定义在 `domain/model/PromptConfig.kt`。

## 注意事项

- **Java 版本:** 项目需要 Java 21，系统默认 Java 8 不兼容
- **DetailScreen vs DiaryDetailScreen:** `DiaryDetailScreen` 是当前使用的屏幕，`DetailScreen.kt` 是遗留文件（未使用）
