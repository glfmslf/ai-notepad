package com.ai.notepad.presentation.detail

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
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
import com.ai.notepad.presentation.theme.Space
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun DiaryDetailScreen(
    entryId: Long,
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

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
                OutlinedTextField(
                    value = uiState.editedText,
                    onValueChange = { viewModel.updateText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    label = { Text("今日记录") },
                    placeholder = { Text("今天发生了什么？你的感悟是...") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            if (micPermissionState.status.isGranted) {
                                if (isRecording) {
                                    viewModel.stopVoiceRecording()
                                } else {
                                    viewModel.startVoiceRecording()
                                }
                            } else {
                                micPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "语音输入",
                            modifier = Modifier.size(24.dp),
                            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(Space.Space8))
                    Text(
                        text = "正在录音...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(Space.Space16))

                Text(
                    text = "标签",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(Space.Space8))

                var newTag by remember { mutableStateOf("") }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.Space4),
                    verticalArrangement = Arrangement.spacedBy(Space.Space4)
                ) {
                    uiState.editedTags.forEach { tag ->
                        AssistChip(
                            onClick = {
                                viewModel.updateTags(uiState.editedTags - tag)
                            },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "删除",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.Space8))

                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("添加标签") },
                    placeholder = { Text("输入标签后点击添加") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (newTag.isNotBlank() && !uiState.editedTags.contains(newTag)) {
                                    viewModel.updateTags(uiState.editedTags + newTag)
                                    newTag = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Space.Space24))

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

                uiState.diaryEntry?.let { entry ->
                    if (!entry.summary.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(Space.Space24))
                        Text(
                            text = "📝 AI 复盘总结",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Space.Space8))
                        Text(
                            text = entry.summary ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
