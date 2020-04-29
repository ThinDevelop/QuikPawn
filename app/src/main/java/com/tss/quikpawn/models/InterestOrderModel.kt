package com.tss.quikpawn.models

data class InterestOrderModel(var order_code: String,
                              var idcard: String,
                              var customer_name: String,
                              var signature: String,
                              var total: String,
                              var interest_month: String,
                              var interest_price: String,
                              var num_expire: String,
                              var date_expire: String,
                              var products: List<ProductModel>,
                              var interest: List<InterestModel>)