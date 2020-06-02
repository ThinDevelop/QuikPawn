package com.tss.quikpawn.util

import android.graphics.*
import android.util.Base64
import com.tss.quikpawn.models.ProductModel
import com.tss.quikpawn.models.ProductModel2
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import kotlin.math.roundToInt


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

        fun productListToBitmap(productList: List<ProductModel2>): Bitmap {
            val bitmap = Bitmap.createBitmap(600, (productList.size*32)+10, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            // new antialised Paint
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb(0,0, 0)
            // text size in pixels
            paint.textSize = 30f
            //custom fonts
//            val fontFace = ResourcesCompat.getFont(context, R.font.acrobat)
//            paint.typeface = Typeface.create(fontFace, Typeface.NORMAL)

            // draw text to the Canvas center
            val bounds = Rect()
            productList.forEachIndexed { index, product ->
                //draw the first text
                paint.getTextBounds(product.name, 0, product.name.length, bounds)
                var x = 5f
                var y = (index+1)*32f
                canvas.drawText(product.name, x, y, paint)
                //draw the second text
                val price = NumberTextWatcherForThousand.getDecimalFormattedString(product.price)
                paint.getTextBounds(price, 0, price.length, bounds)
                x = ((bitmap.width / 4f)*2.8f)+ ((bitmap.width - ((bitmap.width / 4f)*2.8f))-bounds.width())
                canvas.drawText(product.price, x, y, paint)
            }
            return bitmap
        }

        fun productListToProductList2Cost(list: List<ProductModel>): List<ProductModel2> {
            var i = 0
            val listProduct = arrayListOf<ProductModel2>()
            for (product in list) {
                i++
                val name = "" + i + ". " + product.product_name
                if (name.length > 21) {
                    name.substring(0, 20).plus("...")
                }
                listProduct.add(ProductModel2(name, NumberTextWatcherForThousand.getDecimalFormattedString(product.cost) + " บาท"))
            }
            return listProduct
        }

        fun productListToProductList2Sell(list: List<ProductModel>): List<ProductModel2> {
            var i = 0
            val listProduct = arrayListOf<ProductModel2>()
            for (product in list) {
                i++
                val name = "" + i + ". " + product.product_name
                if (name.length > 21) {
                    name.substring(0, 20).plus("...")
                }
                listProduct.add(ProductModel2(name, NumberTextWatcherForThousand.getDecimalFormattedString(product.sale) + " บาท"))
            }
            return listProduct
        }

        fun productListToProductList2Certificate(productModel: ProductModel): List<ProductModel2> {
            val listProduct = arrayListOf<ProductModel2>()
                val name = productModel.product_name
                if (name.length > 21) {
                    name.substring(0, 20).plus("...")
                }
                listProduct.add(ProductModel2(name, NumberTextWatcherForThousand.getDecimalFormattedString(productModel.sale) + " บาท"))

            return listProduct
        }

        fun addComma(number: String): String {
            var result = number
            if (isNumberic(number)) {
                result = String.format("%,d", number.toFloat().roundToInt())
            }
            return result+".00"
        }

        fun addComma(number: Int): String {
            return String.format("%,d", number)
        }

        fun isNumberic(string: String): Boolean {
            return string.matches("-?\\d+(\\.\\d+)?".toRegex())
        }
    }
}