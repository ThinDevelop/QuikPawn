package com.tss.quikpawn.networks

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.androidnetworking.interfaces.DownloadProgressListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.tss.quikpawn.PreferencesManager
import com.tss.quikpawn.models.*
import com.tss.quikpawn.util.Util
import java.io.File


class Network {

    companion object {
        val URL_LOGIN = "https://thequikpawn.com/api/v1/login"
        val URL_UPLOAD = "https://thequikpawn.com/api/v1/image/upload"
        val URL_BUY = "https://thequikpawn.com/api/v1/order/buy"
        val URL_CATEGORY = "https://thequikpawn.com/api/v1/category"
        val URL_CONSIGNMENT = "https://thequikpawn.com/api/v1/order/sell/deposit"
        val URL_SEARCH_PRODUCT = "https://thequikpawn.com/api/v1/search/product/code"
        val URL_SEARCH_NAME = "https://thequikpawn.com/api/v1/search/product/name"
        val URL_SEARCH_ORDER = "https://thequikpawn.com/api/v1/search/order/code"
        val URL_INTEREST = "https://thequikpawn.com/api/v1/order/interest"
        val URL_SELL = "https://thequikpawn.com/api/v1/order/sell"
        val URL_REDEEM = "https://thequikpawn.com/api/v1/order/redeem"
        val URL_LEND = "https://thequikpawn.com/api/v1/order/lend"
        val URL_RETURN_BY_PRODUCT = "https://thequikpawn.com/api/v1/order/return/byproduct"
        val URL_SEARCH_BY_IDCARD = "https://thequikpawn.com/api/v1/search/order/byidcard"
        val URL_LOGOUT = "https://thequikpawn.com/api/v1/logout"
        val URL_LOAD_ORDER = "https://thequikpawn.com/api/v1/search/last/print"
        val URL_GET_ORDER_PRINT = "https://thequikpawn.com/api/v1/order/print"

        fun login(user: String, password: String, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_LOGIN)
                .addBodyParameter("username", user)
                .addBodyParameter("password", password)
                .addBodyParameter("serial_number", android.os.Build.SERIAL)
                .setTag("login")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun upload(file: File, listener: JSONObjectRequestListener) {
            AndroidNetworking.upload(URL_UPLOAD)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addMultipartFile("image", file)
                .setTag("upload")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }
        fun uploadBase64(base64: String, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_UPLOAD)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("image", base64)
                .setTag("upload")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun buyItem(buyModel: BuyParamModel, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_BUY)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("idcard", buyModel.idcard)
                .addBodyParameter("customer_name", buyModel.customer_name)
                .addBodyParameter("customer_address", buyModel.customer_address)
                .addBodyParameter("customer_image", buyModel.customer_image)
                .addBodyParameter("customer_phonenumber", buyModel.customer_phonenumber)
                .addBodyParameter("product", Gson().toJson(buyModel.product))
                .addBodyParameter("company_id", buyModel.company_id)
                .addBodyParameter("company_branch_id", buyModel.company_branch_id)
                .addBodyParameter("tid", PreferencesManager.getInstance().tid)
                .addBodyParameter("user_id", buyModel.user_id)
                .setTag("buyItem")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun getCategory(listener: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_CATEGORY)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .setTag("category")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }

