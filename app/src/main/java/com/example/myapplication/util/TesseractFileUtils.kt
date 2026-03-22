package com.example.myapplication.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object TesseractFileUtils {

    fun copyTrainedDataIfNeeded(context: Context, fileName: String): Result<Unit> {
        return runCatching {
            val tessDataDir = File(context.filesDir, "tessdata")
            if (!tessDataDir.exists()) tessDataDir.mkdirs()

            val outFile = File(tessDataDir, fileName)
            if (outFile.exists() && outFile.length() > 0) return Result.success(Unit)

            context.assets.open("tessdata/$fileName").use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            }
        }
    }
}
