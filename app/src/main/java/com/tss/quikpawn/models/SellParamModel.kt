package com.tss.quikpawn.models

data class SellParamModel(var idcard: String, var customer_name: String, var customer_address: String, var customer_image: String, var customer_phonenumber: String, var product: List<SellProductModel>, var total_price: Int)
