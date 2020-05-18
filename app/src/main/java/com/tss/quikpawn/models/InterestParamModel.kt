package com.tss.quikpawn.models

data class InterestParamModel(var order_code: String,
                              var idcard: String,
                              var customer_name: String, var customer_address: String, var customer_image: String, var customer_phonenumber: String,
                              var interest: List<InterestMonthModel>,
                              var mulct_price: String,
                              var signature: String,
                              var user_id: String)