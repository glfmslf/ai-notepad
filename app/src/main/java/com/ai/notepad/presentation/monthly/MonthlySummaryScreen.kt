package com.ai.notepad.presentation.monthly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai.notepad.presentation.theme.Space

@Composable
private fun ParsedSummaryContent(summary: String) {
    val sectionPattern = Regex("([✨🌟📈🎯])\\s*([^\n✨🌟📈🎯]+)")
    val matches = sectionPattern.findAll(summary).toList()

    if (matches.isEmpty()) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    matches.forEachIndexed { index, match ->
        val emoji = match.groupValues[1]
        val title = match.groupValues[2].trim()
        val contentStart = match.range.last + 1
        val contentEnd = if (index < matches.size - 1) {
            matches[index + 1].range.first
        } else {
            summary.length
        }
        val content = summary.substring(contentStart, contentEnd).trim()

        val (headerColor, sectionSpacing) = when (emoji) {
            "✨" -> MaterialTheme.colorScheme.primary to Space.Space24
            "🌟" -> Color(0xFF10B981) to Space.Space24
            "📈" -> Color(0xFFF59E0B) to Space.Space24
            "🎯" -> MaterialTheme.colorScheme.primary to Space.Space24
            else -> MaterialTheme.colorScheme.onSurface to Space.Space16
        }

        if (index > 0) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Space.Space16),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        Text(
            text = "$emoji $title",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = headerColor,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Space.Space8))

        val bulletPoints = content.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.removePrefix("• ").removePrefix("- ").trim() }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Space.Space4)
        ) {
            bulletPoints.forEach { point ->
                when {
                    point.startsWith("→") -> {
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = Space.Space8)
                        )
                    }
                    point.contains("+ 原因分析") || point.contains("+原因分析") -> {
                        val parts = point.split(Regex("\\+\\s*原因分析"))
                        Text(
                            text = "• ${parts[0].trim()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = Space.Space4)
                        )
                        if (parts.size > 1 && parts[1].isNotBlank()) {
                            Text(
                                text = "  原因：${parts[1].trim()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = Space.Space16)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "• $point",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = Space.Space4)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(sectionSpacing))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(
    month: String,
    viewModel: MonthlyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$month 月度总结") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Space.Space16)
                    .verticalScroll(rememberScrollState())
            ) {
                uiState.monthlyEntry?.let { entry ->
                    val summary = entry.summary
                    if (summary.isNullOrBlank()) {
                        Text(
                            text = "暂无总结",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ParsedSummaryContent(summary = summary)
                    }
                } ?: Text(
                    text = "暂无月度总结",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
