package com.tss.quikpawn.models

data class ConsignmentParamModel(var idcard: String, var customer_name: String, var customer_address: String, var customer_image: String, var customer_phonenumber: String, var product: List<ConsignmentProductModel>, var company_id: String, var company_branch_id: String, var interest: String, var expire: String, var user_id:String, var image_cover: String)
