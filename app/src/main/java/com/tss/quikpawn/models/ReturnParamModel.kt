package com.tss.quikpawn.models

data class ReturnParamModel(var idcard: String,var customer_name: String, var customer_address: String, var customer_image: String, var customer_phonenumber: String, var order_code: String, var product: List<ProductCodeModel>)