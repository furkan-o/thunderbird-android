package com.fsck.k9.contacts

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.fsck.k9.mail.Address

/**
 * Draw a `Bitmap` containing the "contact letter" obtained by [ContactLetterExtractor].
 */
class ContactLetterBitmapCreator(
    private val letterExtractor: ContactLetterExtractor,
    val config: ContactLetterBitmapConfig,
) {
    fun drawBitmap(bitmap: Bitmap, pictureSizeInPx: Int, address: Address): Bitmap {
        val canvas = Canvas(bitmap)

        val backgroundColor = calcUnknownContactColor(address)
        bitmap.eraseColor(backgroundColor)

        val letter = letterExtractor.extractContactLetter(address)
        val isMultiLetter = letter.length > 1

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = calcTextColor(backgroundColor)
            textSize = pictureSizeInPx.toFloat() * (if (isMultiLetter) 0.44f else 0.54f)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val rect = Rect()
        paint.getTextBounds(letter, 0, letter.length, rect)

        val width = paint.measureText(letter)
        canvas.drawText(
            letter,
            pictureSizeInPx / 2f - width / 2f,
            pictureSizeInPx / 2f + rect.height() / 2f,
            paint,
        )

        return bitmap
    }

    private fun calcTextColor(backgroundColor: Int): Int {
        val r = (backgroundColor shr 16) and 0xFF
        val g = (backgroundColor shr 8) and 0xFF
        val b = backgroundColor and 0xFF
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(r, g, b, hsv)

        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        if (luminance > 0.5) {
            hsv[1] = (hsv[1] * 3.5f + 0.5f).coerceIn(0.6f, 0.95f)
            hsv[2] = (hsv[2] * 0.45f).coerceIn(0.2f, 0.55f)
        } else {
            hsv[1] = (hsv[1] * 0.7f).coerceIn(0.3f, 0.6f)
            hsv[2] = 0.95f
        }
        return android.graphics.Color.HSVToColor(0xFF, hsv)
    }

    fun calcUnknownContactColor(address: Address): Int {
        if (config.hasDefaultBackgroundColor) {
            return config.defaultBackgroundColor
        }

        val hash = address.hashCode()
        val backgroundColors = config.backgroundColors
        val colorIndex = (hash and Integer.MAX_VALUE) % backgroundColors.size
        return backgroundColors[colorIndex]
    }

    fun signatureOf(address: Address): String {
        return calcUnknownContactColor(address).toString()
    }
}
