package com.tss.quikpawn.models

data class ConsignmentResponseModel(var order_code: String,
                                    var date_expire: String,
                                    var price: String,
                                    var price_interest: String,
                                    var total: String,
                                    var products: List<ProductModel>)