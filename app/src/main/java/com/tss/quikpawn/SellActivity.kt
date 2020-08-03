package com.tss.quikpawn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tss.quikpawn.models.*
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.NumberTextWatcherForThousand
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_sell.*
import kotlinx.android.synthetic.main.item_customer_info.*
import kotlinx.android.synthetic.main.item_search.*
import org.json.JSONObject
import java.lang.reflect.Type


class SellActivity : BaseK9Activity() {

    var productName = HashMap<String, String>()
    var productList = mutableListOf<SellProductModel>()
    var orderCode: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)
        title = getString(R.string.sell_item)
        val list = intent?.getStringExtra("product_list")
        orderCode = intent?.getStringExtra("order_code")
        list?.let {
            title = getString(R.string.pay_item)
            search_bar.visibility = View.GONE
            val userListType: Type = object : TypeToken<ArrayList<ProductModel>?>() {}.type
            val userArray: ArrayList<ProductModel> = Gson().fromJson(it, userListType)
            for (product in userArray) {
                productList.add(
                    SellProductModel(
                        product.product_code,
                        product.sale.replace(".00", "").toInt()
                    )
                )
                productName.put(product.product_code, product.product_name)
                addItemView(product)
            }
            updateSummaryPrice()
        }
        scan.setOnClickListener {
            val intent = Intent(this@SellActivity, ScanActivity::class.java)
            startActivityForResult(intent, SCAN_REQUEST_CODE)
        }
        btn_clearsign.setOnClickListener {
            signature_pad.clear()
        }

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

            if (!customerName.isEmpty()){
                if (productList.size == 0) {
                    DialogUtil.showNotiDialog(this@SellActivity, getString(R.string.data_missing), getString(R.string.please_add_item))
                    return@setOnClickListener
                }
                var totle_price = 0
                for (product in productList) {
                    if (product.sale_price == 0) {
                        DialogUtil.showNotiDialog(this@SellActivity, getString(R.string.please_add_price), getString(R.string.please_add_sale_price))
                        return@setOnClickListener
                    }
                    totle_price += product.sale_price
                }

                    var sum = 0L
                    val list = mutableListOf("รหัสลูกค้า : "+ customerId+"\nรายการ\n")
                    var a = 0
                    for (product in productList) {
                        a++
                        var name = product.product_code
                        productName.get(product.product_code)?.let {
                            name = it
                        }
                        list.add(a.toString() +". "+name+ " : " +Util.addComma(product.sale_price.toString()))
                        sum += product.sale_price
                    }
                    list.add("รวม "+ Util.addComma(sum.toString()) +" บาท")

                    val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, getString(R.string.text_confirm), getString(R.string.text_cancel))
                    DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                        if (DialogUtil.CONFIRM.equals(it)) {
                            val dialog = createProgressDialog(this, "Loading...")
                            dialog.show()
                            val model = SellParamModel(
                                customerId,
                                customerName,
                                customerAddress,
                                customerPhoto,
                                edt_phonenumber.text.toString(),
                                productList,
                                totle_price
                            )
                            Network.sellItem(model, object : JSONObjectRequestListener {
                                override fun onResponse(response: JSONObject) {
                                    dialog.dismiss()
                                    Log.e("panya", "onResponse : $response")
                                    val status = response.getString("status_code")
                                    if (status == "200") {
                                        val data = response.getJSONObject("data")
                                        printSlip(
                                            Gson().fromJson(
                                                data.toString(),
                                                OrderModel::class.java
                                            )
                                        )
                                        Handler().postDelayed({
                                            printCertificate(
                                                Gson().fromJson(
                                                    data.toString(),
                                                    OrderModel::class.java
                                                )
                                            )
                                            showConfirmDialog(data)
                                        }, 2000)
                                    } else {
                                        showResponse(status, this@SellActivity)
                                    }
                                }

                                override fun onError(error: ANError) {
                                    dialog.dismiss()

                                    var status = error.errorCode.toString()
                                    error.errorBody?.let {
                                        val jObj = JSONObject(it)
                                        if (jObj.has("status_code")) {
                                            status = jObj.getString("status_code")
                                        }
                                    }
                                    showResponse(status, this@SellActivity)
                                    error.printStackTrace()
                                    Log.e(
                                        "panya",
                                        "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                                    )
                                }
                            })
                        }
                })
            } else {
                Toast.makeText(this@SellActivity, "ข้อมูลไม่ครบถ้วน", Toast.LENGTH_LONG).show()
            }
        }
        edt_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (!query.equals("")) {
                        if (checkContains(query)) {
                            Toast.makeText(this@SellActivity, "สินค้านี้ถูกเพิ่มในรายการแล้ว", Toast.LENGTH_LONG).show()
                        } else {
                            loadItem(query)
                        }
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        initialK9()
    }

    fun showConfirmDialog(data: JSONObject) {
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
            }
            orderCode?.let {
                val intent = Intent()
                intent.putExtra("order_code", orderCode)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SCAN_REQUEST_CODE) {
                val barcode = data?.getStringExtra("barcode")
                if (checkContains(barcode!!)) {
                    Toast.makeText(
                        this@SellActivity,
                        "สินค้านี้ถูกเพิ่มในรายการแล้ว",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    loadItem(barcode!!)
                }
            }
        }
    }

    fun checkContains(barcode: String): Boolean {
        for (product in productList) {
            if (product.product_code.equals(barcode)) {
                return true
            }
        }
        return false
    }

    fun loadItem(key: String) {
        val dialog = createProgressDialog(this, "Loading...")
        dialog.show()
        Network.searchProductByCode(key, object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                dialog.dismiss()
                Log.e("panya", "onResponse : $response")
                val status = response.getString("status_code")
                if (status == "200") {
                    val dataJsonObj = response.getJSONObject("data")
                    val data = Gson().fromJson(dataJsonObj.toString(), ProductModel::class.java)
                    val product = SellProductModel(key, 0)//Integer.parseInt(data.sale.replace(".00", "")))
                    productList.add(product)
                    productName.put(key, data.product_name)
                    if (data.status_id.equals(ProductStatus.READY_FOR_SALE.statusId)) {
                        addItemView(data)
                        updateSummaryPrice()
                    } else {
                        DialogUtil.showNotiDialog(this@SellActivity, "สินค้าไม่พร้อมขาย", "")
                    }
                } else {
                    showResponse(status, this@SellActivity)
                }
            }

            override fun onError(error: ANError) {
                dialog.dismiss()

                var status = error.errorCode.toString()
                error.errorBody?.let {
                    val jObj = JSONObject(it)
                    if (jObj.has("status_code")) {
                        status = jObj.getString("status_code")
                    }
                }
                showResponse(status, this@SellActivity)
                error.printStackTrace()
                Log.e(
                    "panya",
                    "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                )
            }
        })
    }

    fun addItemView(productModel: ProductModel) {
        val inflater = LayoutInflater.from(baseContext)
        val contentView: View = inflater.inflate(R.layout.item_cardview_sell, null, false)
        val delete = contentView.findViewById<ImageView>(R.id.btn_delete)
        val image = contentView.findViewById<ImageView>(R.id.img_item)
        val txtId = contentView.findViewById<TextView>(R.id.txt_item_id)
        val txtDetail = contentView.findViewById<TextView>(R.id.txt_detail)
        val txtCost = contentView.findViewById<TextView>(R.id.txt_cost)
        val txtsell = contentView.findViewById<TextView>(R.id.txt_sell)
        txtsell.visibility = View.VISIBLE
        txtId.text = productModel.product_name
        txtDetail.text = productModel.detail
        txtCost.text = Util.addComma(productModel.cost) + " บาท"
        txtsell.text = "0 บาท"//Util.addComma(productModel.sale) + "0 บาท"
        delete.visibility = View.VISIBLE
        contentView.tag = productModel.product_code
        delete.tag = contentView.tag
        delete.setOnClickListener {
            val list = mutableListOf<SellProductModel>()
            for (product in productList) {
                if (product.product_code != it.tag) {
                    list.add(product)
                }
            }
            productList = list
            updateSummaryPrice()
            (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(
                item_container.findViewWithTag<View>(it.tag)
            )
        }
        Glide.with(this) //1
            .load(productModel.image_small)
            .placeholder(R.drawable.ic_image_black_24dp)
            .error(R.drawable.ic_broken_image_black_24dp)
            .skipMemoryCache(true) //2
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE) //3
            .into(image)

        contentView.setOnClickListener {
            DialogUtil.showInputDialog(
                this@SellActivity,
                object : DialogUtil.InputTextBackListerner {
                    override fun onClickConfirm(result: String?) {
                        result?.let {
                            if (it.isEmpty()) return
                            val price = result.replace(",", "").toInt()
                            val productCode = contentView.tag as String
                            val sellProductModel = SellProductModel(productCode, price)
                            updatePrice(sellProductModel)
                            Util.addComma(productModel.sale)
                            txtsell.text = Util.addComma(price.toString()) + "บาท"
                            updateSummaryPrice()

                        }
                    }
                })
        }
        item_container.addView(contentView)
    }

    fun updatePrice(sellPrice: SellProductModel) {
        for (product in productList) {
            if (product.product_code.equals(sellPrice.product_code)) {
                product.sale_price = sellPrice.sale_price
                break
            }
        }
    }

    fun updateSummaryPrice() {
        var sumPrice = 0L
        for (product in productList) {
            sumPrice += product.sale_price
        }
        txt_summary_price.text = "ราคารวม " + Util.addComma(sumPrice.toString()) + " บาท"
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiTitle +" "+info.thaiFirstName + "  " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length - 3) + "XXX")
    }

    fun printSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        val title = Util.textToBitmap(data.type_name)
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

        textList.add(getAddress())
        textList.add(getPhoneNumber())
        textList.add(getZipCode())

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ลูกค้า " + data.customer_name)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รหัสปชช. " + data.idcard)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

