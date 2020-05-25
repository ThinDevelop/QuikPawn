package com.tss.quikpawn.util

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat

class Util {
    companion object {
        fun bitmapToBase64(bitmap: Bitmap): String {
            val byteArray = bitmapToByte(bitmap)
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        fun bitmapToByte(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            bitmap.recycle()
            return byteArray
        }

        fun toDateFormat(date: String): String {
            val parser = SimpleDateFormat("yyyy-MM-dd")
            val formatter = SimpleDateFormat("dd-MM-yyyy")
            val output: String = formatter.format(parser.parse(date))

            return output
        }

        fun productToBitmap() {

        }
    }
}