package com.tss.quikpawn.models

data class BuyResponseModel(var order_code: String, var price: String, var products: List<ProductModel>)