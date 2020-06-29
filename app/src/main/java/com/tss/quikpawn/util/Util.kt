package com.tss.quikpawn.util

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.tss.quikpawn.R
import com.tss.quikpawn.models.ProductModel
import com.tss.quikpawn.models.ProductModel2
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class Util {
    companion object {

        fun getMonth(c: Calendar): String {
            val Months= arrayListOf( "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.",
                "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.",
                "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
            var month = c.get(Calendar.MONTH)

            return Months[month]
        }

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
            val date = toDateTh(output)
            return ""+date.get(Calendar.DATE)+" "+ getMonth(date)+" "+(date.get(Calendar.YEAR)+543)
        }

        fun toDateTh(date: String): Calendar {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            cal.time = sdf.parse(date)
            return cal
        }

        fun rotageBitmap(filePath: String?): Bitmap {
            var bitmap = BitmapFactory.decodeFile(filePath)
            var bitmap2: Bitmap
            if (bitmap.width > bitmap.height) {
                bitmap2 = rotateImage(bitmap, 90f)!!
            }else {
                bitmap2 = bitmap
            }
//            val ei = ExifInterface(filePath)
//            val orientation: Int = ei.getAttributeInt(
//                ExifInterface.TAG_ORIENTATION,
//                ExifInterface.ORIENTATION_UNDEFINED
//            )
//
//            var rotatedBitmap = bitmap
//            when (orientation) {
//                ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap, 90f)
//                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(bitmap, 180f)
//                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(bitmap, 270f)
//                ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
//                else -> rotatedBitmap = bitmap
//            }
            return bitmap2
        }

        private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                matrix, true
            )
        }

        fun stringToCalendar(date: String): Calendar {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            cal.time = sdf.parse(date)
            return cal
        }

        fun addRectangle(bitmap: Bitmap): Bitmap {
            return bitmap
        }

        fun dashLine(context: Context): PrinterParams {
            val cutBitmap = getBitmapFromVectorDrawable(
                context,
                R.drawable.ic_content_cut_black_24dp
            )
            val printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(cutBitmap)
            return printerParams1
        }

        fun dashSignature(): PrinterParams {
            val printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setTextSize(22)
            printerParams1.setText("\n\n\n\n______________________\nลายเซ็น\n")
            return printerParams1
        }

        fun textToBitmap(text: String): Bitmap {
            val bitmap =
            Bitmap.createBitmap(600, 100, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            // new antialised Paint
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb(0, 0, 0)
            // text size in pixels
            paint.textSize = 80f
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            canvas.drawText(text, (bitmap.width/2)-(bounds.width()/2f), 100f, paint)
            return bitmap
        }

        fun productListToBitmap2(productList: List<ProductModel2>): Bitmap {
            val bitmap =
                Bitmap.createBitmap(600, (productList.size * 34) + 10, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            // new antialised Paint
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb(0, 0, 0)
            // text size in pixels
            paint.textSize = 38f
            paint.isFakeBoldText = true
            //custom fonts

            // draw text to the Canvas center
            val bounds = Rect()
            productList.forEachIndexed { index, product ->
                //draw the first text
                paint.getTextBounds("ราคา", 0,  "ราคา".length, bounds)
                var x = 5f
                var y = (index + 1) * 32f
                canvas.drawText("ราคา", x, y, paint)
                //draw the second text
                val price = " "+NumberTextWatcherForThousand.getDecimalFormattedString(product.price)
                paint.getTextBounds(price, 0, price.length, bounds)
                x = ((bitmap.width / 4f) * 2.8f) + ((bitmap.width - ((bitmap.width / 4f) * 2.8f)) - bounds.width())
                canvas.drawText(product.price, x, y, paint)
            }
            return bitmap
        }

        fun productListToBitmap(productList: List<ProductModel2>): Bitmap {
            val bitmap =
                Bitmap.createBitmap(600, (productList.size * 34) + 10, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            // new antialised Paint
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb(0, 0, 0)
            // text size in pixels
            paint.textSize = 38f
            paint.isFakeBoldText = true
            //custom fonts
//            val fontFace = ResourcesCompat.getFont(context, R.font.acrobat)
//            paint.typeface = Typeface.create(fontFace, Typeface.NORMAL)

            // draw text to the Canvas center
            val bounds = Rect()
            productList.forEachIndexed { index, product ->
                //draw the first text
                paint.getTextBounds(product.name, 0, product.name.length, bounds)
                var x = 5f
                var y = (index + 1) * 32f
                canvas.drawText(product.name, x, y, paint)
                //draw the second text
                val price = " "+NumberTextWatcherForThousand.getDecimalFormattedString(product.price)
                paint.getTextBounds(price, 0, price.length, bounds)
                x =
                    ((bitmap.width / 4f) * 2.8f) + ((bitmap.width - ((bitmap.width / 4f) * 2.8f)) - bounds.width())
                canvas.drawText(product.price, x, y, paint)
            }
            return bitmap
        }

        fun getTextRect(data: String): Rect {
            val bounds = Rect()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.textSize = 38f
            paint.getTextBounds(data, 0, data.length, bounds)
            return bounds
        }
        fun productListToProductList3Cost(list: List<ProductModel>): List<ProductModel2> {
            val listProduct = arrayListOf<ProductModel2>()
            for (product in list) {
                var name = product.product_name
                var detail = product.detail
                detail.replace(" "," ")
                name.replace(" "," ")
                listProduct.add(
                    ProductModel2(
                        name,
                        NumberTextWatcherForThousand.getDecimalFormattedString(product.cost) + " บาท"
                    ).setDetail(product.detail)
                )
            }
            return listProduct
        }

        fun productListToProductList2Cost(list: List<ProductModel>): List<ProductModel2> {
            var i = 0
            val listProduct = arrayListOf<ProductModel2>()
            for (product in list) {
                i++
                var name = "" + i + ". " + product.product_name
                name.replace(" "," ")
                if (name.length > 17) {
                    name = name.substring(0, 16).plus("... ")
                }
                listProduct.add(
                    ProductModel2(
                        name,
                        NumberTextWatcherForThousand.getDecimalFormattedString(product.cost) + " บาท"
                    ).setDetail(product.detail)
                )
            }
            return listProduct
        }

        fun productListToProductList3Sell(list: List<ProductModel>): List<ProductModel2> {
            val listProduct = arrayListOf<ProductModel2>()
            for (product in list) {
                var name = product.product_name
                var detail = product.detail
                detail.replace(" "," ")
                name.replace(" "," ")
                listProduct.add(
                    ProductModel2(
                        name,
                        NumberTextWatcherForThousand.getDecimalFormattedString(product.sale) + " บาท"
                    )
                )
            }
            return listProduct
        }

        fun productListToProductList2Sell(list: List<ProductModel>): List<ProductModel2> {
            var i = 0
            val listProduct = arrayListOf<ProductModel2>()
            for (product in list) {
                i++
                var name = "" + i + ". " + product.product_name
                name.replace(" "," ")
                if (name.length > 17) {
                    name = name.substring(0, 16).plus("... ")
                }
                listProduct.add(
                    ProductModel2(
                        name,
                        NumberTextWatcherForThousand.getDecimalFormattedString(product.sale) + " บาท"
                    )
                )
            }
            return listProduct
        }

        fun productListToProductList2Certificate(productModel: ProductModel): List<ProductModel2> {
            val listProduct = arrayListOf<ProductModel2>()
            var name = productModel.product_name
            name.replace(" "," ")
            if (name.length > 17) {
                name = name.substring(0, 16).plus("... ")
            }
            listProduct.add(
                ProductModel2(
                    name,
                    NumberTextWatcherForThousand.getDecimalFormattedString(productModel.sale) + " บาท"
                )
            )

            return listProduct
        }

        fun addComma(number: String): String {
            var result = number
            if (isNumberic(number)) {
                result = String.format("%,d", number.toFloat().roundToInt())
            }
            return result + ".00"
        }

        fun addComma(number: Int): String {
            return String.format("%,d", number)
        }

        fun isNumberic(string: String): Boolean {
            return string.matches("-?\\d+(\\.\\d+)?".toRegex())
        }

        fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
            var drawable =
                ContextCompat.getDrawable(context, drawableId)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable!!).mutate()
            }
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            val bmOverlay =
                Bitmap.createBitmap(
                    bitmap.getWidth() * 19,
                    bitmap.height * 4,
                    Bitmap.Config.ARGB_8888
                )
            val canvas2 = Canvas(bmOverlay)
            canvas2.drawBitmap(bitmap, bitmap.getWidth().toFloat(), 0f, null)
            val paint = Paint()
            paint.setColor(Color.rgb(0, 0, 0))
            paint.setStrokeWidth(1f)
            canvas2.drawLine(
                bitmap.width.toFloat(),
                bitmap.height / 2f,
                (bitmap.getWidth() * 19).toFloat(),
                (bitmap.height / 2f) + 2f,
                paint
            )
            return bmOverlay
        }
    }
}