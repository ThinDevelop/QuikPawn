package com.tss.quikpawn.models

data class ProductModel(var product_id: String,
                        var product_code: String,
                        var product_name: String,
                        var category_id: String,
                        var detail: String,
                        var status_id: String,
                        var status_name: String,
                        var category: String,
                        var cost: String,
                        var rate: String,
                        var sale: String,
                        var image_big: String,
                        var image_small: String)