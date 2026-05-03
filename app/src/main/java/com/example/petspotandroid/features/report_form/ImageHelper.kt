package com.example.petspotandroid.features.report_form

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageHelper {
    fun getCompressedImageBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ByteArrayOutputStream().use { outputStream ->
                    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    outputStream.toByteArray()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}