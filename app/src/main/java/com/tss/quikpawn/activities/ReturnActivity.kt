package com.tss.quikpawn.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import android.widget.TextView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.centerm.centermposoversealib.thailand.ThaiIDSecurityBeen
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tss.quikpawn.BaseK9Activity
import com.tss.quikpawn.R
import com.tss.quikpawn.models.DialogParamModel
import com.tss.quikpawn.models.ProductCodeModel
import com.tss.quikpawn.models.ProductModel
import com.tss.quikpawn.models.ReturnParamModel
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_return.*
import kotlinx.android.synthetic.main.item_customer_info.*
import org.json.JSONObject
import java.lang.reflect.Type

class ReturnActivity : BaseK9Activity() {

    var orderCode: String? = ""
    var productList = mutableListOf<ProductCodeModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return)
        title = getString(R.string.return_item)
        val list = intent?.getStringExtra("product_list")
        orderCode = intent?.getStringExtra("order_code")
        list?.let {
            val productListType: Type = object : TypeToken<ArrayList<ProductModel>?>() {}.type
            val productArray: ArrayList<ProductModel> = Gson().fromJson(it, productListType)
            addItemView(productArray)
        }
        btn_clearsign.setOnClickListener{
            signature_pad.clear()
        }
        btn_ok.setText(R.string.return_item)
        btn_ok.setOnClickListener {
            var signatureBitmap = signature_pad.getSignatureBitmap()
            signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 130, 130, false)
            val customerName = edt_name.text.toString()
            val customerId = edt_idcard.text.toString()
            val signature = Util.bitmapToBase64(signatureBitmap)

            if (!customerName.isEmpty() &&
                !customerId.isEmpty() &&
                !signature.isEmpty()) {

                val list = mutableListOf("รหัสลูกค้า : " + customerId)
                list.add("รหัสรายการ "+orderCode!!)
                for (product in productList) {
                    list.add(product.product_code)
                }

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, "ยืนยัน")
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    val returnModel = ReturnParamModel(
                        customerId,
                        customerName,
                        signature,
                        orderCode!!,
                        productList
                    )
                    Network.returnItem(returnModel, object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            Log.e("panya", "onResponse : $response")
                            val status = response.getString("status_code")
                            if (status == "200") {
                                val dataJsonObj = response.getJSONObject("data")
                                val products = dataJsonObj.getJSONArray("products")
                                val returnOrderCode = dataJsonObj.getString("order_code")
                                val productListType: Type =
                                    object : TypeToken<ArrayList<ProductModel>?>() {}.type
                                val productArray: ArrayList<ProductModel> =
                                    Gson().fromJson(products.toString(), productListType)
                                printSlip(returnOrderCode, productArray)
                                val intent = Intent()
                                intent.putExtra("order_code", orderCode)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                        }

                        override fun onError(error: ANError) {
                            error.printStackTrace()
                            Log.e(
                                "panya",
                                "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                            )
                        }
                    })
                })

            }
        }
        initialK9()
    }

    override fun setupView(info: ThaiIDSecurityBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiFirstName + " " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length - 3) + "XXX")
    }

    fun addItemView(productListModel: ArrayList<ProductModel>) {
        for (productModel in productListModel) {
            productList.add(ProductCodeModel(productModel.product_code))
            val inflater = LayoutInflater.from(baseContext)
            val contentView: View = inflater.inflate(R.layout.item_card_return, null, false)
            val btnDelete = contentView.findViewById<ImageView>(R.id.btn_delete)
            val image = contentView.findViewById<ImageView>(R.id.img_item)
            val txtId = contentView.findViewById<TextView>(R.id.txt_item_id)
            val txtDetail = contentView.findViewById<TextView>(R.id.txt_detail)
            val txtCost = contentView.findViewById<TextView>(R.id.txt_cost)

            txtId.text = productModel.product_id
            txtDetail.text = productModel.detail
            txtCost.text = productModel.cost

            contentView.tag = productModel.product_code
            btnDelete.tag = contentView.tag
            btnDelete.setOnClickListener {
                removeFromList(it.tag as String)
                (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(item_container.findViewWithTag<View>(it.tag))
            }

            contentView.tag = productModel.product_code
            Glide.with(this) //1
                .load(productModel.image_small)
                .placeholder(R.drawable.ic_image_black_24dp)
                .error(R.drawable.ic_broken_image_black_24dp)
                .skipMemoryCache(true) //2
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                .into(image)

            item_container.addView(contentView)
        }
    }

    fun removeFromList(productCode: String) {
        val list = mutableListOf<ProductCodeModel>()
        for (product in productList) {
            if (!productCode.equals(product.product_code)) {
                list.add(product)
            }
        }
        productList = list
    }

    fun printSlip(orderCode: String, productModel: List<ProductModel>) {
        val textList = ArrayList<PrinterParams>()

        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText("รายการ คืนสินค้า")
        textList.add(printerParams1)

        var bitmap = createImageBarcode(orderCode, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(bitmap)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText(orderCode)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        for (product in productModel) {
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
            printerParams1.setTextSize(24)
            printerParams1.setText("*"+product.product_code + " "+product.cost+" บาท")
            textList.add(printerParams1)
        }

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }
}
