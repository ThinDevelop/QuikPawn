package com.tss.quikpawn.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.tss.quikpawn.PreferencesManager
import com.tss.quikpawn.R
import com.tss.quikpawn.models.*
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_buy.*
import kotlinx.android.synthetic.main.activity_return.*
import kotlinx.android.synthetic.main.item_customer_info.*
import kotlinx.android.synthetic.main.item_customer_info.btn_ok
import kotlinx.android.synthetic.main.item_customer_info.edt_idcard
import kotlinx.android.synthetic.main.item_customer_info.edt_name
import kotlinx.android.synthetic.main.item_customer_info.edt_phonenumber
import kotlinx.android.synthetic.main.item_customer_info.signature_pad
import kotlinx.android.synthetic.main.item_sign_view.*
import org.json.JSONObject
import java.lang.reflect.Type

class ReturnActivity : BaseK9Activity() {

    var orderCode: String? = ""
    var productList = mutableListOf<ProductCodeModel>()
    var productModelList = mutableListOf<ProductModel>()

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
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            var customerAddress = address
            var customerPhoto = customerPhoto
            var customerPhone = edt_phonenumber.text.toString()
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            if (customerPhone.length != 10) {
                DialogUtil.showNotiDialog(this, getString(R.string.data_is_wrong), getString(R.string.wrong_phone_number))
                return@setOnClickListener
            }

            if (!customerName.isEmpty()) {

                val list = mutableListOf("รหัสลูกค้า : " + customerId)
                list.add("รหัสรายการ "+orderCode!!+ "\nรายการ")
                var sum = 0
                for (product in productList) {
                    list.add(getProductNameByCode(product.product_code))
                    sum ++
                }
                list.add("รวม " + sum.toString() + " ชิ้น")
                if (productList.isEmpty()) {
                    DialogUtil.showNotiDialog(
                        this@ReturnActivity,
                        getString(R.string.title_error),
                        getString(R.string.data_missing)
                    )
                }

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, getString(R.string.text_confirm), getString(R.string.text_cancel))
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    if (DialogUtil.CONFIRM.equals(it)) {
                        val returnModel = ReturnParamModel(
                            customerId,
                            customerName,
                            customerAddress,
                            customerPhoto,
                            customerPhone,
                            orderCode!!,
                            productList
                        )
                        Network.returnItem(returnModel, object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                Log.e("panya", "onResponse : $response")
                                val status = response.getString("status_code")
                                if (status == "200") {
                                    val dataJsonObj = response.getJSONObject("data")
                                    val orderModel = Gson().fromJson(
                                        dataJsonObj.toString(),
                                        OrderModel::class.java
                                    )
                                    printSlip(orderModel)
                                    showConfirmDialog(orderModel)
                                } else {
                                    showResponse(status, this@ReturnActivity)
                                }
                            }

                            override fun onError(error: ANError) {
                                error.printStackTrace()
                                var status = error.errorCode.toString()
                                error.errorBody?.let {
                                    val jObj = JSONObject(it)
                                    if (jObj.has("status_code")) {
                                        status = jObj.getString("status_code")
                                    }
                                }
                                showResponse(status, this@ReturnActivity)
                                Log.e(
                                    "panya",
                                    "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                                )
                            }
                        })
                    }
                })

            }
        }
        initialK9()
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.please_back_again), Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    fun showConfirmDialog(orderModel: OrderModel) {
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printSlip(orderModel)
            }
            val intent = Intent()
            intent.putExtra("order_code", orderCode)
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiTitle +" "+info.thaiFirstName + "  " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length - 3) + "XXX")
    }

    fun addItemView(productListModel: ArrayList<ProductModel>) {
        for (productModel in productListModel) {
            productModelList.add(productModel)
            productList.add(ProductCodeModel(productModel.product_code))
            val inflater = LayoutInflater.from(baseContext)
            val contentView: View = inflater.inflate(R.layout.item_card_return, null, false)
            val btnDelete = contentView.findViewById<ImageView>(R.id.btn_delete)
            val image = contentView.findViewById<ImageView>(R.id.img_item)
            val txtId = contentView.findViewById<TextView>(R.id.txt_item_id)
            val txtDetail = contentView.findViewById<TextView>(R.id.txt_detail)
            val txtCost = contentView.findViewById<TextView>(R.id.txt_cost)

            txtId.text = productModel.product_name
            txtDetail.text = productModel.detail
            txtCost.text = productModel.sale + " บาท"

            contentView.tag = productModel.product_code
            btnDelete.tag = contentView.tag
            btnDelete.setOnClickListener {
                removeFromList(it.tag as String)
                removeProduct(it.tag as String)
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

    fun removeProduct(product_code: String) {
        for (product in productModelList) {
            if (product.equals(product_code)) {
                productModelList.remove(product)
                break
            }
        }
    }

    fun getProductNameByCode(code: String): String {
        for (product in productModelList) {
            if (product.product_code.equals(code)) {
                return product.product_name
            }
        }
        return code
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

    fun printSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        val title = Util.textToBitmap("คืนสินค้า")
        var printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(title)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n")
        textList.add(printerParams1)

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(bitmap)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText(data.order_code)
        textList.add(printerParams1)


        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ร้าน "+ PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา "+ PreferencesManager.getInstance().companyBranchName)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันครบกำหนด " + Util.toDateFormat(data.date_expire))
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ลูกค้า "+data.customer_name)
        textList.add(printerParams1)


        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รหัสปชช. "+data.idcard)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        var i = 0
        for (product in data.products) {
            i++
            var name = product.product_name
            var detail = product.detail
            detail.replace(" ", " ")
            name.replace(" ", " ")
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("\n" + i + ". " + name+"\n"+detail)
            textList.add(printerParams1)

            val listProduct = arrayListOf<ProductModel>()
            listProduct.add(product)
            val list = Util.productListToProductList3Sell(listProduct)
            val listBitmap = Util.productListToBitmap2(list)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)
        }

        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReturnActivity))
        printdata(textList)
    }
}
