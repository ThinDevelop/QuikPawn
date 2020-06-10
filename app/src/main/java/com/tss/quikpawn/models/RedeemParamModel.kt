package com.tss.quikpawn.models

data class RedeemParamModel (var order_code: String,
                        var idcard: String,
                        var customer_name: String, var customer_address: String, var customer_image: String, var customer_phonenumber: String,
                        var interest: List<InterestMonthModel>,
                        var principle_price: String,
                        var mulct_price: String,
                        var user_id: String)