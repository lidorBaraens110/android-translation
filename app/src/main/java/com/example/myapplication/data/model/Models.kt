package com.example.myapplication.data.model

data class RecentScan(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val language: String
) {
    val preview: String get() = text.take(80).trimEnd()
}

data class RecentTranslation(
    val id: Long = System.currentTimeMillis(),
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String
) {
    val preview: String get() = sourceText.take(60).trimEnd()
    val translatedPreview: String get() = translatedText.take(60).trimEnd()
}
