package com.example.myapplication.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.RecentTranslation
import com.example.myapplication.data.repository.OcrRepository
import com.example.myapplication.data.repository.TranslationRepository
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TranslatorViewModel(
    private val ocrRepository: OcrRepository,
    private val translationRepository: TranslationRepository
) : ViewModel() {

    val languageOptions = linkedMapOf(
        "Hebrew" to TranslateLanguage.HEBREW,
        "English" to TranslateLanguage.ENGLISH,
        "Arabic" to TranslateLanguage.ARABIC
    )

    private val _sourceLanguage = MutableStateFlow("Hebrew")
    val sourceLanguage: StateFlow<String> = _sourceLanguage

    private val _targetLanguage = MutableStateFlow("English")
    val targetLanguage: StateFlow<String> = _targetLanguage

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _recentTranslations = MutableStateFlow<List<RecentTranslation>>(emptyList())
    val recentTranslations: StateFlow<List<RecentTranslation>> = _recentTranslations

    private var autoTranslateJob: Job? = null

    fun setSourceLanguage(lang: String) {
        _sourceLanguage.value = lang
        if (_inputText.value.isNotBlank()) retranslate()
    }

    fun setTargetLanguage(lang: String) {
        _targetLanguage.value = lang
        if (_inputText.value.isNotBlank()) retranslate()
    }

    private fun retranslate() {
        autoTranslateJob?.cancel()
        _translatedText.value = ""
        autoTranslateJob = viewModelScope.launch {
            delay(300)
            translateText {}
        }
    }

    fun swapLanguages() {
        val srcLang = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = srcLang

        val srcText = _inputText.value
        val tgtText = _translatedText.value
        if (tgtText.isNotEmpty()) {
            _inputText.value = tgtText
            _translatedText.value = srcText
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
        _translatedText.value = ""
        autoTranslateJob?.cancel()
        if (text.isBlank()) {
            _isProcessing.value = false
            return
        }
        autoTranslateJob = viewModelScope.launch {
            delay(1000)
            translateText {}
        }
    }

    fun setTranslatedText(text: String) { _translatedText.value = text }

    fun translateText(onDone: () -> Unit) {
        val text = _inputText.value
        if (text.isBlank()) { onDone(); return }

        _isProcessing.value = true
        _translatedText.value = ""

        val srcCode = languageOptions[_sourceLanguage.value] ?: TranslateLanguage.HEBREW
        val tgtCode = languageOptions[_targetLanguage.value] ?: TranslateLanguage.ENGLISH

        translationRepository.translate(
            text = text,
            srcLangCode = srcCode,
            tgtLangCode = tgtCode,
            onSuccess = { result ->
                _translatedText.value = result
                _recentTranslations.value = listOf(
                    RecentTranslation(
                        sourceText = text,
                        translatedText = result,
                        sourceLang = _sourceLanguage.value,
                        targetLang = _targetLanguage.value
                    )
                ) + _recentTranslations.value.take(9)
                _isProcessing.value = false
                onDone()
            },
            onFailure = { error ->
                _translatedText.value = error
                _isProcessing.value = false
                onDone()
            }
        )
    }

    fun runOcr(bitmap: Bitmap, onDone: () -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = ocrRepository.extractText(bitmap)
            _inputText.value = result
            _translatedText.value = ""
            _isProcessing.value = false
            onDone()
        }
    }

    fun clearRecentTranslations() { _recentTranslations.value = emptyList() }
}
