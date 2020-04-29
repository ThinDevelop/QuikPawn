package com.tss.quikpawn.models

data class RedeemParamModel (var order_code: String,
                        var idcard: String,
                        var customer_name: String,
                        var interest: List<InterestMonthModel>,
                        var principle_price: String,
                        var mulct_price: String,
                        var signature: String,
                        var user_id: String)