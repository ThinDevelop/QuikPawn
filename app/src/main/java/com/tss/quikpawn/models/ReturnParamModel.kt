package com.tss.quikpawn.models

data class ReturnParamModel(var idcard: String,var customer_name: String,var signature: String, var order_code: String, var product: List<ProductCodeModel>)