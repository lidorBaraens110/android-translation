package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.OcrRepository
import com.example.myapplication.data.repository.TranslationRepository

class TranslatorViewModelFactory(
    private val ocrRepository: OcrRepository,
    private val translationRepository: TranslationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TranslatorViewModel(ocrRepository, translationRepository) as T
}
