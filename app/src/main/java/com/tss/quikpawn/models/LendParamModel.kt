package com.tss.quikpawn.models

data class LendParamModel(var idcard: String, var customer_name: String, var product: List<SellProductModel>, var deadline: String, var signature: String)