//        val list = Util.productListToProductList2Sell(data.products)
//        val listBitmap = Util.productListToBitmap(list)
//        printerParams1 = TssPrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
//        printerParams1.setBitmap(listBitmap)
//        textList.add(printerParams1)

        var i = 0
        for (product in data.products) {
            i++
            var name = product.product_name
            var detail = product.detail
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("\n" + i + ". " + name.replace(" "," ")+"\n"+detail.replace(" "," "))
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

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        printerParams1.setText("ชำระเงิน " + Util.addComma(data.total) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@SellActivity))
        printdata(textList)
    }

    fun printCertificate(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()
        for (product in data.products) {
            var bitmap = createImageBarcode(data.order_code, "Barcode")!!
            bitmap = Utility.toGrayscale(bitmap)

            val title = Util.textToBitmap("ใบรับประกัน")
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
            printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
            textList.add(printerParams1)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("สาขา " + PreferencesManager.getInstance().companyBranchName)
            textList.add(printerParams1)

            textList.add(getAddress())
            textList.add(getPhoneNumber())
            textList.add(getZipCode())

            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
            textList.add(printerParams1)

            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("ลูกค้า " + data.customer_name)
            textList.add(printerParams1)

            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("รหัสปชช. " + data.idcard +"\nรายการสินค้า")
            textList.add(printerParams1)

            var i = 0
            for (product in data.products) {
                i++
                val name = product.product_name
                val detail = product.detail
                printerParams1 = TssPrinterParams()
                printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
                printerParams1.setTextSize(20)
                printerParams1.setText("\n" + i + ". " + name.replace(" "," ")+"\n"+detail.replace(" "," "))
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
            printerParams1.setText("\n\n\n")
            textList.add(printerParams1)
            textList.add(Util.dashLine(this@SellActivity))
        }
        printdata(textList)
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
}
