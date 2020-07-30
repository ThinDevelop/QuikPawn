package com.tss.quikpawn.models

data class ProductModel2(val name: String, val price: String) {

    var detail2: String = ""

    fun setDetail(detail1: String): ProductModel2 {
        detail2 = detail1
        return this
    }

    fun getDetail(): String {
        return detail2
    }
}