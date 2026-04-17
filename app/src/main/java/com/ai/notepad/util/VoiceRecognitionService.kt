package com.ai.notepad.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "VoiceRecognitionService"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    private var onResultCallback: ((String) -> Unit)? = null

    fun startListening(onResult: (String) -> Unit) {
        Log.d(TAG, "Starting voice recognition")

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available")
            _error.value = "语音识别不可用"
            return
        }

        onResultCallback = onResult
        _transcript.value = ""
        _error.value = null

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
            Log.d(TAG, "Speech recognizer started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer", e)
            _error.value = "启动语音识别失败: ${e.message}"
            _isListening.value = false
        }
    }

    fun stopListening() {
        Log.d(TAG, "Stopping voice recognition")
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognizer", e)
        }
        _isListening.value = false
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed - could be used for visual feedback
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d(TAG, "Buffer received")
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "End of speech")
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                SpeechRecognizer.ERROR_NO_MATCH -> "没有识别到内容"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别服务忙碌"
                SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有语音输入"
                else -> "未知错误"
            }
            Log.e(TAG, "Recognition error: $error - $errorMessage")
            _error.value = errorMessage
            _isListening.value = false

            // For no match error, we can retry
            if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                // Allow retry by resetting state
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val transcriptText = matches?.firstOrNull() ?: ""
            Log.d(TAG, "Recognition results: $transcriptText")

            if (transcriptText.isNotBlank()) {
                _transcript.value = transcriptText
                onResultCallback?.invoke(transcriptText)
            }
            _isListening.value = false
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull() ?: ""
            if (partialText.isNotBlank()) {
                Log.d(TAG, "Partial results: $partialText")
                _transcript.value = partialText
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d(TAG, "Event: $eventType")
        }
    }

    fun release() {
        stopListening()
    }
}
