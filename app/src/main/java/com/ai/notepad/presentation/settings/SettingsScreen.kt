package com.ai.notepad.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai.notepad.presentation.theme.Space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var apiKeyInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Space.Space16)
                .verticalScroll(rememberScrollState())
        ) {
            // API Key 区域
            Text(
                text = "MiniMax API Key",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Space.Space8))

            if (uiState.hasApiKey) {
                Text(
                    text = "API Key 已配置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Space.Space8))
                Button(
                    onClick = { viewModel.clearApiKey() }
                ) {
                    Text("清除 API Key")
                }
            } else {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入 API Key") },
                    placeholder = { Text("请输入您的 MiniMax API Key") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "隐藏" else "显示"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(Space.Space16))
                Button(
                    onClick = { viewModel.saveApiKey(apiKeyInput) },
                    enabled = !uiState.isSaving && apiKeyInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("保存 API Key")
                }
            }

            Spacer(modifier = Modifier.height(Space.Space32))

            // Prompt 自定义区域
            Text(
                text = "Prompt 自定义",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Space.Space4))
            Text(
                text = "可用变量：{{diary_content}}（日记内容）、{{monthly_entries}}（月度日记列表）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Space.Space16))

            Text(
                text = "每日总结 Prompt",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(Space.Space4))
            OutlinedTextField(
                value = uiState.dailyPromptText,
                onValueChange = { viewModel.updateDailyPrompt(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("输入自定义的每日总结 Prompt...") }
            )

            Spacer(modifier = Modifier.height(Space.Space16))

            Text(
                text = "每月总结 Prompt",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(Space.Space4))
            OutlinedTextField(
                value = uiState.monthlyPromptText,
                onValueChange = { viewModel.updateMonthlyPrompt(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("输入自定义的每月总结 Prompt...") }
            )

            Spacer(modifier = Modifier.height(Space.Space16))

            Button(
                onClick = { viewModel.savePromptConfig() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("保存 Prompt")
            }

            Spacer(modifier = Modifier.height(Space.Space8))

            OutlinedButton(
                onClick = { viewModel.resetToDefault() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text("恢复默认 Prompt")
            }

            Spacer(modifier = Modifier.height(Space.Space32))

            // 月度总结区域
            Text(
                text = "月度总结",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Space.Space8))
            Button(
                onClick = { viewModel.generateMonthlySummary() },
                enabled = !uiState.isGeneratingSummary && uiState.hasApiKey,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isGeneratingSummary) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("生成中...")
                } else {
                    Text("生成本月总结")
                }
            }

            Spacer(modifier = Modifier.height(Space.Space32))
        }
    }
}
