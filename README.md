# AI 笔记本

本地存储的 AI 辅助日记应用，支持语音输入和智能总结。

## 功能特点

- **日记记录** - 每日一条日记，自动创建
- **AI 智能总结** - MiniMax API 提供智能复盘分析
- **语音输入** - 支持语音转文字（Whisper 集成中）
- **多标签管理** - 为日记添加标签分类
- **月度总结** - 自动生成月度复盘报告
- **Prompt 自定义** - 可自定义 AI 总结的 Prompt 模板
- **本地存储** - 所有数据存储在本地，保护隐私
- **API Key 安全** - 使用 EncryptedSharedPreferences 加密存储

## 技术栈

| 类别 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| 数据库 | Room |
| 依赖注入 | Hilt |
| AI | MiniMax API (M2.7-highspeed) |
| 语音 | Whisper (设备端) |
| 安全 | EncryptedSharedPreferences |

## 构建

```bash
# 生成 Gradle Wrapper
./gradlew wrapper

# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease
```

## 安装

```bash
# 安装到设备
./gradlew installDebug
```

## 配置

首次使用需要在设置页配置 MiniMax API Key：
1. 打开 App → 点击右上角设置图标
2. 输入你的 API Key
3. 保存后即可使用 AI 总结功能

## 截图

（待添加）

## 许可证

MIT