        fun getLoadOrder(listener: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_LOAD_ORDER)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addQueryParameter("tid", PreferencesManager.getInstance().tid)
                .addQueryParameter("user_id", PreferencesManager.getInstance().userId)
                .addQueryParameter("limit", "100")
                .setTag("loadOrder")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }

        fun logout(listener: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_LOGOUT)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }

        fun orderConsignment(consignmentParamModel: ConsignmentParamModel, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_CONSIGNMENT)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("idcard", consignmentParamModel.idcard)
                .addBodyParameter("customer_name", consignmentParamModel.customer_name)
                .addBodyParameter("customer_address", consignmentParamModel.customer_address)
                .addBodyParameter("customer_image", consignmentParamModel.customer_image)
                .addBodyParameter("customer_phonenumber", consignmentParamModel.customer_phonenumber)
                .addBodyParameter("product", Gson().toJson(consignmentParamModel.product))
                .addBodyParameter("expire", consignmentParamModel.expire)
                .addBodyParameter("interest", consignmentParamModel.interest)
                .addBodyParameter("customer_name", consignmentParamModel.customer_name)
                .addBodyParameter("company_id", consignmentParamModel.company_id)
                .addBodyParameter("company_branch_id", consignmentParamModel.company_branch_id)
                .addBodyParameter("tid", PreferencesManager.getInstance().tid)
                .addBodyParameter("user_id", consignmentParamModel.user_id)
                .addBodyParameter("image_cover", consignmentParamModel.image_cover)
                .setTag("orderConsignment")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun searchProductByCode(code: String, listener: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_SEARCH_PRODUCT)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addQueryParameter("product_code", code)
                .addQueryParameter("user_id", PreferencesManager.getInstance().userId)
                .setTag("search_product")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }

        fun searchOrder(orderCode: String, typeCode: String, listener: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_SEARCH_ORDER)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addQueryParameter("order_code", orderCode)
                .addQueryParameter("type_code", typeCode)
                .addQueryParameter("user_id", PreferencesManager.getInstance().userId)
                .setTag("category")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }

        fun searchOrderByIdCardAndType(idCard: String, typeCode: String, listener: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_SEARCH_BY_IDCARD)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addQueryParameter("idcard", idCard)
                .addQueryParameter("type_code", typeCode)
                .addQueryParameter("user_id", PreferencesManager.getInstance().userId)
                .setTag("category")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }

        fun searchOrderByIdCard(idCard: String, listener: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_SEARCH_BY_IDCARD)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addQueryParameter("idcard", idCard)
                .addQueryParameter("user_id", PreferencesManager.getInstance().userId)
                .setTag("category")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }

        fun sellItem(sellParamModel: SellParamModel, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_SELL)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("idcard", sellParamModel.idcard)
                .addBodyParameter("customer_name", sellParamModel.customer_name)
                .addBodyParameter("customer_address", sellParamModel.customer_address)
                .addBodyParameter("customer_image", sellParamModel.customer_image)
                .addBodyParameter("customer_phonenumber", sellParamModel.customer_phonenumber)
                .addBodyParameter("product", Gson().toJson(sellParamModel.product))
                .addBodyParameter("total_price", sellParamModel.total_price.toString())
                .addBodyParameter("user_id", PreferencesManager.getInstance().userId)
                .addBodyParameter("tid", PreferencesManager.getInstance().tid)
                .setTag("sellItem")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun interest(interestParamModel: InterestParamModel, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_INTEREST)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("order_code", interestParamModel.order_code)
                .addBodyParameter("idcard", interestParamModel.idcard)
                .addBodyParameter("customer_name", interestParamModel.customer_name)
                .addBodyParameter("customer_address", interestParamModel.customer_address)
                .addBodyParameter("customer_image", interestParamModel.customer_image)
                .addBodyParameter("customer_phonenumber", interestParamModel.customer_phonenumber)
                .addBodyParameter("interest", Gson().toJson(interestParamModel.interest))
                .addBodyParameter("user_id", interestParamModel.user_id)
                .addBodyParameter("tid", PreferencesManager.getInstance().tid)
                .setTag("interest")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun redeem(redeemParamModel: RedeemParamModel, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_REDEEM)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("order_code", redeemParamModel.order_code)
                .addBodyParameter("idcard", redeemParamModel.idcard)
                .addBodyParameter("customer_name", redeemParamModel.customer_name)
                .addBodyParameter("customer_address", redeemParamModel.customer_address)
                .addBodyParameter("customer_image", redeemParamModel.customer_image)
                .addBodyParameter("customer_phonenumber", redeemParamModel.customer_phonenumber)
                .addBodyParameter("interest", Gson().toJson(redeemParamModel.interest))
                .addBodyParameter("principle_price", redeemParamModel.principle_price)
                .addBodyParameter("mulct_price", redeemParamModel.mulct_price)
                .addBodyParameter("user_id", redeemParamModel.user_id)
                .addBodyParameter("tid", PreferencesManager.getInstance().tid)
                .setTag("interest")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun lend(lendParamModel: LendParamModel, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_LEND)
                .addHeaders("Authorization", "Bearer "+ PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("idcard", lendParamModel.idcard)
                .addBodyParameter("customer_name", lendParamModel.customer_name)
                .addBodyParameter("customer_address", lendParamModel.customer_address)
                .addBodyParameter("customer_image", lendParamModel.customer_image)
                .addBodyParameter("customer_phonenumber", lendParamModel.customer_phonenumber)
                .addBodyParameter("product", Gson().toJson(lendParamModel.product))
                .addBodyParameter("deadline", lendParamModel.deadline)
                .addBodyParameter("user_id", PreferencesManager.getInstance().userId)
                .addBodyParameter("tid", PreferencesManager.getInstance().tid)
                .setTag("sellItem")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(listener)
        }

        fun returnItem(returnParamModel: ReturnParamModel, listener: JSONObjectRequestListener) {
            AndroidNetworking.post(URL_RETURN_BY_PRODUCT)
                .addHeaders("Authorization", "Bearer " + PreferencesManager.getInstance().token)
                .addHeaders("Content-type", "application/json")
                .addHeaders("Accept", "application/json")
                .addBodyParameter("order_code", returnParamModel.order_code)
                .addBodyParameter("idcard", returnParamModel.idcard)
                .addBodyParameter("customer_name", returnParamModel.customer_name)
                .addBodyParameter("customer_address", returnParamModel.customer_address)
                .addBodyParameter("customer_image", returnParamModel.customer_image)
                .addBodyParameter("customer_phonenumber", returnParamModel.customer_phonenumber)
                .addBodyParameter("product", Gson().toJson(returnParamModel.product))
                .addBodyParameter("user_id", PreferencesManager.getInstance().userId)
                .addBodyParameter("tid", PreferencesManager.getInstance().tid)
                .setTag("category")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(listener)
        }



        fun downloadPDF(url: String, context: Context) {
            AndroidNetworking.download(url, Util.getAppPath(context), "pdf_test.pdf")
                .setTag("downloadTest")
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(object : DownloadProgressListener {
                    override fun onProgress(bytesDownloaded: Long, totalBytes: Long) {

                    }
                })
                .startDownload(object : DownloadListener{
                    override fun onDownloadComplete() {

                    }

                    override fun onError(anError: ANError?) {

                    }
                })
        }
    }
}