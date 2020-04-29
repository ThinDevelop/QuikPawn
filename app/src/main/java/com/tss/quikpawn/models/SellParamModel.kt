package com.tss.quikpawn.models

data class SellParamModel(var idcard: String, var customer_name: String, var product: List<SellProductModel>, var total_price: Int, var signature: String)
