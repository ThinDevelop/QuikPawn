package com.tss.quikpawn.models

data class BuyParamModel(var idcard: String, var customer_name: String, var product: List<BuyProductModel>, var company_id: String, var company_branch_id: String, var signature: String, var user_id:String)
