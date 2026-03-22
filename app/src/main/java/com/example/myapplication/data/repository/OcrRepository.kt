package com.example.myapplication.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.myapplication.util.preprocessBitmap
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OcrRepository(private val context: Context) {

    suspend fun extractText(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val processed = preprocessBitmap(bitmap)
            val tess = TessBaseAPI()
            val ok = tess.init(context.filesDir.absolutePath, "heb+eng+ara")
            if (!ok) {
                tess.recycle()
                return@withContext "Tesseract init failed"
            }
            tess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK)
            tess.setVariable("preserve_interword_spaces", "1")
            tess.setImage(processed)
            val text = tess.utF8Text ?: "No text found"
            tess.recycle()
            text
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
