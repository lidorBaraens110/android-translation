package com.example.myapplication.data.repository

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslationRepository {

    fun translate(
        text: String,
        srcLangCode: String,
        tgtLangCode: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val translator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(srcLangCode)
                .setTargetLanguage(tgtLangCode)
                .build()
        )

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { result -> onSuccess(result) }
                    .addOnFailureListener { onFailure("Translation failed") }
            }
            .addOnFailureListener { onFailure("Model download failed. Check network.") }
    }
}
